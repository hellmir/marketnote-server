package com.personal.marketnote.notification.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.notification.adapter.out.vendor.aligo.AligoHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(AligoProperties.class)
public class AligoClientConfig {

    @Bean
    public AligoHttpClient aligoHttpClient(
            RestClient.Builder builder, ObjectMapper objectMapper, AligoProperties properties
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(properties.getTimeout()));
        requestFactory.setReadTimeout(Duration.ofMillis(properties.getTimeout()));

        RestClient restClient = builder
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();

        return new AligoHttpClient(restClient, objectMapper, properties);
    }
}
