package com.aliyun.sls.otel.profiling.config;

import java.util.Collections;

/**
 * NoopProfilingConfigs is the default ProfilingConfigs.
 */
public class NoopProfilingConfigs extends ProfilingConfigs {

    public static final ProfilingConfigs INSTANCE = new NoopProfilingConfigs();

    private NoopProfilingConfigs() {
        this.setEnabled(false);
        this.setMaxProfilingCount(10);
        this.setProfilingRules(Collections.emptyList());
        this.setAgentConfigs(Collections.emptyMap());
    }
}
