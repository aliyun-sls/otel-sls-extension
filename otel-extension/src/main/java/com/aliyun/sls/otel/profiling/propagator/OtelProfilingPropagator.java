package com.aliyun.sls.otel.profiling.propagator;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

import com.aliyun.sls.otel.profiling.action.AlibabaProfilingAction;
import com.aliyun.sls.otel.profiling.action.ProfilingAction;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import static com.aliyun.sls.otel.profiling.config.Constants.PROFILING_FLAG;

public class OtelProfilingPropagator implements TextMapPropagator {
    // The header key used to propagate the profiling flag.
    private static final String SLS_OTEL_EXTENSION_HEADER = "x-sls-otel-extension";

    private static final Logger logger = Logger.getLogger(OtelProfilingPropagator.class.getName());

    @Override
    public Collection<String> fields() {
        return Collections.singletonList(SLS_OTEL_EXTENSION_HEADER);
    }

    @Override
    public <C> void inject(Context context, C carrier, TextMapSetter<C> setter) {
        SpanContext currentSpanContext = Span.fromContext(context).getSpanContext();
        boolean profilingFlag = AlibabaProfilingAction.INSTANCE.checkIfAlreadyProfiling(context,
                currentSpanContext.getTraceId());

        if (profilingFlag) {
            if (logger.isLoggable(java.util.logging.Level.FINE)) {
                logger.fine("inject profiling flag");
            }

            setter.set(carrier, SLS_OTEL_EXTENSION_HEADER, "01");
        }
    }

    @Override
    public <C> Context extract(Context context, C carrier, TextMapGetter<C> getter) {
        String slsOtelExtension = getter.get(carrier, SLS_OTEL_EXTENSION_HEADER);

        // If the header is present, set the profiling flag to true.
        if (logger.isLoggable(java.util.logging.Level.FINE)) {
            logger.fine("extract profiling flag: " + slsOtelExtension);
        }

        if (slsOtelExtension != null && slsOtelExtension.equals("01")) {
            return context.with(PROFILING_FLAG, true);
        }
        return context;
    }

}
