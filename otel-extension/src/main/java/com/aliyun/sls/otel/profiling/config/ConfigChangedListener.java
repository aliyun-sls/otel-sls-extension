package com.aliyun.sls.otel.profiling.config;

/**
 * ConfigChangedListener is the interface for config changed listener.
 */
public interface ConfigChangedListener {

    /**
     * onConfigChanged will be called when config changed.
     *
     * @param config
     */
    void onConfigChanged(ProfilingConfig config);
}
