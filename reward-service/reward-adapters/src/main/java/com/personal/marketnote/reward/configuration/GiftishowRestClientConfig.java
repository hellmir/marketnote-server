package com.personal.marketnote.reward.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GiftishowRestClientConfig {

    @Bean
    public RestClient giftishowRestClient(
            RestClient.Builder restClientBuilder,
            GiftishowApiProperties giftishowApiProperties
    ) {
        return restClientBuilder.clone()
                .baseUrl(giftishowApiProperties.getBaseUrl())
                .build();
    }
}
