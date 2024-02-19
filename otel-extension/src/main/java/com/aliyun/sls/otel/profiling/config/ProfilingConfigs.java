package com.aliyun.sls.otel.profiling.config;

import java.util.List;
import java.util.Map;

public class ProfilingConfigs {
    private boolean enabled;
    private int maxProfilingCount;
    private int profilingIntervalMillis;
    private List<ProfilingRule> profilingRules;
    private Map<String, String> agentConfigs;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxProfilingCount() {
        return maxProfilingCount;
    }

    public void setMaxProfilingCount(int maxProfilingCount) {
        this.maxProfilingCount = maxProfilingCount;
    }

    public int getProfilingIntervalMillis() {
        return profilingIntervalMillis;
    }

    public void setProfilingIntervalMillis(int profilingIntervalMillis) {
        this.profilingIntervalMillis = profilingIntervalMillis;
    }

    public List<ProfilingRule> getProfilingRules() {
        return profilingRules;
    }

    public void setProfilingRules(List<ProfilingRule> profilingRules) {
        this.profilingRules = profilingRules;
    }

    public Map<String, String> getAgentConfigs() {
        return agentConfigs;
    }

    public void setAgentConfigs(Map<String, String> agentConfigs) {
        this.agentConfigs = agentConfigs;
    }

    public ProfilingConfigs overrideFromEnvAndSystemProperties() {
        // enable profiling
        if (System.getenv("OTEL_PROFILING_ENABLED") != null) {
            this.enabled = Boolean.parseBoolean(System.getenv("OTEL_PROFILING_ENABLED"));
        }
        if (System.getProperty("otel.profiling.enabled") != null) {
            this.enabled = Boolean.parseBoolean(System.getProperty("otel.profiling.enabled"));
        }

        // max profiling count
        if (System.getenv("OTEL_PROFILING_MAX_PROFILING_COUNT") != null) {
            this.maxProfilingCount = Integer.parseInt(System.getenv("OTEL_PROFILING_MAX_PROFILING_COUNT"));
        }
        if (System.getProperty("otel.profiling.max.profiling.count") != null) {
            this.maxProfilingCount = Integer.parseInt(System.getProperty("otel.profiling.max.profiling.count"));
        }

        // profiling interval millis
        if (System.getenv("OTEL_PROFILING_INTERVAL_MILLIS") != null) {
            this.profilingIntervalMillis = Integer.parseInt(System.getenv("OTEL_PROFILING_INTERVAL_MILLIS"));
        }
        if (System.getProperty("otel.profiling.interval.millis") != null) {
            this.profilingIntervalMillis = Integer.parseInt(System.getProperty("otel.profiling.interval.millis"));
        }

        return this;
    }
}
