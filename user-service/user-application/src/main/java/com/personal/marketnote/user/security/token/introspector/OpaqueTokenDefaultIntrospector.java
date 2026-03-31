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
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.personal.marketnote.common.security.token.utility.TokenConstant.ISS_CLAIM_KEY;
import static com.personal.marketnote.common.security.token.utility.TokenConstant.SUB_CLAIM_KEY;

@RequiredArgsConstructor
@Slf4j
public class OpaqueTokenDefaultIntrospector implements OpaqueTokenIntrospector {
    private final TokenSupport tokenSupport;
    private final FindUserPort findUserPort;
    private final Map<AuthVendor, List<String>> vendorIssuerMap;

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        try {
            AuthVendor resolvedVendor = resolveVendorFromIdToken(token);
            if (FormatValidator.hasValue(resolvedVendor)) {
                return parseVendorIdToken(token, resolvedVendor);
            }

            // Default path: our own JWT or opaque token handled by TokenSupport
            OAuth2AuthenticationInfo userInfo = tokenSupport.authenticate(token);
            String oidcId = userInfo.id();
            AuthVendor authVendor = userInfo.authVendor();
            User user = findUserPort.findByAuthVendorAndOidcId(authVendor, oidcId)
                    .orElse(findUserPort.findById(userInfo.userId()).orElse(null));

            if (FormatValidator.hasValue(user)) {
                return new DefaultOAuth2AuthenticatedPrincipal(
                        String.valueOf(user.getId()),
                        Map.of(
                                SUB_CLAIM_KEY, FormatValidator.hasValue(oidcId) ? oidcId : "",
                                ISS_CLAIM_KEY, userInfo.authVendor().name()
                        ),
                        List.of(new SimpleGrantedAuthority(user.getRole().getId()))
                );
            }

            return new DefaultOAuth2AuthenticatedPrincipal(
                    "-1",
                    Map.of(
                            SUB_CLAIM_KEY, FormatValidator.hasValue(oidcId) ? oidcId : "",
                            ISS_CLAIM_KEY, userInfo.authVendor().name()
                    ),
                    List.of(new SimpleGrantedAuthority(PrimaryRole.ROLE_GUEST.name()))
            );
        } catch (InvalidAccessTokenException e) {
            return new DefaultOAuth2AuthenticatedPrincipal(
                    "-1",
                    Map.of(
                            SUB_CLAIM_KEY, "",
                            ISS_CLAIM_KEY, AuthVendor.NATIVE.name()
                    ),
                    List.of(new SimpleGrantedAuthority(PrimaryRole.ROLE_ANONYMOUS.name()))
            );
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
            boolean isRs256 = "RS256".equalsIgnoreCase(header.optString("alg", ""));

            if (!isJwt || !isRs256) {
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
        JSONObject payload = parseJwtPayload(token);
        String oidcId = payload.optString(SUB_CLAIM_KEY, "");

        User user = findUserPort.findByAuthVendorAndOidcId(vendor, oidcId).orElse(null);

        if (FormatValidator.hasValue(user)) {
            return new DefaultOAuth2AuthenticatedPrincipal(
                    String.valueOf(user.getId()),
                    Map.of(
                            SUB_CLAIM_KEY, oidcId,
                            ISS_CLAIM_KEY, vendor.name()
                    ),
                    List.of(new SimpleGrantedAuthority(user.getRole().getId()))
            );
        }

        return new DefaultOAuth2AuthenticatedPrincipal(
                "-1",
                Map.of(
                        SUB_CLAIM_KEY, oidcId,
                        ISS_CLAIM_KEY, vendor.name()
                ),
                List.of(new SimpleGrantedAuthority(PrimaryRole.ROLE_GUEST.name()))
        );
    }

    private JSONObject parseJwtPayload(String token) {
        String[] parts = token.split("\\.");
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
        return new JSONObject(payloadJson);
    }
}
