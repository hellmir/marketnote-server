package com.personal.marketnote.common.adapter.in.interception;

import com.personal.marketnote.common.utility.PerformanceMeasurer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.personal.marketnote.common.application.aop.LogMessage.PERFORMANCE_MEASUREMENT;

public class LoggingInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(com.personal.marketnote.common.adapter.in.interception.LoggingInterceptor.class);
    private static final String REQUEST_RECEPTION = "Received request for '{}'";
    private static final String REQUEST_COMPLETION
            = "Completed request for '{}', status code: {}, ";
    private static final String BEFORE_MEMORY = "beforeMemory";
    private static final String STARTED_AT = "startedAt";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info(REQUEST_RECEPTION, request.getRequestURI());

        long beforeMemory = PerformanceMeasurer.computeUsedMemory(0);
        long startedAt = PerformanceMeasurer.computeElapsedTime(0);

        request.setAttribute(BEFORE_MEMORY, beforeMemory);
        request.setAttribute(STARTED_AT, startedAt);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) {
        long beforeMemory = (long) request.getAttribute(BEFORE_MEMORY);
        long startedAt = (long) request.getAttribute(STARTED_AT);
        long elapsedTime = PerformanceMeasurer.computeElapsedTime(startedAt);
        long memoryUsage = PerformanceMeasurer.computeUsedMemory(beforeMemory);

        log.info(
                REQUEST_COMPLETION + PERFORMANCE_MEASUREMENT,
                request.getRequestURI(), response.getStatus(), elapsedTime, memoryUsage
        );
    }
}
