package com.aliyun.sls.otel.profiling.selector;

import io.opentelemetry.sdk.trace.ReadWriteSpan;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Profiling selector with count limit.
 */
public class ProfilingCountLimitSelector extends AbstractProfilingSelector {

    private final int maxLimit;
    private final ReentrantLock lock = new ReentrantLock();

    public ProfilingCountLimitSelector(int maxLimit) {
        this.maxLimit = maxLimit;
    }


    @Override
    public boolean shouldBeProfiling(ReadWriteSpan span) {
        // Check if need to profiling another thread of the same trace.
        if (checkIfAlreadyProfiling(span.getSpanContext().getTraceId())) {
            LOGGER.info(span.getSpanContext().getTraceId() + " is already profiling, continue to profiling");
            profilingAction.startProfiling(span);
            return true;
        }


        boolean shouldBeProfiling = profilingSelectors.isEmpty() ? true : false;
        for (ProfilingSelector profilingSelector : profilingSelectors) {
            if (profilingSelector.shouldBeProfiling(span)) {
                shouldBeProfiling = true;
                break;
            }
        }

        if (shouldBeProfiling && checkIfNeedProfiling(span)) {
            LOGGER.info("start profiling, matched rule: " + this.getClass().getName());
            if (profilingAction != null) {
                profilingAction.startProfiling(span);
                return true;
            } else {
                LOGGER.warning("profilingAction is null, skip profiling");
            }
        }

        return false;
    }

    public boolean checkIfNeedProfiling(ReadWriteSpan readWriteSpan) {
        // if not profiling, try to start profiling.
        if (profilingAction.currentProfilingTraceSize() < maxLimit && lock.tryLock()) {
            try {
                profilingAction.addProfilingTraceId(readWriteSpan.getSpanContext().getTraceId());
                return true;
            } finally {
                lock.unlock();
            }
        } else {
            LOGGER.info("profiling count is out of limit, skip profiling");
        }

        return false;
    }
}
