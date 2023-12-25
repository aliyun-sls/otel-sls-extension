package com.aliyun.sls.otel.profiling.selector;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import com.aliyun.sls.otel.profiling.config.ProfilingConfig;

import io.opentelemetry.sdk.trace.ReadWriteSpan;

/**
 * AgentResourceSelector is the selector for agent resource.
 */
public class AgentResourceSelector extends AbstractProfilingSelector {

    private boolean enabled = true;

    public AgentResourceSelector(Map<String, String> resource, ProfilingConfig profilingConfig) {
        for (Entry<String, String> entrySet : resource.entrySet()) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("AgentResourceSelector: " + entrySet.getKey() + " : " + entrySet.getValue() + " : "
                        + profilingConfig.getResourceConfig(entrySet.getKey()));
            }
            enabled = enabled
                    && entrySet.getValue().equalsIgnoreCase(profilingConfig.getResourceConfig(entrySet.getKey()));
        }
    }

    @Override
    protected boolean checkIfNeedProfiling(ReadWriteSpan readWriteSpan) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("AgentResourceSelector: checkIfNeedProfiling: " + enabled);
        }
        return enabled;
    }

}
