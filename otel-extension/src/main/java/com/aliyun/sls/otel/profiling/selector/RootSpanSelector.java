package com.aliyun.sls.otel.profiling.selector;

import io.opentelemetry.sdk.trace.ReadWriteSpan;

/**
 * RootSpanSelector is the selector for root span.
 */
public class RootSpanSelector extends ProfilingIntervalLimitSelector {

    public RootSpanSelector(long intervalMillis) {
        super(intervalMillis);
    }

    @Override
    protected boolean checkProfilingRule(ReadWriteSpan span) {
        LOGGER.info("RootSpanSelector: checkProfilingRule: " + span.getParentSpanContext().getSpanId());
        if ("0000000000000000".equals(span.getParentSpanContext().getSpanId())) {
            return true;
        }
        return false;
    }
}
