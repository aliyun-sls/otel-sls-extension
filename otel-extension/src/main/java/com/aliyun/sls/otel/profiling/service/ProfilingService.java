package com.aliyun.sls.otel.profiling.service;

import com.alibaba.cpc.Agent;
import com.aliyun.sls.otel.profiling.config.ConfigChangedListener;
import com.aliyun.sls.otel.profiling.config.ProfilingConfig;
import com.aliyun.sls.otel.profiling.selector.ProfilingSelector;
import com.aliyun.sls.otel.profiling.selector.SelectorFactory;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * ProfilingService is the main class for profiling, it provides the profiling.
 */
public enum ProfilingService implements ConfigChangedListener {

    INSTANCE;

    private boolean enableProfiling = false;
    private final Logger logger = Logger.getLogger(ProfilingService.class.getName());
    private final AtomicReference<ProfilingSelector> profilingSelector = new AtomicReference<>(null);

    ProfilingService() {
        ConfigService.INSTANCE.registryListener(this);
    }

    public boolean tryToProfiling(ReadWriteSpan readWriteSpan) {
        if (!enableProfiling || this.profilingSelector.get() == null) {
            return false;
        }

        return this.profilingSelector.get().shouldBeProfiling(readWriteSpan);
    }

    public void stopProfiling(ReadableSpan span) {
        if (!enableProfiling || this.profilingSelector.get() == null) {
            return;
        }

        this.profilingSelector.get().stopProfiling(span);
    }

    @Override
    public void onConfigChanged(ProfilingConfig config) {
        if (enableProfiling != config.profilingEnabled()) {
            enableProfiling = config.profilingEnabled();
            logger.info("profiling enabled: " + enableProfiling);

            if (enableProfiling) {
                logger.info("start profiling agent");
                Agent.start(config.getAgentConfig());
            } else {
                logger.info("stop profiling agent");
                Agent.stop();
            }
        }

        ProfilingSelector profilingSelector = SelectorFactory.INSTANCE.buildProfilingSelector(config);
        this.profilingSelector.set(profilingSelector);
    }
}
