package com.personal.marketnote.common.security.introspection;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.utility.FormatValidator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.Base64.Decoder;
import java.util.stream.Collectors;

/**
 * HS256 서명 검증 기반의 OpaqueTokenIntrospector.
 * - HMAC-SHA256 서명 검증
 * - exp 만료 검사(초 단위)
 * - 권한은 claims 중 roles/roleIds/authorities/scope 에서 추출
 */
public class HmacJwtOpaqueTokenIntrospector implements OpaqueTokenIntrospector {
    private static final Decoder BASE64_URL = Base64.getUrlDecoder();
    private static final String HMAC_ALG = "HmacSHA256";

    private final ObjectMapper objectMapper;
    private final byte[] secret;

    public HmacJwtOpaqueTokenIntrospector(ObjectMapper objectMapper, String secret) {
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        String[] parts = FormatValidator.hasValue(token)
                ? token.split("\\.")
                : new String[0];
        if (parts.length != 3) {
            throw new BadOpaqueTokenException("Invalid token format");
        }

        String headerB64 = parts[0];
        String payloadB64 = parts[1];
        String sigB64 = parts[2];

        Map<String, Object> header = parsePart(headerB64);
        String alg = String.valueOf(header.getOrDefault("alg", ""));
        if (!"HS256".equalsIgnoreCase(alg)) {
            throw new BadOpaqueTokenException("Unsupported alg: " + alg);
        }

        // 서명 검증
        verifySignature(headerB64, payloadB64, sigB64);

        Map<String, Object> payload = parsePart(payloadB64);
        // exp 만료 검사 (초 단위 가정)
        Object expObj = payload.get("exp");
        if (expObj instanceof Number expNum) {
            long exp = expNum.longValue();
            long now = Instant.now().getEpochSecond();
            if (now >= exp) {
                throw new BadOpaqueTokenException("Token expired");
            }
        }

        String sub = String.valueOf(payload.getOrDefault("sub", "-1"));
        Collection<GrantedAuthority> authorities = extractAuthorities(payload);

        // Spring Security는 exp/iat/nbf를 Instant로 기대하므로 변환
        Map<String, Object> normalized = new HashMap<>(payload);
        Object iatObj = payload.get("iat");
        Object nbfObj = payload.get("nbf");
        if (expObj instanceof Number n) {
            normalized.put("exp", Instant.ofEpochSecond(n.longValue()));
        }
        if (iatObj instanceof Number n) {
            normalized.put("iat", Instant.ofEpochSecond(n.longValue()));
        }
        if (nbfObj instanceof Number n) {
            normalized.put("nbf", Instant.ofEpochSecond(n.longValue()));
        }

        return new DefaultOAuth2AuthenticatedPrincipal(sub, normalized, authorities);
    }

    private Map<String, Object> parsePart(String b64) {
        try {
            byte[] jsonBytes = BASE64_URL.decode(b64);
            return objectMapper.readValue(jsonBytes, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new BadOpaqueTokenException("Invalid token part");
        }
    }

    private void verifySignature(String headerB64, String payloadB64, String sigB64) {
        try {
            String signingInput = headerB64 + "." + payloadB64;
            Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(new SecretKeySpec(secret, HMAC_ALG));
            byte[] signature = mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
            String expected = base64UrlNoPad(signature);
            if (!constantTimeEquals(expected, sigB64)) {
                throw new BadOpaqueTokenException("Invalid signature");
            }
        } catch (BadOpaqueTokenException e) {
            throw e;
        } catch (Exception e) {
            throw new BadOpaqueTokenException("Signature verification failed");
        }
    }

    private String base64UrlNoPad(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (FormatValidator.hasNoValue(a) || FormatValidator.hasNoValue(b))
            return false;
        if (a.length() != b.length())
            return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractAuthorities(Map<String, Object> claims) {
        List<String> roles = new ArrayList<>();
        for (String key : List.of("roles", "roleIds", "authorities")) {
            Object v = claims.get(key);
            if (v instanceof Collection<?> c) {
                roles.addAll(c.stream().map(Object::toString).toList());
            }
        }
        // scope (공백 구분)
        Object scopeObj = claims.get("scope");
        if (scopeObj instanceof String scope && !scope.isBlank()) {
            roles.addAll(Arrays.stream(scope.split("\\s+")).collect(Collectors.toList()));
        }
        if (roles.isEmpty()) {
            roles.add("ROLE_USER");
        }
        LinkedHashSet<GrantedAuthority> set = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return Collections.unmodifiableSet(set);
    }
}
