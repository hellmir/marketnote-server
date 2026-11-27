package com.personal.marketnote.notification.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.notification.adapter.out.vendor.aligo.AligoHttpClient;
import com.personal.marketnote.notification.adapter.out.vendor.aligo.AligoOperationTargetTypeResolver;
import com.personal.marketnote.notification.adapter.utility.AligoVendorCommunicationFailureHandler;
import com.personal.marketnote.notification.adapter.utility.AligoVendorCommunicationRecorder;
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
        RestClient.Builder builder, ObjectMapper objectMapper, AligoProperties properties,
        ExternalCallLogger externalCallLogger,
        AligoVendorCommunicationRecorder vendorCommunicationRecorder,
        AligoVendorCommunicationFailureHandler vendorCommunicationFailureHandler,
        VendorCommunicationPayloadGenerator vendorCommunicationPayloadGenerator,
        AligoOperationTargetTypeResolver operationTargetTypeResolver
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(properties.getTimeout()));
        requestFactory.setReadTimeout(Duration.ofMillis(properties.getTimeout()));

        RestClient restClient = builder
            .baseUrl(properties.getBaseUrl())
            .requestFactory(requestFactory)
            .build();

        return new AligoHttpClient(
            restClient, objectMapper, properties, externalCallLogger,
            vendorCommunicationRecorder, vendorCommunicationFailureHandler,
            vendorCommunicationPayloadGenerator, operationTargetTypeResolver
        );
    }
}
