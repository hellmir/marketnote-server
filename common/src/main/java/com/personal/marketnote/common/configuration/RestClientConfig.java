package com.personal.marketnote.common.configuration;

import com.personal.marketnote.common.utility.http.client.restclient.RestClientErrorHandler;
import com.personal.marketnote.common.utility.http.client.restclient.RestClientLoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    private final int connectTimeout;
    private final int readTimeout;

    public RestClientConfig(
            @Value("${rest-client.connect-timeout:5000}") int connectTimeout,
            @Value("${rest-client.read-timeout:10000}") int readTimeout
    ) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    @Bean
    public RestClientLoggingInterceptor restClientLoggingInterceptor() {
        return new RestClientLoggingInterceptor();
    }

    @Bean
    public RestClientCustomizer restClientCustomizer(RestClientLoggingInterceptor loggingInterceptor) {
        return builder -> {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeout));
            requestFactory.setReadTimeout(Duration.ofMillis(readTimeout));

            builder.requestFactory(requestFactory)
                    .requestInterceptor(loggingInterceptor)
                    .defaultStatusHandler(RestClientErrorHandler::isError, RestClientErrorHandler.noOpErrorHandler());
        };
    }
}
