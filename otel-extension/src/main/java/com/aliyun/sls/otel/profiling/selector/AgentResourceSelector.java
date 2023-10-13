package com.aliyun.sls.otel.profiling.selector;

import java.util.Map;
import java.util.Map.Entry;

import com.aliyun.sls.otel.profiling.config.ProfilingConfig;

import io.opentelemetry.sdk.trace.ReadWriteSpan;

/**
 * AgentResourceSelector is the selector for agent resource.
 */
public class AgentResourceSelector extends AbstractProfilingSelector {

    private boolean enabled = true;

    public AgentResourceSelector(Map<String, String> resource, ProfilingConfig profilingConfig) {
        for (Entry<String, String> entrySet : resource.entrySet()) {
            LOGGER.info("AgentResourceSelector: " + entrySet.getKey() + " : " + entrySet.getValue() + " : " + profilingConfig.getResourceConfig(entrySet.getKey()));
            enabled = enabled && entrySet.getValue().equalsIgnoreCase(profilingConfig.getResourceConfig(entrySet.getKey()));
        }
    }

    @Override
    protected boolean checkIfNeedProfiling(ReadWriteSpan readWriteSpan) {
        LOGGER.info("AgentResourceSelector: checkIfNeedProfiling: " + enabled);
        return enabled;
    }

}
