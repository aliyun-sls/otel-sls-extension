package com.aliyun.sls.otel.profiling.processor;

import com.aliyun.sls.otel.profiling.service.ProfilingService;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

/**
 * E2EProfilingSpanProcessor is the span processor for profiling. It will try to start profiling
 * when span start and stop profiling when span end.
 */
public class E2EProfilingSpanProcessor implements SpanProcessor {

    private final ProfilingService profilingService;
    private static final AttributeKey<String> PROFILING_ATTRIBUTE = AttributeKey.stringKey("profiling");

    public E2EProfilingSpanProcessor(ProfilingService profilingService) {
        this.profilingService = profilingService;
    }

    @Override
    public void onStart(Context context, ReadWriteSpan readWriteSpan) {
        if (profilingService.tryToProfiling(readWriteSpan)) {
            readWriteSpan.setAttribute(PROFILING_ATTRIBUTE, "true");
        }
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan readableSpan) {
        if (readableSpan.getAttribute(PROFILING_ATTRIBUTE) != null) {
            this.profilingService.stopProfiling(readableSpan);
        }
    }

    @Override
    public boolean isEndRequired() {
        return true;
    }
}
