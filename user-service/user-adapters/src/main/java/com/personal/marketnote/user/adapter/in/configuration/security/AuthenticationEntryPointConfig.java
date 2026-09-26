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

import java.util.List;
import java.util.Map;

@Configuration
public class AuthenticationEntryPointConfig {

    @Bean
    public VendorIdTokenVerifier vendorIdTokenVerifier(
            @Value("${oauth2.kakao.client-id}") String kakaoClientId,
            @Value("${oauth2.google.client-id}") String googleClientId,
            @Value("${oauth2.apple.audience}") String appleAudience
    ) {
        Map<AuthVendor, VendorJwtConfig> configMap = Map.of(
                AuthVendor.KAKAO, new VendorJwtConfig(
                        "https://kauth.kakao.com/.well-known/jwks.json",
                        List.of("kauth.kakao.com", "https://kauth.kakao.com"),
                        kakaoClientId
                ),
                AuthVendor.GOOGLE, new VendorJwtConfig(
                        "https://www.googleapis.com/oauth2/v3/certs",
                        List.of("accounts.google.com", "https://accounts.google.com"),
                        googleClientId
                ),
                AuthVendor.APPLE, new VendorJwtConfig(
                        "https://appleid.apple.com/auth/keys",
                        List.of("appleid.apple.com", "https://appleid.apple.com"),
                        appleAudience
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
}
