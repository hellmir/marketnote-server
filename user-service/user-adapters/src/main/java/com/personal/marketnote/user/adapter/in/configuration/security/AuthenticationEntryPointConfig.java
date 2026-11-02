package com.personal.marketnote.user.adapter.in.configuration.security;

import com.personal.marketnote.user.port.out.user.FindUserPort;
import com.personal.marketnote.user.security.token.introspector.OpaqueTokenDefaultIntrospector;
import com.personal.marketnote.user.security.token.introspector.VendorIdTokenVerifier;
import com.personal.marketnote.user.security.token.introspector.VendorJwtConfig;
import com.personal.marketnote.user.security.token.support.TokenSupport;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class AuthenticationEntryPointConfig {

    @Bean
    public VendorIdTokenVerifier vendorIdTokenVerifier(
            @Value("${oauth2.kakao.allowed-audiences}") List<String> kakaoAllowedAudiences,
            @Value("${oauth2.google.allowed-audiences}") List<String> googleAllowedAudiences,
            @Value("${oauth2.apple.allowed-audiences}") List<String> appleAllowedAudiences
    ) {
        Map<AuthVendor, VendorJwtConfig> configMap = Map.of(
                AuthVendor.KAKAO, new VendorJwtConfig(
                        "https://kauth.kakao.com/.well-known/jwks.json",
                        List.of("kauth.kakao.com", "https://kauth.kakao.com"),
                        sanitize(kakaoAllowedAudiences)
                ),
                AuthVendor.GOOGLE, new VendorJwtConfig(
                        "https://www.googleapis.com/oauth2/v3/certs",
                        List.of("accounts.google.com", "https://accounts.google.com"),
                        sanitize(googleAllowedAudiences)
                ),
                AuthVendor.APPLE, new VendorJwtConfig(
                        "https://appleid.apple.com/auth/keys",
                        List.of("appleid.apple.com", "https://appleid.apple.com"),
                        sanitize(appleAllowedAudiences)
                )
        );
        return new VendorIdTokenVerifier(configMap);
    }

    @Bean
    @ConditionalOnMissingBean(OpaqueTokenIntrospector.class)
    public OpaqueTokenIntrospector defaultOpaqueTokenIntrospector(
            TokenSupport tokenSupport,
            FindUserPort findUserPort,
            VendorIdTokenVerifier vendorIdTokenVerifier
    ) {
        Map<AuthVendor, List<String>> vendorIssuerMap = vendorIdTokenVerifier.getVendorIssuerMap();
        return new OpaqueTokenDefaultIntrospector(
                tokenSupport, findUserPort, vendorIssuerMap, vendorIdTokenVerifier
        );
    }

    private Set<String> sanitize(List<String> rawAudiences) {
        return rawAudiences.stream()
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
