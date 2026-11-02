package com.personal.marketnote.common.configuration.swagger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerOriginConfig {

    @Bean
    public SwaggerOrigin swaggerOrigin(@Value("${client.origin}") String clientOrigin) {
        return SwaggerOrigin.parse(clientOrigin);
    }
}
