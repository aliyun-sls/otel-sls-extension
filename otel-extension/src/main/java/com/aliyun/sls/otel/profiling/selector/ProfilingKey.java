package com.aliyun.sls.otel.profiling.selector;

import java.util.Objects;

/**
 * ProfilingKey is used to identify a profiling task. It is used as the key of the profiling task map. The key is
 * composed of traceID and profilingStartTimeMillis. The traceID is used to identify a trace. The traceID is used to
 * identify a profiling task. The profilingStartTimeMillis is the start time of the profiling task.
 */
public class ProfilingKey implements Comparable<ProfilingKey> {
    private final String traceID;
    private final long profilingStartTimeMillis;

    private ProfilingKey(String traceID) {
        this.traceID = traceID;
        this.profilingStartTimeMillis = System.currentTimeMillis();
    }

    public static ProfilingKey newKey(String traceID) {
        return new ProfilingKey(traceID);
    }

    public String getTraceID() {
        return traceID;
    }

    public long getProfilingStartTimeMillis() {
        return profilingStartTimeMillis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ProfilingKey that = (ProfilingKey) o;
        return Objects.equals(traceID, that.traceID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(traceID);
    }

    @Override
    public int compareTo(ProfilingKey o) {
        return this.hashCode() - o.hashCode();
    }
}
