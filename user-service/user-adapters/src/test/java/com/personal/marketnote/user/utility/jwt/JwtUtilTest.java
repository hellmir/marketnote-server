package com.personal.marketnote.user.utility.jwt;

import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import com.personal.marketnote.user.utility.jwt.claims.TokenClaims;
import com.personal.marketnote.user.utility.jwt.exception.InvalidJwtGenerationParametersException;
import com.personal.marketnote.user.utility.jwt.exception.InvalidJwtTokenTypeException;
import com.personal.marketnote.user.utility.jwt.exception.InvalidOAuth2VendorException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtUtil JWT 파싱 및 클레임 추출 로직 테스트")
class JwtUtilTest {

    private static final String JWT_SECRET = "test-secret-key-for-hs256-jwt-unit-test-minimum-256-bits";
    private static final String OTHER_SECRET = "another-secret-key-for-hs256-jwt-test-minimum-256-bits!!";
    private static final Long ACCESS_TOKEN_TTL = 3_600_000L;
    private static final Long REFRESH_TOKEN_TTL = 604_800_000L;

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(JWT_SECRET, ACCESS_TOKEN_TTL, REFRESH_TOKEN_TTL);
    }

    @Nested
    @DisplayName("generateAccessToken - 액세스 토큰 생성")
    class GenerateAccessTokenTest {

        @Test
        @DisplayName("유효한 파라미터로 액세스 토큰을 생성한다")
        void shouldGenerateAccessTokenWithValidParameters() {
            String token = jwtUtil.generateAccessToken(
                    "user-subject",
                    42L,
                    List.of("ROLE_USER"),
                    AuthVendor.NATIVE
            );

            assertThat(token).isNotBlank();
            TokenClaims claims = jwtUtil.parseAccessToken(token);
            assertThat(claims.getId()).isEqualTo("user-subject");
            assertThat(claims.getUserId()).isEqualTo(42L);
            assertThat(claims.getRoleIds()).containsExactly("ROLE_USER");
            assertThat(claims.getAuthVendor()).isEqualTo(AuthVendor.NATIVE);
            assertThat(claims.getTokenType()).isEqualTo(JwtTokenType.ACCESS_TOKEN);
        }

        @Test
        @DisplayName("userId가 null이어도 액세스 토큰을 생성한다")
        void shouldGenerateAccessTokenWhenUserIdIsNull() {
            String token = jwtUtil.generateAccessToken(
                    "user-subject",
                    null,
                    List.of("ROLE_USER"),
                    AuthVendor.NATIVE
            );

            TokenClaims claims = jwtUtil.parseAccessToken(token);
            assertThat(claims.getUserId()).isNull();
        }

        @Test
        @DisplayName("id가 null이면 InvalidJwtGenerationParametersException을 던진다")
        void shouldThrowInvalidJwtGenerationParametersExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> jwtUtil.generateAccessToken(
                    null,
                    42L,
                    List.of("ROLE_USER"),
                    AuthVendor.NATIVE
            ))
                    .isInstanceOf(InvalidJwtGenerationParametersException.class)
                    .hasMessageContaining("memberId cannot be null");
        }

        @Test
        @DisplayName("roleIds가 비어있으면 InvalidJwtGenerationParametersException을 던진다")
        void shouldThrowInvalidJwtGenerationParametersExceptionWhenRoleIdsIsEmpty() {
            assertThatThrownBy(() -> jwtUtil.generateAccessToken(
                    "user-subject",
                    42L,
                    List.of(),
                    AuthVendor.NATIVE
            ))
                    .isInstanceOf(InvalidJwtGenerationParametersException.class)
                    .hasMessageContaining("roleIds cannot be empty");
        }

        @Test
        @DisplayName("authVendor가 null이면 InvalidJwtGenerationParametersException을 던진다")
        void shouldThrowInvalidJwtGenerationParametersExceptionWhenAuthVendorIsNull() {
            assertThatThrownBy(() -> jwtUtil.generateAccessToken(
                    "user-subject",
                    42L,
                    List.of("ROLE_USER"),
                    null
            ))
                    .isInstanceOf(InvalidJwtGenerationParametersException.class)
                    .hasMessageContaining("authVendor cannot be null");
        }

        @Test
        @DisplayName("여러 파라미터가 동시에 null이면 모든 에러 메시지를 포함한다")
        void shouldIncludeAllMessagesWhenMultipleParametersInvalid() {
            assertThatThrownBy(() -> jwtUtil.generateAccessToken(
                    null,
                    null,
                    List.of(),
                    null
            ))
                    .isInstanceOf(InvalidJwtGenerationParametersException.class)
                    .hasMessageContaining("memberId cannot be null")
                    .hasMessageContaining("roleIds cannot be empty")
                    .hasMessageContaining("authVendor cannot be null");
        }
    }

    @Nested
    @DisplayName("generateRefreshToken - 리프레시 토큰 생성")
    class GenerateRefreshTokenTest {

        @Test
        @DisplayName("userId 없이 리프레시 토큰을 생성한다")
        void shouldGenerateRefreshTokenWithoutUserId() {
            String token = jwtUtil.generateRefreshToken(
                    "user-subject",
                    List.of("ROLE_USER"),
                    AuthVendor.KAKAO
            );

            assertThat(token).isNotBlank();
            TokenClaims claims = jwtUtil.parseRefreshToken(token);
            assertThat(claims.getId()).isEqualTo("user-subject");
            assertThat(claims.getUserId()).isNull();
            assertThat(claims.getAuthVendor()).isEqualTo(AuthVendor.KAKAO);
            assertThat(claims.getTokenType()).isEqualTo(JwtTokenType.REFRESH_TOKEN);
        }

        @Test
        @DisplayName("userId 포함하여 리프레시 토큰을 생성한다")
        void shouldGenerateRefreshTokenWithUserId() {
            String token = jwtUtil.generateRefreshToken(
                    "user-subject",
                    99L,
                    List.of("ROLE_USER"),
                    AuthVendor.GOOGLE
            );

            assertThat(token).isNotBlank();
            TokenClaims claims = jwtUtil.parseRefreshToken(token);
            assertThat(claims.getUserId()).isEqualTo(99L);
            assertThat(claims.getAuthVendor()).isEqualTo(AuthVendor.GOOGLE);
        }
    }

    @Nested
    @DisplayName("parseAccessToken - 액세스 토큰 파싱")
    class ParseAccessTokenTest {

        @Test
        @DisplayName("유효한 액세스 토큰을 파싱하여 TokenClaims를 반환한다")
        void shouldParseValidAccessToken() {
            LocalDateTime beforeGeneration = LocalDateTime.now().minusSeconds(1);
            String token = jwtUtil.generateAccessToken(
                    "user-subject",
                    42L,
                    List.of("ROLE_USER", "ROLE_ADMIN"),
                    AuthVendor.NATIVE
            );
            LocalDateTime afterGeneration = LocalDateTime.now().plusSeconds(1);

            TokenClaims claims = jwtUtil.parseAccessToken(token);

            assertThat(claims.getId()).isEqualTo("user-subject");
            assertThat(claims.getUserId()).isEqualTo(42L);
            assertThat(claims.getRoleIds()).containsExactly("ROLE_USER", "ROLE_ADMIN");
            assertThat(claims.getAuthVendor()).isEqualTo(AuthVendor.NATIVE);
            assertThat(claims.getTokenType()).isEqualTo(JwtTokenType.ACCESS_TOKEN);
            assertThat(claims.getIssuedAt()).isBetween(beforeGeneration, afterGeneration);
            assertThat(claims.getExpirationAt()).isAfter(claims.getIssuedAt());
        }

        @Test
        @DisplayName("리프레시 토큰을 액세스 토큰으로 파싱하면 InvalidJwtTokenTypeException을 던진다")
        void shouldThrowInvalidJwtTokenTypeExceptionWhenRefreshTokenParsedAsAccessToken() {
            String refreshToken = jwtUtil.generateRefreshToken(
                    "user-subject",
                    42L,
                    List.of("ROLE_USER"),
                    AuthVendor.NATIVE
            );

            assertThatThrownBy(() -> jwtUtil.parseAccessToken(refreshToken))
                    .isInstanceOf(InvalidJwtTokenTypeException.class)
                    .hasMessageContaining("ACCESS_TOKEN")
                    .hasMessageContaining("REFRESH_TOKEN");
        }

        @Test
        @DisplayName("만료된 액세스 토큰을 파싱하면 ExpiredJwtException을 던진다")
        void shouldThrowExpiredJwtExceptionWhenAccessTokenExpired() {
            String expiredToken = buildExpiredToken(JwtTokenType.ACCESS_TOKEN);

            assertThatThrownBy(() -> jwtUtil.parseAccessToken(expiredToken))
                    .isInstanceOf(ExpiredJwtException.class);
        }

        @Test
        @DisplayName("다른 시크릿으로 서명된 액세스 토큰을 파싱하면 JwtException을 던진다")
        void shouldThrowJwtExceptionWhenAccessTokenSignatureInvalid() {
            JwtUtil otherJwtUtil = new JwtUtil(OTHER_SECRET, ACCESS_TOKEN_TTL, REFRESH_TOKEN_TTL);
            String tokenFromOtherSecret = otherJwtUtil.generateAccessToken(
                    "user-subject",
                    42L,
                    List.of("ROLE_USER"),
                    AuthVendor.NATIVE
            );

            assertThatThrownBy(() -> jwtUtil.parseAccessToken(tokenFromOtherSecret))
                    .isInstanceOf(JwtException.class);
        }
    }

    @Nested
    @DisplayName("parseRefreshToken - 리프레시 토큰 파싱")
    class ParseRefreshTokenTest {

        @Test
        @DisplayName("유효한 리프레시 토큰을 파싱하여 TokenClaims를 반환한다")
        void shouldParseValidRefreshToken() {
            String token = jwtUtil.generateRefreshToken(
                    "user-subject",
                    99L,
                    List.of("ROLE_USER"),
                    AuthVendor.APPLE
            );

            TokenClaims claims = jwtUtil.parseRefreshToken(token);

            assertThat(claims.getId()).isEqualTo("user-subject");
            assertThat(claims.getUserId()).isEqualTo(99L);
            assertThat(claims.getAuthVendor()).isEqualTo(AuthVendor.APPLE);
            assertThat(claims.getTokenType()).isEqualTo(JwtTokenType.REFRESH_TOKEN);
        }

        @Test
        @DisplayName("userId 없는 리프레시 토큰을 파싱하면 userId가 null이다")
        void shouldReturnNullUserIdWhenRefreshTokenHasNoUserId() {
            String token = jwtUtil.generateRefreshToken(
                    "user-subject",
                    List.of("ROLE_USER"),
                    AuthVendor.NATIVE
            );

            TokenClaims claims = jwtUtil.parseRefreshToken(token);

            assertThat(claims.getUserId()).isNull();
        }

        @Test
        @DisplayName("액세스 토큰을 리프레시 토큰으로 파싱하면 InvalidJwtTokenTypeException을 던진다")
        void shouldThrowInvalidJwtTokenTypeExceptionWhenAccessTokenParsedAsRefreshToken() {
            String accessToken = jwtUtil.generateAccessToken(
                    "user-subject",
                    42L,
                    List.of("ROLE_USER"),
                    AuthVendor.NATIVE
            );

            assertThatThrownBy(() -> jwtUtil.parseRefreshToken(accessToken))
                    .isInstanceOf(InvalidJwtTokenTypeException.class)
                    .hasMessageContaining("REFRESH_TOKEN")
                    .hasMessageContaining("ACCESS_TOKEN");
        }

        @Test
        @DisplayName("만료된 리프레시 토큰을 파싱하면 ExpiredJwtException을 던진다")
        void shouldThrowExpiredJwtExceptionWhenRefreshTokenExpired() {
            String expiredToken = buildExpiredToken(JwtTokenType.REFRESH_TOKEN);

            assertThatThrownBy(() -> jwtUtil.parseRefreshToken(expiredToken))
                    .isInstanceOf(ExpiredJwtException.class);
        }

        @Test
        @DisplayName("다른 시크릿으로 서명된 리프레시 토큰을 파싱하면 JwtException을 던진다")
        void shouldThrowJwtExceptionWhenRefreshTokenSignatureInvalid() {
            JwtUtil otherJwtUtil = new JwtUtil(OTHER_SECRET, ACCESS_TOKEN_TTL, REFRESH_TOKEN_TTL);
            String tokenFromOtherSecret = otherJwtUtil.generateRefreshToken(
                    "user-subject",
                    99L,
                    List.of("ROLE_USER"),
                    AuthVendor.NATIVE
            );

            assertThatThrownBy(() -> jwtUtil.parseRefreshToken(tokenFromOtherSecret))
                    .isInstanceOf(JwtException.class);
        }
    }

    @Nested
    @DisplayName("chooseVendor - iss 클레임 기반 벤더 추론 (authVendor 클레임 부재 시)")
    class ChooseVendorTest {

        @Test
        @DisplayName("authVendor 클레임 없이 iss에 kakao가 포함되면 KAKAO로 파싱한다")
        void shouldResolveKakaoVendorFromIss() {
            String token = buildTokenWithoutAuthVendor("https://kauth.kakao.com");

            TokenClaims claims = jwtUtil.parseAccessToken(token);

            assertThat(claims.getAuthVendor()).isEqualTo(AuthVendor.KAKAO);
        }

        @Test
        @DisplayName("authVendor 클레임 없이 iss에 google이 포함되면 GOOGLE로 파싱한다")
        void shouldResolveGoogleVendorFromIss() {
            String token = buildTokenWithoutAuthVendor("https://accounts.google.com");

            TokenClaims claims = jwtUtil.parseAccessToken(token);

            assertThat(claims.getAuthVendor()).isEqualTo(AuthVendor.GOOGLE);
        }

        @Test
        @DisplayName("authVendor 클레임 없이 iss에 apple이 포함되면 APPLE로 파싱한다")
        void shouldResolveAppleVendorFromIss() {
            String token = buildTokenWithoutAuthVendor("https://appleid.apple.com");

            TokenClaims claims = jwtUtil.parseAccessToken(token);

            assertThat(claims.getAuthVendor()).isEqualTo(AuthVendor.APPLE);
        }

        @Test
        @DisplayName("authVendor 클레임 없이 알 수 없는 iss이면 InvalidOAuth2VendorException을 던진다")
        void shouldThrowInvalidOAuth2VendorExceptionWhenIssDoesNotMatchAnyVendor() {
            String token = buildTokenWithoutAuthVendor("https://unknown-issuer.example.com");

            assertThatThrownBy(() -> jwtUtil.parseAccessToken(token))
                    .isInstanceOf(InvalidOAuth2VendorException.class)
                    .hasMessageContaining("unknown-issuer");
        }
    }

    @Nested
    @DisplayName("revokeToken - 토큰 폐기")
    class RevokeTokenTest {

        @Test
        @DisplayName("토큰을 폐기하면 만료된 새 토큰을 반환한다")
        void shouldReturnExpiredTokenWhenRevoked() {
            String validToken = jwtUtil.generateAccessToken(
                    "user-subject",
                    42L,
                    List.of("ROLE_USER"),
                    AuthVendor.NATIVE
            );

            String revokedToken = jwtUtil.revokeToken(validToken);

            assertThat(revokedToken).isNotBlank().isNotEqualTo(validToken);
            assertThatThrownBy(() -> jwtUtil.parseAccessToken(revokedToken))
                    .isInstanceOf(ExpiredJwtException.class);
        }
    }

    private String buildExpiredToken(JwtTokenType tokenType) {
        SecretKey key = signingKey(JWT_SECRET);
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claim("tokenType", tokenType.name())
                .issuedAt(new Date(now - 10_000L))
                .expiration(new Date(now - 1_000L))
                .subject("user-subject")
                .claim("roleIds", List.of("ROLE_USER"))
                .claim("userId", 42L)
                .claim("authVendor", AuthVendor.NATIVE.name())
                .signWith(key)
                .compact();
    }

    private String buildTokenWithoutAuthVendor(String iss) {
        SecretKey key = signingKey(JWT_SECRET);
        return Jwts.builder()
                .claim("tokenType", JwtTokenType.ACCESS_TOKEN.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_TTL))
                .subject("user-subject")
                .claim("roleIds", List.of("ROLE_USER"))
                .claim("userId", 42L)
                .issuer(iss)
                .signWith(key)
                .compact();
    }

    private static SecretKey signingKey(String secret) {
        return new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );
    }
}
