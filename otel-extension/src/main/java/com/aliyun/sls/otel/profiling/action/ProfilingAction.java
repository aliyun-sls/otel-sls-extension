package com.aliyun.sls.otel.profiling.action;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;

/**
 * ProfilingAction is the interface for profiling action.
 */
public interface ProfilingAction {

    /**
     * Start profiling.
     *
     * @param readWriteSpan
     */
    void startProfiling(ReadWriteSpan readWriteSpan);

    /**
     * Stop profiling.
     *
     * @param readableSpan
     */
    void stopProfiling(ReadableSpan readableSpan);

    /**
     * prepare profiling for an trace.
     *
     * @param traceID
     */
    void addProfilingTraceId(String traceID);

    /**
     * return the size of profiling trace.
     *
     * @return
     */
    int currentProfilingTraceSize();

    /**
     * check if the traceId is already profiling.
     *
     * @param context
     *
     * @param traceId
     *
     * @return
     */
    boolean checkIfAlreadyProfiling(Context context, String traceId);

    /**
     * clean timeout profiling trace.
     *
     * @param timeout
     */
    void cleanTimeoutProfilingTrace(long timeout);
}
