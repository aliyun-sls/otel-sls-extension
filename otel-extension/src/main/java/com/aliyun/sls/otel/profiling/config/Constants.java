package com.aliyun.sls.otel.profiling.config;

import io.opentelemetry.context.ContextKey;

public class Constants {
    public static final ContextKey<Boolean> PROFILING_FLAG = ContextKey.named("profiling-flag");
}
