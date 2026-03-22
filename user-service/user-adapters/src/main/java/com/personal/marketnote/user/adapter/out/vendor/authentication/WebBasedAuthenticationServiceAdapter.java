package com.personal.marketnote.user.adapter.out.vendor.authentication;

import com.personal.marketnote.common.adapter.out.SecurityAdapter;
import com.personal.marketnote.common.domain.exception.token.InvalidRefreshTokenException;
import com.personal.marketnote.common.utility.FormatConverter;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.adapter.exception.HashAlgorithmNotAvailableException;
import com.personal.marketnote.common.utility.http.cookie.HttpCookieName;
import com.personal.marketnote.common.utility.http.cookie.HttpCookieObject;
import com.personal.marketnote.common.utility.http.cookie.HttpCookieUtils;
import com.personal.marketnote.user.adapter.in.web.authentication.request.OAuth2LoginRequest;
import com.personal.marketnote.user.adapter.in.web.authentication.response.OAuth2LoginResponse;
import com.personal.marketnote.user.adapter.in.web.authentication.response.RefreshedAccessTokenResponse;
import com.personal.marketnote.user.adapter.in.web.authentication.response.WebBasedTokenRefreshResponse;
import com.personal.marketnote.user.port.in.result.LoginResult;
import com.personal.marketnote.user.port.in.usecase.authentication.Oauth2LoginUseCase;
import com.personal.marketnote.user.security.token.dto.GrantedTokenInfo;
import com.personal.marketnote.user.security.token.support.TokenSupport;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import com.personal.marketnote.user.utility.OAuth2WebUtils;
import com.personal.marketnote.user.utility.jwt.JwtUtil;
import com.personal.marketnote.user.utility.jwt.claims.TokenClaims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.personal.marketnote.common.security.token.utility.TokenConstant.ONE_DAY_MILLIS;

@SecurityAdapter
@Slf4j
public class WebBasedAuthenticationServiceAdapter {
    private static final long REFRESH_TOKEN_COOKIE_MAX_AGE = 2592000000L;
    private static final Pattern REDIRECTION_DESTINATION_PATTERN
            = Pattern.compile("^(http|https)://[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)*(:\\d{1,5})?$");
    private final Oauth2LoginUseCase oauth2LoginUseCase;
    private final TokenSupport tokenSupport;
    private final String serverOrigin;
    private final String clientOrigin;
    private final HttpCookieUtils httpCookieUtils;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${spring.jwt.refresh-token.ttl:1209600000}")
    private Long refreshTokenTtlMillis;

    public WebBasedAuthenticationServiceAdapter(
            Oauth2LoginUseCase oauth2LoginUseCase,
            TokenSupport tokenSupport,
            @Value("${server.origin}") String serverOrigin,
            @Value("${client.origin}") List<String> clientOrigins,
            HttpCookieUtils httpCookieUtils,
            JwtUtil jwtUtil,
            StringRedisTemplate stringRedisTemplate
    ) {
        this.oauth2LoginUseCase = oauth2LoginUseCase;
        this.tokenSupport = tokenSupport;
        this.serverOrigin = serverOrigin;
        this.clientOrigin = clientOrigins.getFirst();
        this.httpCookieUtils = httpCookieUtils;
        this.jwtUtil = jwtUtil;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public OAuth2LoginResponse loginByOAuth2(OAuth2LoginRequest oAuth2LoginRequest) {
        HttpServletRequest servletRequest = oAuth2LoginRequest.request();
        String redirectUri = serverOrigin + servletRequest.getRequestURI();

        LoginResult loginResult =
                oauth2LoginUseCase.loginByOAuth2(oAuth2LoginRequest.code(), redirectUri, AuthVendor.valueOfIgnoreCase(oAuth2LoginRequest.authVendor()));

        String redirectionDestination = getRedirectionDestination(oAuth2LoginRequest.state());
        log.debug("redirectionDestination={}", redirectionDestination);

        HttpCookieObject refreshTokenCookie = httpCookieUtils.generateHttpOnlyCookie(
                HttpCookieName.REFRESH_TOKEN,
                loginResult.getRefreshToken(),
                REFRESH_TOKEN_COOKIE_MAX_AGE
        );

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.asSetCookieHeaderValue());

        headers.add(
                HttpHeaders.LOCATION, OAuth2WebUtils.buildAfterLoginRedirectionUrl(
                        redirectionDestination, loginResult, oAuth2LoginRequest.authVendor()
                )
        );

        log.info("Redirect URI: {}", redirectUri);

        if (log.isDebugEnabled()) {
            log.debug("code: {}", oAuth2LoginRequest.code());
            log.debug("Auth vendor: {}", oAuth2LoginRequest.authVendor());
            log.debug("state: {}", oAuth2LoginRequest.state());
            log.debug("loginResult={}", loginResult);
            log.debug("refreshTokenCookie={}", refreshTokenCookie.asSetCookieHeaderValue());
        }

        return new OAuth2LoginResponse(headers);
    }

    private String getRedirectionDestination(String client) {
        String destination = OAuth2WebUtils.extractDestination(client);
        if (FormatValidator.hasValue(destination)) {
            log.debug("From query parameter");
            return destination;
        }

        log.debug("Redirect to default origin");

        return clientOrigin;
    }

    public WebBasedTokenRefreshResponse issueNewAccessToken(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new InvalidRefreshTokenException("refresh_token is null");
        }

        GrantedTokenInfo grantedTokenInfo = tokenSupport.refreshToken(refreshToken);
        log.debug("grantedTokenInfo: {}", grantedTokenInfo);
        String newRefreshToken = grantedTokenInfo.refreshToken();
        String newAccessToken = grantedTokenInfo.accessToken();

        TokenClaims claims = jwtUtil.parseRefreshToken(refreshToken);
        long millisLeft = Duration.between(LocalDateTime.now(), claims.getExpirationAt()).toMillis();

        if (millisLeft < ONE_DAY_MILLIS && FormatValidator.hasValue(newRefreshToken)) {
            refreshToken = newRefreshToken;
            Long id = FormatConverter.parseToLong(grantedTokenInfo.id());
            String redisKey = "refreshToken:" + id;
            String refreshTokenValue = "r" + encodeBySha256Hex(refreshToken);
            stringRedisTemplate.opsForValue()
                    .set(Objects.requireNonNull(redisKey), Objects.requireNonNull(refreshTokenValue),
                            Objects.requireNonNull(refreshTokenTtlMillis), TimeUnit.MILLISECONDS);
        }

        HttpCookieObject refreshTokenCookie = httpCookieUtils.generateHttpOnlyCookie(
                HttpCookieName.REFRESH_TOKEN,
                refreshToken,
                REFRESH_TOKEN_COOKIE_MAX_AGE
        );

        log.debug("refreshTokenCookie: {}", refreshTokenCookie.asSetCookieHeaderValue());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.asSetCookieHeaderValue());

        return new WebBasedTokenRefreshResponse(headers, new RefreshedAccessTokenResponse(newAccessToken));
    }

    private String encodeBySha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                String hex = Integer.toHexString(b & 0xff);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new HashAlgorithmNotAvailableException(e);
        }
    }
}
