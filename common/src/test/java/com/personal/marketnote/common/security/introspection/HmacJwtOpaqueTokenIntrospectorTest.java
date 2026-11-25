package com.personal.marketnote.common.security.introspection;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HmacJwtOpaqueTokenIntrospectorTest {

    private static final String SECRET = "test-hmac-jwt-secret-key-1234567890";
    private static final String HMAC_ALG = "HmacSHA256";

    private ObjectMapper objectMapper;
    private HmacJwtOpaqueTokenIntrospector introspector;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        introspector = new HmacJwtOpaqueTokenIntrospector(objectMapper, SECRET);
    }

    @Test
    @DisplayName("유효한 HS256 토큰을 검증하면 sub 클레임을 principal name으로 가진 인증 객체를 반환한다")
    void shouldReturnPrincipalWhenValidToken() throws Exception {
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        long exp = Instant.now().getEpochSecond() + 3600;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", "100");
        payload.put("exp", exp);
        String token = signToken(header, payload, SECRET);

        OAuth2AuthenticatedPrincipal principal = introspector.introspect(token);

        assertThat(principal.getName()).isEqualTo("100");
        assertThat((Object) principal.getAttribute("sub")).isEqualTo("100");
    }

    @Test
    @DisplayName("payload에 권한 정보가 없으면 기본 권한 ROLE_USER를 부여한다")
    void shouldAssignDefaultRoleUserWhenNoAuthorityClaim() throws Exception {
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", "1");
        payload.put("exp", Instant.now().getEpochSecond() + 600);
        String token = signToken(header, payload, SECRET);

        OAuth2AuthenticatedPrincipal principal = introspector.introspect(token);

        assertThat(authorityValues(principal)).containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("roles 클레임이 컬렉션이면 권한으로 추출한다")
    void shouldExtractAuthoritiesFromRolesClaim() throws Exception {
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", "1");
        payload.put("exp", Instant.now().getEpochSecond() + 600);
        payload.put("roles", List.of("ROLE_ADMIN", "ROLE_SELLER"));
        String token = signToken(header, payload, SECRET);

        OAuth2AuthenticatedPrincipal principal = introspector.introspect(token);

        assertThat(authorityValues(principal)).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_SELLER");
    }

    @Test
    @DisplayName("roleIds, authorities 클레임이 컬렉션이면 권한으로 합쳐 추출한다")
    void shouldExtractAuthoritiesFromRoleIdsAndAuthoritiesClaims() throws Exception {
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", "1");
        payload.put("exp", Instant.now().getEpochSecond() + 600);
        payload.put("roleIds", List.of("10", "20"));
        payload.put("authorities", List.of("PERM_READ"));
        String token = signToken(header, payload, SECRET);

        OAuth2AuthenticatedPrincipal principal = introspector.introspect(token);

        assertThat(authorityValues(principal)).containsExactlyInAnyOrder("10", "20", "PERM_READ");
    }

    @Test
    @DisplayName("scope 클레임이 공백 구분 문자열이면 권한으로 분해 추출한다")
    void shouldExtractAuthoritiesFromScopeClaim() throws Exception {
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", "1");
        payload.put("exp", Instant.now().getEpochSecond() + 600);
        payload.put("scope", "read write delete");
        String token = signToken(header, payload, SECRET);

        OAuth2AuthenticatedPrincipal principal = introspector.introspect(token);

        assertThat(authorityValues(principal)).containsExactlyInAnyOrder("read", "write", "delete");
    }

    @Test
    @DisplayName("sub 클레임이 없으면 기본값 -1을 principal name으로 사용한다")
    void shouldUseDefaultSubWhenMissing() throws Exception {
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("exp", Instant.now().getEpochSecond() + 600);
        String token = signToken(header, payload, SECRET);

        OAuth2AuthenticatedPrincipal principal = introspector.introspect(token);

        assertThat(principal.getName()).isEqualTo("-1");
    }

    @Test
    @DisplayName("exp/iat/nbf 숫자 클레임을 Instant로 변환하여 attribute에 담는다")
    void shouldNormalizeTimeClaimsToInstant() throws Exception {
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        long exp = Instant.now().getEpochSecond() + 600;
        long iat = Instant.now().getEpochSecond() - 60;
        long nbf = Instant.now().getEpochSecond() - 30;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", "1");
        payload.put("exp", exp);
        payload.put("iat", iat);
        payload.put("nbf", nbf);
        String token = signToken(header, payload, SECRET);

        OAuth2AuthenticatedPrincipal principal = introspector.introspect(token);

        assertThat((Object) principal.getAttribute("exp")).isEqualTo(Instant.ofEpochSecond(exp));
        assertThat((Object) principal.getAttribute("iat")).isEqualTo(Instant.ofEpochSecond(iat));
        assertThat((Object) principal.getAttribute("nbf")).isEqualTo(Instant.ofEpochSecond(nbf));
    }

    @Test
    @DisplayName("토큰이 null이면 형식 오류로 예외를 던진다")
    void shouldThrowWhenTokenIsNull() {
        assertThatThrownBy(() -> introspector.introspect(null))
                .isInstanceOf(BadOpaqueTokenException.class)
                .hasMessageContaining("Invalid token format");
    }

    @Test
    @DisplayName("토큰이 빈 문자열이면 형식 오류로 예외를 던진다")
    void shouldThrowWhenTokenIsBlank() {
        assertThatThrownBy(() -> introspector.introspect(""))
                .isInstanceOf(BadOpaqueTokenException.class)
                .hasMessageContaining("Invalid token format");
    }

    @Test
    @DisplayName("토큰이 3 파트가 아니면 형식 오류로 예외를 던진다")
    void shouldThrowWhenTokenPartsNotThree() {
        assertThatThrownBy(() -> introspector.introspect("only.two"))
                .isInstanceOf(BadOpaqueTokenException.class)
                .hasMessageContaining("Invalid token format");
    }

    @Test
    @DisplayName("alg가 HS256이 아니면 지원하지 않는 알고리즘으로 예외를 던진다")
    void shouldThrowWhenAlgNotHs256() throws Exception {
        Map<String, Object> header = Map.of("alg", "RS256");
        Map<String, Object> payload = Map.of("sub", "1", "exp", Instant.now().getEpochSecond() + 60);
        String token = signToken(header, payload, SECRET);

        assertThatThrownBy(() -> introspector.introspect(token))
                .isInstanceOf(BadOpaqueTokenException.class)
                .hasMessageContaining("Unsupported alg");
    }

    @Test
    @DisplayName("서명이 다른 키로 생성되었으면 서명 검증 실패 예외를 던진다")
    void shouldThrowWhenSignatureInvalid() throws Exception {
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = Map.of("sub", "1", "exp", Instant.now().getEpochSecond() + 60);
        String token = signToken(header, payload, "different-secret");

        assertThatThrownBy(() -> introspector.introspect(token))
                .isInstanceOf(BadOpaqueTokenException.class)
                .hasMessageContaining("Invalid signature");
    }

    @Test
    @DisplayName("exp가 현재 시간보다 과거이면 만료 예외를 던진다")
    void shouldThrowWhenTokenExpired() throws Exception {
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = Map.of("sub", "1", "exp", Instant.now().getEpochSecond() - 10);
        String token = signToken(header, payload, SECRET);

        assertThatThrownBy(() -> introspector.introspect(token))
                .isInstanceOf(BadOpaqueTokenException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("payload가 유효한 Base64URL 이지만 JSON이 아니면 파싱 실패 예외를 던진다")
    void shouldThrowWhenPayloadNotJson() throws Exception {
        String headerB64 = base64Url(objectMapper.writeValueAsBytes(Map.of("alg", "HS256")));
        String payloadB64 = base64Url("not-a-json".getBytes(StandardCharsets.UTF_8));
        String signingInput = headerB64 + "." + payloadB64;
        String sigB64 = base64Url(hmac(signingInput.getBytes(StandardCharsets.UTF_8), SECRET));
        String token = headerB64 + "." + payloadB64 + "." + sigB64;

        assertThatThrownBy(() -> introspector.introspect(token))
                .isInstanceOf(BadOpaqueTokenException.class)
                .hasMessageContaining("Invalid token part");
    }

    private List<String> authorityValues(OAuth2AuthenticatedPrincipal principal) {
        return principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    private String signToken(Map<String, Object> header, Map<String, Object> payload, String secret) throws Exception {
        String headerB64 = base64Url(objectMapper.writeValueAsBytes(header));
        String payloadB64 = base64Url(objectMapper.writeValueAsBytes(payload));
        String signingInput = headerB64 + "." + payloadB64;
        String sigB64 = base64Url(hmac(signingInput.getBytes(StandardCharsets.UTF_8), secret));
        return headerB64 + "." + payloadB64 + "." + sigB64;
    }

    private byte[] hmac(byte[] data, String secret) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALG);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALG));
        return mac.doFinal(data);
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
