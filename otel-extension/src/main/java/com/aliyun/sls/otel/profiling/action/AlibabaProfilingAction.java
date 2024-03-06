package com.aliyun.sls.otel.profiling.action;

import static com.aliyun.sls.otel.profiling.constant.Constants.LABEL_PROFILE_ID;
import static com.aliyun.sls.otel.profiling.constant.Constants.LABEL_SPAN_ID;
import static com.aliyun.sls.otel.profiling.constant.Constants.LABEL_TRACE_ID;
import static com.aliyun.sls.otel.profiling.selector.ProfilingKey.newKey;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alibaba.cpc.asyncprofiler.labels.LabelsSet;
import com.alibaba.cpc.asyncprofiler.labels.ScopedContext;
import com.alibaba.cpc.tracing.TracingProfiling;
import com.aliyun.sls.otel.profiling.selector.ProfilingKey;

import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;

/**
 * AlibabaProfilingAction is the implementation of ProfilingAction, it provides the profiling action.
 */
public enum AlibabaProfilingAction implements ProfilingAction {

    INSTANCE;

    protected static final Logger LOGGER = Logger.getLogger(AlibabaProfilingAction.class.getName());
    // <TraceID, <SpanID, ScopeContext>>
    protected final Map<ProfilingKey, Map<String, ScopedContext>> profilingTraces = new ConcurrentSkipListMap<>();

    @Override
    public final void stopProfiling(ReadableSpan readableSpan) {
        stopProfiling(readableSpan.getSpanContext().getTraceId(), readableSpan.getSpanContext().getSpanId());
    }

    private void stopProfiling(String traceID, String spanID) {
        ScopedContext scopedContext = profilingTraces.getOrDefault(newKey(traceID), Collections.emptyMap())
                .remove(spanID);

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
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("start profiling for traceId: " + readWriteSpan.getSpanContext().getTraceId() + " spanID:"
                        + readWriteSpan.getSpanContext().getSpanId() + " in threadID: " + Thread.currentThread().getId());
        }

        ScopedContext scopedContext = new ScopedContext(new LabelsSet(initLables(readWriteSpan)));
        TracingProfiling.instance().addCurrentThread();
        profilingTraces
                .computeIfAbsent(newKey(readWriteSpan.getSpanContext().getTraceId()),
                        profilingKey -> new ConcurrentHashMap<>(5, 1))
                .put(readWriteSpan.getSpanContext().getSpanId(), scopedContext);

    }

    @Override
    public final void addProfilingTraceId(String traceID) {
        profilingTraces.computeIfAbsent(newKey(traceID), profilingKey -> new ConcurrentHashMap<>(5, 1));
    }

    public final void removeProfilingTraceId(String traceID) {
        Map<String, ScopedContext> contextMap = profilingTraces.get(newKey(traceID));
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
    public boolean checkIfAlreadyProfiling(String traceId) {
        return profilingTraces.containsKey(newKey(traceId));
    }

}
