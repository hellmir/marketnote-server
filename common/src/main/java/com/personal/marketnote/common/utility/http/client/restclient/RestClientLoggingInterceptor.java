package com.personal.marketnote.common.utility.http.client.restclient;

import com.personal.marketnote.common.utility.PerformanceMeasurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;

public class RestClientLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RestClientLoggingInterceptor.class);

    private static final String OUTBOUND_REQUEST = "Outbound request: {} {}";
    private static final String OUTBOUND_RESPONSE = "Outbound response: {} {} -> status={}, elapsed={}ms";
    private static final String OUTBOUND_ERROR = "Outbound request failed: {} {} -> error={}, elapsed={}ms";

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        HttpMethod method = request.getMethod();
        URI uri = request.getURI();

        log.info(OUTBOUND_REQUEST, method, uri);

        long startedAt = PerformanceMeasurer.computeElapsedTime(0);

        try {
            ClientHttpResponse response = execution.execute(request, body);
            long elapsedTime = PerformanceMeasurer.computeElapsedTime(startedAt);

            log.info(OUTBOUND_RESPONSE, method, uri, response.getStatusCode().value(), elapsedTime);

            return response;
        } catch (IOException exception) {
            long elapsedTime = PerformanceMeasurer.computeElapsedTime(startedAt);

            log.error(OUTBOUND_ERROR, method, uri, exception.getMessage(), elapsedTime);

            throw exception;
        }
    }
}
