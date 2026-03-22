package com.personal.marketnote.common.security.hmac;

import com.personal.marketnote.common.security.hmac.exception.HmacAuthenticationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.util.List;

import static com.personal.marketnote.common.security.hmac.HmacHeaderConstants.*;
import static com.personal.marketnote.common.security.token.utility.TokenConstant.AUTHENTICATION_SCHEME;

@RequiredArgsConstructor
@Slf4j
public class HmacAuthenticationFilter extends OncePerRequestFilter {
    private final String secretKey;
    private final HmacNonceValidator hmacNonceValidator;
    private final Clock clock;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (FormatValidator.hasNoValue(authorization)) {
            return false;
        }
        return authorization.startsWith(AUTHENTICATION_SCHEME);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String signature = request.getHeader(HEADER_SIGNATURE);
        String timestamp = request.getHeader(HEADER_TIMESTAMP);
        String nonce = request.getHeader(HEADER_NONCE);

        if (!hasAnyHmacHeader(signature, timestamp, nonce)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!hasAllHmacHeaders(signature, timestamp, nonce)) {
            throw new HmacAuthenticationFailedException(
                    "HMAC 인증 실패: 필수 헤더가 누락되었습니다. 3개 헤더(X-HMAC-Signature, X-HMAC-Timestamp, X-HMAC-Nonce)를 모두 포함해야 합니다.");
        }

        String httpMethod = request.getMethod();
        String requestPath = request.getRequestURI();

        HmacSignatureValidator.validate(secretKey, timestamp, nonce, httpMethod, requestPath, signature, clock);
        hmacNonceValidator.validateAndStore(nonce);

        PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(
                "SERVICE",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_SERVICE"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private boolean hasAnyHmacHeader(String signature, String timestamp, String nonce) {
        return FormatValidator.hasValue(signature)
                || FormatValidator.hasValue(timestamp)
                || FormatValidator.hasValue(nonce);
    }

    private boolean hasAllHmacHeaders(String signature, String timestamp, String nonce) {
        return FormatValidator.hasValue(signature)
                && FormatValidator.hasValue(timestamp)
                && FormatValidator.hasValue(nonce);
    }
}
