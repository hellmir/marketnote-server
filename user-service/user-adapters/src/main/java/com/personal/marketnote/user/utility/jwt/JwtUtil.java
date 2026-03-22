package com.personal.marketnote.user.utility.jwt;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import com.personal.marketnote.user.utility.jwt.claims.TokenClaims;
import com.personal.marketnote.user.utility.jwt.exception.InvalidJwtGenerationParametersException;
import com.personal.marketnote.user.utility.jwt.exception.InvalidJwtTokenTypeException;
import com.personal.marketnote.user.utility.jwt.exception.InvalidOAuth2VendorException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.personal.marketnote.common.security.token.utility.TokenConstant.ISS_CLAIM_KEY;
import static com.personal.marketnote.common.utility.ApiConstant.USER_ID_KEY;

@Component
@Profile("qa.test | prod")
public class JwtUtil {
    private static final String ROLE_IDS_CLAIM_KEY = "roleIds";
    private static final String TOKEN_TYPE_CLAIM_KEY = "tokenType";
    private static final String AUTH_VENDOR_CLAIM_KEY = "authVendor";

    private final SecretKey secretKey;
    private final JwtParser parser;
    private final Map<JwtTokenType, Long> ttlsByTokenType;

    public JwtUtil(@Value("${spring.jwt.secret}") String jwtSecret,
                   // 테스트가 쉬운 구조를 만들기 위해 TTL을 enum이 아닌 Spring properties에 정의하여 생성자로 주입
                   @Value("${spring.jwt.access-token.ttl}") Long accessTokenTtl,
                   @Value("${spring.jwt.refresh-token.ttl}") Long refreshTokenTtl) {
        secretKey = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());
        parser = Jwts.parser().verifyWith(secretKey)
                .json(new JacksonDeserializer<>())
                .build();
        ttlsByTokenType = Map.of(
                JwtTokenType.ACCESS_TOKEN, accessTokenTtl,
                JwtTokenType.REFRESH_TOKEN, refreshTokenTtl
        );
    }

    public TokenClaims parseAccessToken(String token)
            throws JwtException, IllegalArgumentException, ClassCastException {
        return parse(token, JwtTokenType.ACCESS_TOKEN);
    }

    public TokenClaims parseRefreshToken(String token) throws JwtException, IllegalArgumentException, ClassCastException {
        return parse(token, JwtTokenType.REFRESH_TOKEN);
    }

    private TokenClaims parse(String token, JwtTokenType tokenType) {
        Claims claims = parser.parseSignedClaims(token).getPayload();
        Object userIdObj = claims.get(USER_ID_KEY);

        Long userId = null;

        if (FormatValidator.hasValue(userIdObj)) {
            userId = ((Number) userIdObj).longValue();
        }

        String authVendorKey = claims.get(AUTH_VENDOR_CLAIM_KEY, String.class);

        if (FormatValidator.hasNoValue(authVendorKey)) {
            String iss = claims.get(ISS_CLAIM_KEY, String.class);

            authVendorKey = chooseVendor(iss);
        }

        AuthVendor authVendor = AuthVendor.valueOfIgnoreCase(authVendorKey);

        return TokenClaims.builder()
                .tokenType(extractTokenType(claims, tokenType))
                .expirationAt(claims.getExpiration()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime())
                .issuedAt(claims.getIssuedAt()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime())
                .id(claims.getSubject())
                .userId(userId)
                .roleIds((List<String>) claims.get("roleIds"))
                .authVendor(authVendor)
                .build();
    }

    private String chooseVendor(String iss) {
        String kakao = "kakao";
        String google = "google";
        String apple = "apple";

        if (iss.contains(kakao)) {
            return kakao.toUpperCase();
        }

        if (iss.contains(google)) {
            return google.toUpperCase();
        }

        if (iss.contains(apple)) {
            return apple.toUpperCase();
        }

        throw new InvalidOAuth2VendorException(iss);
    }

    private JwtTokenType extractTokenType(Claims claims, JwtTokenType expectedTokenType) {
        JwtTokenType fromClaim = JwtTokenType.from(claims.get(TOKEN_TYPE_CLAIM_KEY, String.class));
        if (expectedTokenType != fromClaim) {
            throw new InvalidJwtTokenTypeException(expectedTokenType, fromClaim);
        }

        return fromClaim;
    }

    public String generateAccessToken(String id, Long userId, List<String> roleIds, AuthVendor authVendor) {
        validate(id, roleIds, authVendor);

        return getBuilderWithCommonClaims(JwtTokenType.ACCESS_TOKEN, id, userId, roleIds, authVendor).compact();
    }

    private void validate(String id, List<String> roleIds, AuthVendor authVendor) {
        List<String> messages = new ArrayList<>(2);
        if (FormatValidator.hasNoValue(id)) {
            messages.add("memberId cannot be null");
        }

        if (ObjectUtils.isEmpty(roleIds)) {
            messages.add("roleIds cannot be empty");
        }

        if (FormatValidator.hasNoValue(authVendor)) {
            messages.add("authVendor cannot be null");
        }

        if (!messages.isEmpty()) {
            throw new InvalidJwtGenerationParametersException(String.join("\n", messages.toArray(new String[0])));
        }
    }

    public String generateRefreshToken(String id, List<String> roleIds, AuthVendor authVendor) {
        return generateRefreshToken(id, null, roleIds, authVendor);
    }

    public String generateRefreshToken(String id, Long userId, List<String> roleIds, AuthVendor authVendor) {
        return getBuilderWithCommonClaims(JwtTokenType.REFRESH_TOKEN, id, userId, roleIds, authVendor).compact();
    }

    private JwtBuilder getBuilderWithCommonClaims(
            JwtTokenType tokenType,
            String id,
            Long userId,
            List<String> roleIds,
            AuthVendor authVendor
    ) {
        return Jwts.builder()
                .claim(TOKEN_TYPE_CLAIM_KEY, tokenType)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ttlsByTokenType.get(tokenType)))
                .subject(id)
                .claim(ROLE_IDS_CLAIM_KEY, roleIds)
                .claim(USER_ID_KEY, userId)
                .claim(AUTH_VENDOR_CLAIM_KEY, authVendor.name())
                .signWith(secretKey);
    }

    public String revokeToken(String token) {
        JwtParser parser = Jwts.parser().verifyWith(secretKey)
                .json(new JacksonDeserializer<>())
                .build();

        Claims tokenClaims = parser.parseSignedClaims(token).getPayload();

        return Jwts.builder()
                .claims(tokenClaims)
                .expiration(new Date(System.currentTimeMillis() - 1))
                .signWith(secretKey)
                .compact();
    }
}
