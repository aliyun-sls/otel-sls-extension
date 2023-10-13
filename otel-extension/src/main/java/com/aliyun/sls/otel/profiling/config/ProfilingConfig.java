package com.aliyun.sls.otel.profiling.config;

import java.util.List;
import java.util.Map;

/**
 * ProfilingConfig is the interface for profiling config.
 */
public interface ProfilingConfig {

    int getMaxProfilingCount();

    String getResourceConfig(String key);

    long getProfilingIntervalMillis();

    List<ProfilingRule> getProfilingRules();

    String getServiceName();

    boolean profilingEnabled();

    Map<String, String> getAgentConfig();
}
