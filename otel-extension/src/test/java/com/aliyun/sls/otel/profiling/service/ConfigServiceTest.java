package com.aliyun.sls.otel.profiling.service;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConfigServiceTest {

    private ConfigProperties configProperties;

    @Test
    public void testLoadConfig() {
        Map<String, String> attribute = new HashMap<>();
        attribute.put("otel.profiling.config_endpoint", "classpath:/config.yaml");
        configProperties = DefaultConfigProperties.createForTest(attribute);

        ConfigService.INSTANCE.initProfilingConfiguration(configProperties);
    }
}
