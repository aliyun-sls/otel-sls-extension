package com.aliyun.sls.otel.profiling.selector;

import com.aliyun.sls.otel.profiling.action.AlibabaProfilingAction;
import com.aliyun.sls.otel.profiling.action.ProfilingAction;
import com.aliyun.sls.otel.profiling.service.ProfilingService;
import com.aliyun.sls.otel.profiling.service.ScheduleTaskService;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * AbstractProfilingSelector is the abstract class for profiling selector, it provides the profiling selector.
 */
public abstract class AbstractProfilingSelector implements ProfilingSelector {
    protected static final Logger LOGGER = Logger.getLogger(ProfilingService.class.getName());

    protected final ProfilingAction profilingAction;
    protected final List<ProfilingSelector> profilingSelectors = new ArrayList<>();

    protected AbstractProfilingSelector(ProfilingAction profilingAction) {
        this.profilingAction = profilingAction;
        ScheduleTaskService.INSTANCE.submitJob(
                () -> profilingAction.cleanTimeoutProfilingTrace(TimeUnit.MINUTES.toMillis(2)), 2, 2,
                java.util.concurrent.TimeUnit.MINUTES);
    }

    public AbstractProfilingSelector() {
        this(AlibabaProfilingAction.INSTANCE);
    }

    /**
     * check if already profiling
     *
     * @param traceId
     *
     * @return
     */
    protected boolean checkIfAlreadyProfiling(String traceId) {
        return profilingAction.checkIfAlreadyProfiling(traceId);
    }

    /**
     * check if need profiling, if need, start profiling
     */
    public boolean shouldBeProfiling(ReadWriteSpan span) {
        if (checkIfNeedProfiling(span)) {
            LOGGER.info("start profiling, matched rule: " + this.getClass().getName());
            if (profilingAction != null) {
                profilingAction.startProfiling(span);
                return true;
            } else {
                LOGGER.warning("profilingAction is null, skip profiling");
            }
        }

        if (profilingSelectors.isEmpty()) {
            return false;
        }

        for (ProfilingSelector profilingSelector : profilingSelectors) {
            if (profilingSelector.shouldBeProfiling(span)) {
                return true;
            }
        }
        return false;
    }

    /**
     * stop profiling
     */
    @Override
    public void stopProfiling(ReadableSpan span) {
        profilingAction.stopProfiling(span);
    }

    /**
     * add profiling selector
     *
     * @param profilingSelector
     */
    public void addProfilingSelector(ProfilingSelector... profilingSelector) {
        if (profilingSelector.length <= 0) {
            return;
        }

        profilingSelectors.addAll(Arrays.asList(profilingSelector));
    }

    /**
     * add profiling selector
     *
     * @param profilingSelector
     */
    public void addProfilingSelector(ProfilingSelector profilingSelector) {
        profilingSelectors.add(profilingSelector);
    }

    /**
     * check if need profiling
     *
     * @param readWriteSpan
     *
     * @return
     */
    protected abstract boolean checkIfNeedProfiling(ReadWriteSpan readWriteSpan);
}
