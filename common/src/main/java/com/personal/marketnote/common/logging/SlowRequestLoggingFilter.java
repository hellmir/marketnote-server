package com.personal.marketnote.common.logging;

import com.personal.marketnote.common.utility.FormatValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * HTTP 요청 latency가 임계값을 초과하면 WARN 레벨로 자동 캡처한다.
 *
 * <p>임계값 기본 1000ms (환경변수 {@code SLOW_REQUEST_THRESHOLD_MS} / property {@code slow-request.threshold-ms}).
 * prod/qa 모두 활성. WARN 레벨이라 운영 noise 거의 없음.
 *
 * <p>{@link TraceIdMdcFilter}({@link Ordered#HIGHEST_PRECEDENCE}) 직후에 실행되어 traceId가
 * MDC에 이미 주입된 상태에서 측정한다.
 *
 * <p>query string은 {@link PiiMaskingConverter}로 PII(email/phone/nickname/name)를 마스킹하고,
 * 시크릿 키({@link #SECRET_KEYS})는 값을 {@code ***}로 redact 한다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class SlowRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SlowRequestLoggingFilter.class);

    private static final Set<String> SECRET_KEYS = Set.of(
            "token", "password", "apikey", "api_key", "secret", "authorization", "auth"
    );
    private static final Pattern SECRET_KEY_PATTERN = Pattern.compile(
            "(?i)(" + String.join("|", SECRET_KEYS) + ")=[^&]*"
    );

    /**
     * CRLF/탭/제어문자 — 로그 라인 위조(log injection / log forging) 방지용.
     * URL 디코딩 후 path/query/method에 포함될 수 있으므로 출력 직전 강제 치환.
     */
    private static final Pattern CRLF_PATTERN = Pattern.compile("[\\r\\n\\t\\u0000-\\u001F\\u007F]");

    /**
     * thresholdMs 최소값. 음수/0 설정 시 모든 요청이 WARN 폭증하므로 강제 가드.
     */
    private static final long MIN_THRESHOLD_MS = 100L;

    private final long thresholdMs;

    public SlowRequestLoggingFilter(@Value("${slow-request.threshold-ms:1000}") long thresholdMs) {
        this.thresholdMs = thresholdMs < MIN_THRESHOLD_MS ? MIN_THRESHOLD_MS : thresholdMs;
    }

    public long getThresholdMs() {
        return thresholdMs;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain
    ) throws ServletException, IOException {
        long startNanos = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            if (elapsedMs >= thresholdMs) {
                log.warn(
                        "Slow request: method={}, path={}, query={}, status={}, elapsedMs={}",
                        sanitizeForLog(request.getMethod()),
                        sanitizeForLog(request.getRequestURI()),
                        sanitizeQuery(request.getQueryString()),
                        response.getStatus(),
                        elapsedMs
                );
            }
        }
    }

    private static String sanitizeQuery(String query) {
        if (FormatValidator.hasNoValue(query)) {
            return "";
        }
        // PII 마스킹 (email/phone/nickname/name) + 시크릿 키 redact + CRLF 차단
        String masked = PiiMaskingConverter.mask(query);
        String redacted = SECRET_KEY_PATTERN.matcher(masked).replaceAll("$1=***");
        return CRLF_PATTERN.matcher(redacted).replaceAll("_");
    }

    private static String sanitizeForLog(String value) {
        if (FormatValidator.hasNoValue(value)) {
            return "";
        }
        return CRLF_PATTERN.matcher(value).replaceAll("_");
    }
}
