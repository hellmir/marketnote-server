package com.personal.marketnote.common.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

@Configuration
public class OpaqueTokenIntrospectorProvider {
    @Bean
    @ConditionalOnMissingBean(OpaqueTokenIntrospector.class)
    public OpaqueTokenIntrospector defaultOpaqueTokenIntrospector(
            ObjectMapper objectMapper,
            @Value("${spring.jwt.secret}") String jwtSecret) {
        return new com.personal.marketnote.common.security.introspection.HmacJwtOpaqueTokenIntrospector(objectMapper,
                jwtSecret);
    }
}
