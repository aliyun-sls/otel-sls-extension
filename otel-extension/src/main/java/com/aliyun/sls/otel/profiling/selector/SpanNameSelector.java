package com.aliyun.sls.otel.profiling.selector;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import io.opentelemetry.sdk.trace.ReadWriteSpan;

public class SpanNameSelector extends ProfilingIntervalLimitSelector {

    private final String spanNamePattern;
    private final Pattern pattern;

    public SpanNameSelector(long intervalMillis, String spanNamePattern) {
        super(intervalMillis);
        this.spanNamePattern = spanNamePattern;
        this.pattern = Pattern.compile(spanNamePattern);
    }

    public SpanNameSelector(String spanNamePattern) {
        this(TimeUnit.SECONDS.toMillis(5), spanNamePattern);
    }

    @Override
    protected boolean checkProfilingRule(ReadWriteSpan span) {
        if (!pattern.matcher(span.getName()).matches()) {
            return false;
        }
        LOGGER.info("start profiling, matched rule: " + this.getClass().getName());
        return true;
    }
}
