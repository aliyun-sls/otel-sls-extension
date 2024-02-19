package com.aliyun.sls.otel.profiling;

import com.aliyun.sls.otel.profiling.propagator.OtelProfilingPropagator;
import com.google.auto.service.AutoService;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurablePropagatorProvider;

@AutoService(ConfigurablePropagatorProvider.class)
public class E2EProfilingPropagatorProvider implements ConfigurablePropagatorProvider {

    @Override
    public TextMapPropagator getPropagator(ConfigProperties config) {
        return new OtelProfilingPropagator();
    }

    @Override
    public String getName() {
        return "sls-otel-extension";
    }
}
