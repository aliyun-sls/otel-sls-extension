package com.aliyun.sls.otel.profiling.selector;

import com.aliyun.sls.otel.profiling.config.ProfilingConfig;
import com.aliyun.sls.otel.profiling.config.ProfilingRule;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Selector factory
 */
public enum SelectorFactory {
    INSTANCE;

    private static final Logger LOGGER = Logger.getLogger(SelectorFactory.class.getName());

    /**
     * Build profiling selector
     *
     * @param profilingConfig
     *            profiling config
     *
     * @return profiling selector
     */
    public ProfilingSelector buildProfilingSelector(ProfilingConfig profilingConfig) {
        ProfilingCountLimitSelector profilingCountLimitSelector = new ProfilingCountLimitSelector(
            profilingConfig.getMaxProfilingCount());

        if (profilingConfig.getProfilingRules() != null && !profilingConfig.getProfilingRules().isEmpty()) {
            Map<String, ProfilingSelector> profilingSelectorMap = new HashMap<>();
            for (ProfilingRule profilingRule : profilingConfig.getProfilingRules()) {
                profilingSelectorMap.put(profilingRule.getName(), generateSelector(profilingRule, profilingConfig));
            }
            profilingCountLimitSelector.addProfilingSelector(profilingSelectorMap.values().toArray(new ProfilingSelector[0]));
        } else {
            LOGGER.warning("No profiling rules, use default profiling selector");
            profilingCountLimitSelector.addProfilingSelector(defaultProfilingSelector());
        }

        return profilingCountLimitSelector;
    }

    public ProfilingSelector defaultProfilingSelector() {
        return new RootSpanSelector(1000);
    }

    /**
     * Generate selector
     *
     * @param profilingRule
     * @param profilingConfig
     *
     * @return
     */
    public ProfilingSelector generateSelector(ProfilingRule profilingRule, ProfilingConfig profilingConfig) {
        switch (profilingRule.getType()) {
        case "ROOT_SPAN":
            return new RootSpanSelector(profilingConfig.getProfilingIntervalMillis());
        case "AGENT_RESOURCE":
            return new AgentResourceSelector(profilingRule.getAttributes(), profilingConfig);
        case "SPAN_ATTRIBUTE":
            return new SpanAttributeSelector(profilingRule.getAttributes());
        case "SPAN_NAME":
            return new SpanNameSelector(profilingRule.getAttributes().getOrDefault("pattern", ""));
        default:
            throw new IllegalArgumentException("Illegal profiling rule type[" + profilingRule.getType() + "]");
        }
    }
}
