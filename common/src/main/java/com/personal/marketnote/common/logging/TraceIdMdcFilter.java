package com.personal.marketnote.common.logging;

import com.personal.marketnote.common.utility.FormatValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdMdcFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Request-ID";
    public static final String TRACE_ID_MDC_KEY = "traceId";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    public static final String CAUSATION_ID_HEADER = "X-Causation-ID";
    public static final String CAUSATION_ID_MDC_KEY = "causationId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain
    ) throws ServletException, IOException {
        String traceId = TraceIdValidator.validateOrFallback(request.getHeader(TRACE_ID_HEADER));
        MDC.put(TRACE_ID_MDC_KEY, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);

        String correlationId = resolveCorrelationId(request, traceId);
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        String causationId = resolveCausationId(request);
        if (FormatValidator.hasValue(causationId)) {
            MDC.put(CAUSATION_ID_MDC_KEY, causationId);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_MDC_KEY);
            MDC.remove(CORRELATION_ID_MDC_KEY);
            MDC.remove(CAUSATION_ID_MDC_KEY);
        }
    }

    private static String resolveCorrelationId(HttpServletRequest request, String traceId) {
        String headerValue = request.getHeader(CORRELATION_ID_HEADER);
        if (FormatValidator.hasNoValue(headerValue)) {
            return traceId;
        }
        if (!TraceIdValidator.TRACE_ID_PATTERN.matcher(headerValue).matches()) {
            return traceId;
        }
        return headerValue;
    }

    private static String resolveCausationId(HttpServletRequest request) {
        String headerValue = request.getHeader(CAUSATION_ID_HEADER);
        if (FormatValidator.hasNoValue(headerValue)) {
            return null;
        }
        if (!TraceIdValidator.TRACE_ID_PATTERN.matcher(headerValue).matches()) {
            return null;
        }
        return headerValue;
    }
}
