package com.aliyun.sls.otel.profiling.action;

import com.alibaba.cpc.asyncprofiler.labels.LabelsSet;
import com.alibaba.cpc.asyncprofiler.labels.ScopedContext;
import com.alibaba.cpc.tracing.TracingProfiling;
import com.aliyun.sls.otel.profiling.config.Constants;
import com.aliyun.sls.otel.profiling.selector.ProfilingKey;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.aliyun.sls.otel.profiling.constant.Constants.*;
import static com.aliyun.sls.otel.profiling.selector.ProfilingKey.newKey;

/**
 * AlibabaProfilingAction is the implementation of ProfilingAction, it provides the profiling action.
 */
public enum AlibabaProfilingAction implements ProfilingAction {

    INSTANCE;

    protected static final Logger LOGGER = Logger.getLogger(AlibabaProfilingAction.class.getName());
    // <TraceID, <ThreadID, ScopeContext>>
    protected final Map<ProfilingKey, Map<Long, ScopedContext>> profilingTraces = new ConcurrentSkipListMap<>();

    @Override
    public final void stopProfiling(ReadableSpan readableSpan) {
        stopProfiling(readableSpan.getSpanContext().getTraceId(), Thread.currentThread().getId());
    }

    private void stopProfiling(String traceID, Long threadID) {
        ScopedContext scopedContext = profilingTraces.getOrDefault(newKey(traceID), Collections.emptyMap())
                .remove(Thread.currentThread().getId());

        if (scopedContext != null) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("stop profiling for traceId: " + traceID + " threadID: " + Thread.currentThread().getId());
            }
            scopedContext.close();
            TracingProfiling.instance().removeCurrentThread();
            removeProfilingTraceId(traceID);
        }
    }

    @Override
    public final void startProfiling(ReadWriteSpan readWriteSpan) {
        Long threadId = Thread.currentThread().getId();

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("start profiling for traceId: " + readWriteSpan.getSpanContext().getTraceId() + " threadID: "
                    + threadId);
        }

        ScopedContext scopedContext = new ScopedContext(new LabelsSet(initLables(readWriteSpan)));
        TracingProfiling.instance().addCurrentThread();
        profilingTraces.computeIfAbsent(newKey(readWriteSpan.getSpanContext().getTraceId()),
                profilingKey -> new ConcurrentHashMap<>(5, 1)).put(threadId, scopedContext);

    }

    @Override
    public final void addProfilingTraceId(String traceID) {
        profilingTraces.computeIfAbsent(newKey(traceID), profilingKey -> new ConcurrentHashMap<>(5, 1));
    }

    public final void removeProfilingTraceId(String traceID) {
        Map<Long, ScopedContext> contextMap = profilingTraces.get(newKey(traceID));
        if (contextMap.isEmpty()) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("remove profiling traceId: " + traceID);
            }
            profilingTraces.remove(newKey(traceID));
        }
    }

    protected static Map<String, String> initLables(ReadWriteSpan span) {
        Map<String, String> labels = new HashMap<>();

        labels.put(LABEL_PROFILE_ID, span.getSpanContext().getSpanId());
        labels.put(LABEL_TRACE_ID, span.getSpanContext().getTraceId());
        labels.put(LABEL_SPAN_ID, span.getSpanContext().getSpanId());

        return labels;
    }

    @Override
    public int currentProfilingTraceSize() {
        return profilingTraces.size();
    }

    @Override
    public boolean checkIfAlreadyProfiling(Context context, String traceId) {
        Boolean profilingFlag = context.get(Constants.PROFILING_FLAG);
        if (profilingFlag != null && profilingFlag) {
            return true;
        }

        return profilingTraces.containsKey(newKey(traceId));
    }

    @Override
    public void cleanTimeoutProfilingTrace(long timeout) {
        Iterator<Map.Entry<ProfilingKey, Map<Long, ScopedContext>>> iterator = profilingTraces.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ProfilingKey, Map<Long, ScopedContext>> entry = iterator.next();
            if (System.currentTimeMillis() - entry.getKey().getProfilingStartTimeMillis() > timeout) {
                cleanProfilingTrace(entry);
                continue;
            }
        }
    }

    private void cleanProfilingTrace(Map.Entry<ProfilingKey, Map<Long, ScopedContext>> entry) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("clean timeout profiling traceId: " + entry.getKey().getTraceID() + " startTime: "
                    + entry.getKey().getProfilingStartTimeMillis());
        }

        Iterator<Map.Entry<Long, ScopedContext>> iterator = entry.getValue().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, ScopedContext> contextEntry = iterator.next();
            stopProfiling(entry.getKey().getTraceID(), contextEntry.getKey());
            iterator.remove();
        }
    }

}
