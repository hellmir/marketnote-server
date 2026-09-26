package com.personal.marketnote.user.security.token.introspector;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class VendorIdTokenVerifier {
    private final Map<AuthVendor, VendorJwtConfig> vendorConfigMap;
    private final ConcurrentHashMap<AuthVendor, JwtDecoder> decoderCache = new ConcurrentHashMap<>();

    public VendorIdTokenVerifier(Map<AuthVendor, VendorJwtConfig> vendorConfigMap) {
        this.vendorConfigMap = vendorConfigMap;
    }

    public String verifyAndExtractSubject(String token, AuthVendor vendor) {
        VendorJwtConfig config = vendorConfigMap.get(vendor);
        if (FormatValidator.hasNoValue(config)) {
            throw new JwtException("벤더 JWT 설정이 존재하지 않습니다: " + vendor);
        }

        JwtDecoder decoder = decoderCache.computeIfAbsent(vendor, v -> buildDecoder(config));
        Jwt jwt = decoder.decode(token);

        String subject = jwt.getSubject();
        if (FormatValidator.hasNoValue(subject)) {
            throw new JwtException("JWT에 sub claim이 존재하지 않습니다: vendor=" + vendor);
        }
        return subject;
    }

    public Map<AuthVendor, List<String>> getVendorIssuerMap() {
        return vendorConfigMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().issuers()));
    }

    private JwtDecoder buildDecoder(VendorJwtConfig config) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(config.jwksUri())
                .jwsAlgorithms(algorithms -> {
                    algorithms.add(SignatureAlgorithm.RS256);
                    algorithms.add(SignatureAlgorithm.ES256);
                })
                .build();

        OAuth2TokenValidator<Jwt> issuerValidator = new JwtClaimValidator<>(
                JwtClaimNames.ISS,
                issuer -> config.issuers().contains(String.valueOf(issuer))
        );

        OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<>(
                JwtClaimNames.AUD,
                audience -> {
                    if (audience == null) {
                        return false;
                    }
                    if (audience instanceof List<?> audList) {
                        return audList.contains(config.audience());
                    }
                    return config.audience().equals(String.valueOf(audience));
                }
        );

        OAuth2TokenValidator<Jwt> expirationValidator = new JwtTimestampValidator();

        DelegatingOAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                issuerValidator, audienceValidator, expirationValidator
        );
        decoder.setJwtValidator(validator);

        return decoder;
    }
}
