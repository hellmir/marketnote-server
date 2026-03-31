package com.personal.marketnote.user.adapter.in.configuration.security;

import com.personal.marketnote.user.port.out.user.FindUserPort;
import com.personal.marketnote.user.security.token.introspector.OpaqueTokenDefaultIntrospector;
import com.personal.marketnote.user.security.token.support.TokenSupport;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import java.util.List;
import java.util.Map;

@Configuration
public class AuthenticationEntryPointConfig {

    private static final Map<AuthVendor, List<String>> VENDOR_ISSUER_MAP = Map.of(
            AuthVendor.KAKAO, List.of("kauth.kakao.com", "https://kauth.kakao.com"),
            AuthVendor.GOOGLE, List.of("accounts.google.com", "https://accounts.google.com"),
            AuthVendor.APPLE, List.of("appleid.apple.com", "https://appleid.apple.com")
    );

    @Bean
    @ConditionalOnMissingBean(OpaqueTokenIntrospector.class)
    public OpaqueTokenIntrospector defaultOpaqueTokenIntrospector(
            TokenSupport tokenSupport,
            FindUserPort findUserPort
    ) {
        return new OpaqueTokenDefaultIntrospector(tokenSupport, findUserPort, VENDOR_ISSUER_MAP);
    }
}
