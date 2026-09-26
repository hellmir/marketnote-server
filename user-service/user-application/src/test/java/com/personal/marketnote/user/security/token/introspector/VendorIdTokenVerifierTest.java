package com.personal.marketnote.user.security.token.introspector;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VendorIdTokenVerifierTest {
    private static final String KAKAO_ISSUER = "https://kauth.kakao.com";
    private static final String KAKAO_AUDIENCE = "kakao-client-id";
    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String APPLE_AUDIENCE = "apple-client-id";

    private MockWebServer mockWebServer;
    private RSAKey rsaKey;
    private ECKey ecKey;

    @BeforeEach
    void setUp() throws Exception {
        rsaKey = new RSAKeyGenerator(2048)
                .keyID("test-rsa-key")
                .generate();

        ecKey = new ECKeyGenerator(Curve.P_256)
                .keyID("test-ec-key")
                .generate();

        mockWebServer = new MockWebServer();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("RS256 서명된 정상 벤더 ID 토큰의 subject를 반환한다")
    void verifyValidRs256TokenReturnsSubject() throws Exception {
        // given
        setDispatcher(new JWKSet(rsaKey.toPublicJWK()));
        mockWebServer.start();
        String jwksUri = mockWebServer.url("/jwks").toString();

        VendorIdTokenVerifier verifier = buildVerifier(
                AuthVendor.KAKAO, jwksUri, List.of(KAKAO_ISSUER), KAKAO_AUDIENCE
        );

        String token = buildRs256Token(KAKAO_ISSUER, KAKAO_AUDIENCE, "kakao-subject-123", rsaKey);

        // when
        String subject = verifier.verifyAndExtractSubject(token, AuthVendor.KAKAO);

        // then
        assertThat(subject).isEqualTo("kakao-subject-123");
    }

    @Test
    @DisplayName("ES256 서명된 정상 벤더 ID 토큰의 subject를 반환한다")
    void verifyValidEs256TokenReturnsSubject() throws Exception {
        // given
        setDispatcher(new JWKSet(ecKey.toPublicJWK()));
        mockWebServer.start();
        String jwksUri = mockWebServer.url("/jwks").toString();

        VendorIdTokenVerifier verifier = buildVerifier(
                AuthVendor.APPLE, jwksUri, List.of(APPLE_ISSUER), APPLE_AUDIENCE
        );

        String token = buildEs256Token(APPLE_ISSUER, APPLE_AUDIENCE, "apple-subject-456");

        // when
        String subject = verifier.verifyAndExtractSubject(token, AuthVendor.APPLE);

        // then
        assertThat(subject).isEqualTo("apple-subject-456");
    }

    @Test
    @DisplayName("서명이 위조된 토큰 검증 시 JwtException을 발생시킨다")
    void verifyForgedTokenThrowsJwtException() throws Exception {
        // given
        setDispatcher(new JWKSet(rsaKey.toPublicJWK()));
        mockWebServer.start();
        String jwksUri = mockWebServer.url("/jwks").toString();

        VendorIdTokenVerifier verifier = buildVerifier(
                AuthVendor.KAKAO, jwksUri, List.of(KAKAO_ISSUER), KAKAO_AUDIENCE
        );

        RSAKey differentKey = new RSAKeyGenerator(2048).keyID("test-rsa-key").generate();
        String forgedToken = buildRs256Token(KAKAO_ISSUER, KAKAO_AUDIENCE, "victim-subject", differentKey);

        // when & then
        assertThatThrownBy(() -> verifier.verifyAndExtractSubject(forgedToken, AuthVendor.KAKAO))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("만료된 토큰 검증 시 JwtException을 발생시킨다")
    void verifyExpiredTokenThrowsJwtException() throws Exception {
        // given
        setDispatcher(new JWKSet(rsaKey.toPublicJWK()));
        mockWebServer.start();
        String jwksUri = mockWebServer.url("/jwks").toString();

        VendorIdTokenVerifier verifier = buildVerifier(
                AuthVendor.KAKAO, jwksUri, List.of(KAKAO_ISSUER), KAKAO_AUDIENCE
        );

        String expiredToken = buildRs256TokenWithExpiry(
                KAKAO_ISSUER, KAKAO_AUDIENCE, "kakao-subject",
                Date.from(Instant.now().minusSeconds(3600))
        );

        // when & then
        assertThatThrownBy(() -> verifier.verifyAndExtractSubject(expiredToken, AuthVendor.KAKAO))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("잘못된 issuer의 토큰 검증 시 JwtException을 발생시킨다")
    void verifyInvalidIssuerThrowsJwtException() throws Exception {
        // given
        setDispatcher(new JWKSet(rsaKey.toPublicJWK()));
        mockWebServer.start();
        String jwksUri = mockWebServer.url("/jwks").toString();

        VendorIdTokenVerifier verifier = buildVerifier(
                AuthVendor.KAKAO, jwksUri, List.of(KAKAO_ISSUER), KAKAO_AUDIENCE
        );

        String wrongIssuerToken = buildRs256Token("https://evil.com", KAKAO_AUDIENCE, "subject", rsaKey);

        // when & then
        assertThatThrownBy(() -> verifier.verifyAndExtractSubject(wrongIssuerToken, AuthVendor.KAKAO))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("잘못된 audience의 토큰 검증 시 JwtException을 발생시킨다")
    void verifyInvalidAudienceThrowsJwtException() throws Exception {
        // given
        setDispatcher(new JWKSet(rsaKey.toPublicJWK()));
        mockWebServer.start();
        String jwksUri = mockWebServer.url("/jwks").toString();

        VendorIdTokenVerifier verifier = buildVerifier(
                AuthVendor.KAKAO, jwksUri, List.of(KAKAO_ISSUER), KAKAO_AUDIENCE
        );

        String wrongAudienceToken = buildRs256Token(KAKAO_ISSUER, "wrong-audience", "subject", rsaKey);

        // when & then
        assertThatThrownBy(() -> verifier.verifyAndExtractSubject(wrongAudienceToken, AuthVendor.KAKAO))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("sub claim이 없는 토큰 검증 시 JwtException을 발생시킨다")
    void verifyTokenWithoutSubClaimThrowsJwtException() throws Exception {
        // given
        setDispatcher(new JWKSet(rsaKey.toPublicJWK()));
        mockWebServer.start();
        String jwksUri = mockWebServer.url("/jwks").toString();

        VendorIdTokenVerifier verifier = buildVerifier(
                AuthVendor.KAKAO, jwksUri, List.of(KAKAO_ISSUER), KAKAO_AUDIENCE
        );

        String noSubToken = buildRs256TokenWithoutSubject(KAKAO_ISSUER, KAKAO_AUDIENCE);

        // when & then
        assertThatThrownBy(() -> verifier.verifyAndExtractSubject(noSubToken, AuthVendor.KAKAO))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("sub claim");
    }

    @Test
    @DisplayName("설정되지 않은 벤더의 토큰 검증 시 JwtException을 발생시킨다")
    void verifyUnconfiguredVendorThrowsJwtException() {
        // given
        VendorIdTokenVerifier verifier = new VendorIdTokenVerifier(Map.of());

        // when & then
        assertThatThrownBy(() -> verifier.verifyAndExtractSubject("any-token", AuthVendor.GOOGLE))
                .isInstanceOf(JwtException.class);
    }

    private VendorIdTokenVerifier buildVerifier(
            AuthVendor vendor, String jwksUri, List<String> issuers, String audience
    ) {
        VendorJwtConfig config = new VendorJwtConfig(jwksUri, issuers, audience);
        return new VendorIdTokenVerifier(Map.of(vendor, config));
    }

    private void setDispatcher(JWKSet jwkSet) {
        String jwksBody = jwkSet.toString();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return new MockResponse()
                        .setBody(jwksBody)
                        .addHeader("Content-Type", "application/json");
            }
        });
    }

    private String buildRs256Token(
            String issuer, String audience, String subject, RSAKey signingKey
    ) throws Exception {
        JWSSigner signer = new RSASSASigner(signingKey);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(audience)
                .subject(subject)
                .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
                .issueTime(new Date())
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(signingKey.getKeyID()).build(),
                claims
        );
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }

    private String buildRs256TokenWithExpiry(
            String issuer, String audience, String subject, Date expiry
    ) throws Exception {
        JWSSigner signer = new RSASSASigner(rsaKey);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(audience)
                .subject(subject)
                .expirationTime(expiry)
                .issueTime(new Date())
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
                claims
        );
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }

    private String buildRs256TokenWithoutSubject(String issuer, String audience) throws Exception {
        JWSSigner signer = new RSASSASigner(rsaKey);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(audience)
                .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
                .issueTime(new Date())
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
                claims
        );
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }

    private String buildEs256Token(String issuer, String audience, String subject) throws Exception {
        JWSSigner signer = new ECDSASigner(ecKey);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(audience)
                .subject(subject)
                .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
                .issueTime(new Date())
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(ecKey.getKeyID()).build(),
                claims
        );
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }
}
