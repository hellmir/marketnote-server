package com.personal.marketnote.user.adapter.in.configuration.security;

import com.personal.marketnote.user.port.out.user.FindUserPort;
import com.personal.marketnote.user.security.token.introspector.OpaqueTokenDefaultIntrospector;
import com.personal.marketnote.user.security.token.support.TokenSupport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

@Configuration
public class AuthenticationEntryPointConfig {
    @Bean
    @ConditionalOnMissingBean(OpaqueTokenIntrospector.class)
    public OpaqueTokenIntrospector defaultOpaqueTokenIntrospector(TokenSupport tokenSupport, FindUserPort findUserPort) {
        return new OpaqueTokenDefaultIntrospector(tokenSupport, findUserPort);
    }
}
