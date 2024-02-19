package com.aliyun.sls.otel.profiling.selector;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;

/**
 * ProfilingSelector is the selector for profiling. It will try to start profiling when span start and stop profiling
 * when span end.
 */
public interface ProfilingSelector {
    boolean shouldBeProfiling(Context context, ReadWriteSpan span);

    void stopProfiling(ReadableSpan span);
}
