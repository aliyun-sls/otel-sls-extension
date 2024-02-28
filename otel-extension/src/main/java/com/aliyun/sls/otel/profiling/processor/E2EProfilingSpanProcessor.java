package com.aliyun.sls.otel.profiling.processor;

import java.util.logging.Logger;

import com.aliyun.sls.otel.profiling.service.ProfilingService;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

/**
 * E2EProfilingSpanProcessor is the span processor for profiling. It will try to start profiling when span start and
 * stop profiling when span end.
 */
public class E2EProfilingSpanProcessor implements SpanProcessor {

    private static final Logger logger = Logger.getLogger(E2EProfilingSpanProcessor.class.getName());
    private final ProfilingService profilingService;
    private static final AttributeKey<String> PROFILING_ATTRIBUTE = AttributeKey.stringKey("profiling");

    public E2EProfilingSpanProcessor(ProfilingService profilingService) {
        this.profilingService = profilingService;
    }

    @Override
    public void onStart(Context context, ReadWriteSpan readWriteSpan) {
        try {
            if (profilingService.tryToProfiling(readWriteSpan)) {
                readWriteSpan.setAttribute(PROFILING_ATTRIBUTE, "true");
            }
        } catch (Exception e) {
            logger.warning("Failed to start profiling: " + e.getMessage());
        }
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan readableSpan) {
        try {
            if (readableSpan.getAttribute(PROFILING_ATTRIBUTE) != null) {
                this.profilingService.stopProfiling(readableSpan);
            }
        } catch (Exception e) {
            logger.warning("Failed to stop profiling: " + e.getMessage());
        }
    }

    @Override
    public boolean isEndRequired() {
        return true;
    }
}
