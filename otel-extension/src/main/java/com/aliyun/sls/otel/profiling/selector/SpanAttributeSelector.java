package com.aliyun.sls.otel.profiling.selector;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.trace.ReadWriteSpan;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Span attribute selector.
 */
public class SpanAttributeSelector extends ProfilingIntervalLimitSelector {

    private final Map<String, String> attributesMappings;

    public SpanAttributeSelector(long intervalMillis, Map<String, String> attributesMappings) {
        super(intervalMillis);
        this.attributesMappings = attributesMappings;
    }

    public SpanAttributeSelector(Map<String, String> attributesMappings) {
        this(TimeUnit.SECONDS.toMillis(5), attributesMappings);
    }

    @Override
    protected boolean checkProfilingRule(ReadWriteSpan span) {
        for (Map.Entry<String, String> entry : attributesMappings.entrySet()) {
            if (!span.getAttribute(AttributeKey.stringKey(entry.getKey())).equals(entry.getValue())) {
                return false;
            }
        }
        LOGGER.info("start profiling, matched rule: " + this.getClass().getName());
        return true;
    }

}
