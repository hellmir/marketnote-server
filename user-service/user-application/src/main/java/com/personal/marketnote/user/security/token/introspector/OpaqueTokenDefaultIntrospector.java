package com.personal.marketnote.user.security.token.introspector;

import com.personal.marketnote.common.domain.exception.token.InvalidAccessTokenException;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.constant.PrimaryRole;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.port.out.user.FindUserPort;
import com.personal.marketnote.user.security.token.dto.OAuth2AuthenticationInfo;
import com.personal.marketnote.user.security.token.support.TokenSupport;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.personal.marketnote.common.security.token.utility.TokenConstant.ISS_CLAIM_KEY;
import static com.personal.marketnote.common.security.token.utility.TokenConstant.SUB_CLAIM_KEY;

@RequiredArgsConstructor
@Slf4j
public class OpaqueTokenDefaultIntrospector implements OpaqueTokenIntrospector {
    private static final Set<String> ASYMMETRIC_ALGORITHMS = Set.of("RS256", "ES256");

    private final TokenSupport tokenSupport;
    private final FindUserPort findUserPort;
    private final Map<AuthVendor, List<String>> vendorIssuerMap;
    private final VendorIdTokenVerifier vendorIdTokenVerifier;

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        try {
            AuthVendor resolvedVendor = resolveVendorFromIdToken(token);
            if (FormatValidator.hasValue(resolvedVendor)) {
                return parseVendorIdToken(token, resolvedVendor);
            }

            OAuth2AuthenticationInfo userInfo = tokenSupport.authenticate(token);
            String oidcId = userInfo.id();
            AuthVendor authVendor = userInfo.authVendor();
            String issuer = authVendor.name();
            User user = findUserPort.findAllStatusUserByAuthVendorAndOidcId(authVendor, oidcId)
                    .orElseGet(() -> findUserPort.findAllStatusUserById(userInfo.userId()).orElse(null));

            return resolvePrincipal(user, oidcId, issuer);
        } catch (InvalidAccessTokenException e) {
            return buildAnonymousPrincipal("", AuthVendor.NATIVE.name());
        }
    }

    private AuthVendor resolveVendorFromIdToken(String token) {
        if (FormatValidator.hasNoValue(token)) {
            return null;
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return null;
        }

        try {
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            JSONObject header = new JSONObject(headerJson);
            JSONObject payload = new JSONObject(payloadJson);

            boolean isJwt = "JWT".equalsIgnoreCase(header.optString("typ", "JWT"));
            // pre-filter only: actual algorithm enforcement is done by NimbusJwtDecoder in VendorIdTokenVerifier
            String alg = header.optString("alg", "").toUpperCase();
            boolean isAsymmetricAlg = ASYMMETRIC_ALGORITHMS.contains(alg);

            if (!isJwt || !isAsymmetricAlg) {
                return null;
            }

            String iss = payload.optString(ISS_CLAIM_KEY, "");

            for (Map.Entry<AuthVendor, List<String>> entry : vendorIssuerMap.entrySet()) {
                for (String issuer : entry.getValue()) {
                    if (issuer.equals(iss)) {
                        return entry.getKey();
                    }
                }
            }

            return null;
        } catch (IllegalArgumentException e) {
            log.debug("Failed to Base64URL decode token header/payload: {}", e.getMessage());
            return null;
        }
    }

    private OAuth2AuthenticatedPrincipal parseVendorIdToken(String token, AuthVendor vendor) {
        try {
            String oidcId = vendorIdTokenVerifier.verifyAndExtractSubject(token, vendor);
            String issuer = vendor.name();
            User user = findUserPort.findAllStatusUserByAuthVendorAndOidcId(vendor, oidcId).orElse(null);
            return resolvePrincipal(user, oidcId, issuer);
        } catch (JwtException e) {
            log.warn("벤더 ID 토큰 검증 실패: vendor={}, error={}", vendor, e.getMessage());
            return buildAnonymousPrincipal("", vendor.name());
        }
    }

    private OAuth2AuthenticatedPrincipal resolvePrincipal(User user, String oidcId, String issuer) {
        if (FormatValidator.hasNoValue(user)) {
            return buildGuestPrincipal(oidcId, issuer);
        }
        if (!user.isActive()) {
            return buildAnonymousPrincipal(oidcId, issuer);
        }
        return buildUserPrincipal(user, oidcId, issuer);
    }

    private OAuth2AuthenticatedPrincipal buildUserPrincipal(User user, String oidcId, String issuer) {
        return new DefaultOAuth2AuthenticatedPrincipal(
                String.valueOf(user.getId()),
                buildAttributes(oidcId, issuer),
                List.of(new SimpleGrantedAuthority(user.getRole().getId()))
        );
    }

    private OAuth2AuthenticatedPrincipal buildGuestPrincipal(String oidcId, String issuer) {
        return new DefaultOAuth2AuthenticatedPrincipal(
                "-1",
                buildAttributes(oidcId, issuer),
                List.of(new SimpleGrantedAuthority(PrimaryRole.ROLE_GUEST.name()))
        );
    }

    private OAuth2AuthenticatedPrincipal buildAnonymousPrincipal(String oidcId, String issuer) {
        return new DefaultOAuth2AuthenticatedPrincipal(
                "-1",
                buildAttributes(oidcId, issuer),
                List.of(new SimpleGrantedAuthority(PrimaryRole.ROLE_ANONYMOUS.name()))
        );
    }

    private Map<String, Object> buildAttributes(String oidcId, String issuer) {
        return Map.of(
                SUB_CLAIM_KEY, FormatValidator.hasValue(oidcId) ? oidcId : "",
                ISS_CLAIM_KEY, issuer
        );
    }
}
