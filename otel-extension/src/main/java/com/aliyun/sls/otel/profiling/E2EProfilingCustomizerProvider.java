package com.aliyun.sls.otel.profiling;

import com.aliyun.sls.otel.profiling.processor.E2EProfilingSpanProcessor;
import com.aliyun.sls.otel.profiling.service.ConfigService;
import com.aliyun.sls.otel.profiling.service.ProfilingService;
import com.google.auto.service.AutoService;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;

/**
 * This class is used to customize the
 * {@link io.opentelemetry.sdk.autoconfigure.spi.SdkTracerProviderConfigurer} by
 * adding a {@link E2EProfilingSpanProcessor} to it.
 */
@AutoService(AutoConfigurationCustomizerProvider.class)
public class E2EProfilingCustomizerProvider implements AutoConfigurationCustomizerProvider {

    @Override
    public void customize(AutoConfigurationCustomizer autoConfiguration) {
        autoConfiguration.addTracerProviderCustomizer((sdkTracerProviderBuilder, configProperties) -> {
            E2EProfilingSpanProcessor profilingSpanProcessor = new E2EProfilingSpanProcessor(ProfilingService.INSTANCE);
            ConfigService.INSTANCE.initProfilingConfiguration(configProperties);
            return sdkTracerProviderBuilder.addSpanProcessor(profilingSpanProcessor);
        });
    }
}
