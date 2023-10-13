package com.aliyun.sls.otel.profiling.selector;

import io.opentelemetry.sdk.trace.ReadWriteSpan;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Profiling selector with interval limit.
 */
public abstract class ProfilingIntervalLimitSelector extends AbstractProfilingSelector {

    /**
     * Interval limit in milliseconds.
     */
    protected final long intervalMillis;
    /**
     * Last profiling time.
     */
    protected final AtomicLong lastProfilingTime = new AtomicLong(0);

    public ProfilingIntervalLimitSelector(long intervalMillis) {
        this.intervalMillis = intervalMillis;
    }

    public boolean checkIfNeedProfiling(ReadWriteSpan span) {
        long now = System.currentTimeMillis();
        long last = lastProfilingTime.get();

        if ((now - last > intervalMillis) && checkProfilingRule(span)) {
            if (lastProfilingTime.compareAndSet(last, now)) {
                lastProfilingTime.set(last);
                return true;
            }
        }

        return false;
    }

    protected abstract boolean checkProfilingRule(ReadWriteSpan span);
}
