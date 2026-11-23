package com.personal.marketnote.common.utility.http.client.restclient;

import com.personal.marketnote.common.logging.TraceIdMdcFilter;
import com.personal.marketnote.common.utility.FormatValidator;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class TraceIdPropagationInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        String traceId = MDC.get(TraceIdMdcFilter.TRACE_ID_MDC_KEY);

        if (FormatValidator.hasNoValue(traceId)) {
            return execution.execute(request, body);
        }
        if (request.getHeaders().containsKey(TraceIdMdcFilter.TRACE_ID_HEADER)) {
            return execution.execute(request, body);
        }

        request.getHeaders().add(TraceIdMdcFilter.TRACE_ID_HEADER, traceId);
        return execution.execute(request, body);
    }
}
