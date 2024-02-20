package com.aliyun.sls.otel.profiling.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.aliyun.sls.otel.profiling.config.ConfigChangedListener;
import com.aliyun.sls.otel.profiling.config.NoopProfilingConfigs;
import com.aliyun.sls.otel.profiling.config.ProfilingConfig;
import com.aliyun.sls.otel.profiling.config.ProfilingConfigs;
import com.aliyun.sls.otel.profiling.config.ProfilingRule;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;

public enum ConfigService implements ProfilingConfig {

    INSTANCE;

    private ProfilingConfigs profilingConfigs;
    private Map<String, String> resourceAttribute;
    private volatile ScheduledFuture<?> scheduledFuture;
    private static final Logger LOGGER = Logger.getLogger(ConfigService.class.getName());
    private final LinkedBlockingDeque<ConfigChangedListener> configChangedListeners = new LinkedBlockingDeque<>();

    private String ip;
    private String hostname;

    ConfigService() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            hostname = inetAddress.getHostName();
            ip = inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            hostname = "";
            ip = "";
        }
    }

    private ProfilingConfigs loadConfigFromClassPath(String fileName) throws IOException {
        InputStream configFile = ConfigService.class.getResourceAsStream(fileName);
        if (configFile == null) {
            throw new IOException("Config file not found: " + fileName);
        }

        ObjectMapper mapper = YAMLMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS).build();
        return mapper.readValue(configFile, ProfilingConfigs.class);
    }

    public ProfilingConfigs loadConfigFromFile(String fileName) throws IOException {
        File configFile = new File(fileName);
        if (!configFile.exists()) {
            throw new IOException("Config file not found: " + fileName);
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(configFile, ProfilingConfigs.class);
    }

    public ProfilingConfigs loadConfigFromUrl(String url) throws IOException {
        URL configUrl = new URL(url);
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(configUrl, ProfilingConfigs.class);
    }

    private void notifyConfigChangedListeners() {
        for (ConfigChangedListener listener : configChangedListeners) {
            listener.onConfigChanged(ConfigService.INSTANCE);
        }
    }

    public void initProfilingConfiguration(ConfigProperties configProperties) {
        resourceAttribute = initResourceAttribute(configProperties);

        String configFile = configProperties.getString("otel.profiling.config_endpoint");
        LOGGER.info("agent config file: " + configFile);
        if (configFile != null && !configFile.isEmpty()) {
            profilingConfigs = reloadConfig(configFile);
            if (scheduledFuture == null) {
                scheduledFuture = ScheduleTaskService.INSTANCE.submitJob(() -> {
                    try {
                        profilingConfigs = reloadConfig(configFile);
                        profilingConfigs = profilingConfigs.overrideFromEnvAndSystemProperties();
                        notifyConfigChangedListeners();
                    } catch (Exception e) {
                        LOGGER.warning("reload config failed: " + e.getMessage());
                    }
                }, 10, 10, TimeUnit.MINUTES);
            }
        } else {
            LOGGER.info("config file is empty, use default config");
            profilingConfigs = NoopProfilingConfigs.INSTANCE;
            LOGGER.info("get default config: " + profilingConfigs.isEnabled());
        }

        profilingConfigs = profilingConfigs.overrideFromEnvAndSystemProperties();
        notifyConfigChangedListeners();
    }

    private ProfilingConfigs reloadConfig(String configFile) {
        LOGGER.info("reload config from: " + configFile);
        try {
            if (configFile.startsWith("file:/")) {
                return loadConfigFromFile(configFile.substring("file:".length()));
            }

            if (configFile.startsWith("http://") || configFile.startsWith("https://")) {
                return loadConfigFromUrl(configFile);
            }

            if (configFile.startsWith("classpath:/")) {
                return loadConfigFromClassPath(configFile.substring("classpath:".length()));
            }
        } catch (Exception e) {
            LOGGER.warning("reload config failed: " + e.getMessage());
        }

        return NoopProfilingConfigs.INSTANCE;
    }

    private Map<String, String> initResourceAttribute(ConfigProperties configProperties) {
        Map<String, String> resourceAttributes = configProperties.getMap("otel.resource.attributes", new HashMap<>());

        String serviceName = configProperties.getString("otel.service.name");
        if (serviceName != null && !serviceName.isEmpty()) {
            resourceAttributes.put("service.name", serviceName);
        }

        LOGGER.info("get resource attributes: " + resourceAttributes);
        return resourceAttributes;
    }

    @Override
    public int getMaxProfilingCount() {
        return profilingConfigs.getMaxProfilingCount();
    }

    @Override
    public String getResourceConfig(String key) {
        return resourceAttribute.getOrDefault(key, "");
    }

    @Override
    public long getProfilingIntervalMillis() {
        return profilingConfigs.getProfilingIntervalMillis();
    }

    @Override
    public List<ProfilingRule> getProfilingRules() {
        return profilingConfigs.getProfilingRules();
    }

    @Override
    public String getServiceName() {
        return resourceAttribute.getOrDefault("service.name", "UnknownService:(Java)");
    }

    @Override
    public boolean profilingEnabled() {
        return profilingConfigs.isEnabled();
    }

    @Override
    public Map<String, String> getAgentConfig() {
        Map<String, String> agentConfig = new HashMap<>();
        for (Map.Entry<String, String> entry : profilingConfigs.getAgentConfigs().entrySet()) {
            agentConfig.put(String.format("profiling.%s", entry.getKey()), entry.getValue());
        }

        // set default upload server
        if (!profilingConfigs.getAgentConfigs().containsKey("agent.upload.server")) {
            agentConfig.put("profiling.agent.upload.server", "http://logtail-statefulset.kube-system:4040");
        }

        agentConfig.put("profiling.agent.spy.name", "java");
        agentConfig.put("profiling.app.name", getServiceName());
        agentConfig.put("profiling.app.labels", "hostname=" + hostname + ";ip=" + ip);

        // set default profiling engine
        agentConfig.put("profiling.cpu.engine", "auto");
        agentConfig.put("profiling.wallclock.engine", "auto");
        agentConfig.put("profiling.alloc.engine", "auto");

        return agentConfig;
    }

    public void registryListener(ConfigChangedListener profilingService) {
        configChangedListeners.add(profilingService);
    }
}
