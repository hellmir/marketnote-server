package com.personal.marketnote.common.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlowRequestLoggingFilterTest {

    /**
     * 임계값 100L: 가드 최소값과 동일. {@link #SLEEP_MS_EXCEEDS} sleep으로 50ms 마진 확보 → 결정론적 임계값 초과.
     * (생성자 가드 {@code thresholdMs < 100 ? 100 : thresholdMs}로 인해 0/음수는 100으로 강제됨)
     */
    private static final long THRESHOLD_GUARDED_MIN = 100L;

    /**
     * 임계값 {@link Long#MAX_VALUE}: {@code TimeUnit.NANOSECONDS.toMillis} 결과 상한 ~9e12로 도달 불가
     * → 어떤 latency도 임계값 미초과 → 결정론적 미초과 보장.
     */
    private static final long THRESHOLD_NEVER_EXCEEDS = Long.MAX_VALUE;

    /**
     * sleep 시간 (ms). 임계값 100ms 대비 50ms 마진 — CI 변동성(보통 ~10ms 이하) 충분히 흡수.
     */
    private static final long SLEEP_MS_EXCEEDS = 150L;

    private Logger logger;
    private ListAppender<ILoggingEvent> appender;
    private boolean previousAdditive;
    private Level previousLevel;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(SlowRequestLoggingFilter.class);
        previousAdditive = logger.isAdditive();
        previousLevel = logger.getLevel();
        logger.setAdditive(false);
        logger.setLevel(Level.WARN);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
        appender.stop();
        logger.setAdditive(previousAdditive);
        logger.setLevel(previousLevel);
    }

    @Test
    @DisplayName("요청 latency가 임계값 이상이면 WARN 로그에 method/path/query/status/elapsedMs를 포함하여 캡처한다")
    void shouldLogWarnWhenLatencyExceedsThreshold() throws ServletException, IOException {
        // given
        SlowRequestLoggingFilter filter = new SlowRequestLoggingFilter(THRESHOLD_GUARDED_MIN);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products/1");
        request.setQueryString("size=20&page=0");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        // when
        filter.doFilter(request, response, sleepingChain(SLEEP_MS_EXCEEDS));

        // then
        assertThat(appender.list).hasSize(1);
        ILoggingEvent event = appender.list.get(0);
        assertThat(event.getLevel()).isEqualTo(Level.WARN);
        Object[] args = event.getArgumentArray();
        assertThat(args).hasSize(5);
        assertThat(args[0]).isEqualTo("GET");
        assertThat(args[1]).isEqualTo("/api/products/1");
        assertThat(args[2]).isEqualTo("size=20&page=0");
        assertThat(args[3]).isEqualTo(200);
        assertThat(args[4]).isInstanceOf(Long.class);
        String message = event.getFormattedMessage();
        assertThat(message).contains("Slow request");
        assertThat(message).contains("method=GET, path=/api/products/1");
        assertThat(message).contains(", query=size=20&page=0,");
        assertThat(message).contains(", status=200,");
        assertThat(message).contains(", elapsedMs=");
    }

    @Test
    @DisplayName("요청 latency가 임계값 미만이면 WARN 로그를 남기지 않는다")
    void shouldNotLogWhenLatencyBelowThreshold() throws ServletException, IOException {
        // given
        SlowRequestLoggingFilter filter = new SlowRequestLoggingFilter(THRESHOLD_NEVER_EXCEEDS);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, new MockFilterChain());

        // then
        assertThat(appender.list).isEmpty();
    }

    @Test
    @DisplayName("필터 체인에서 예외가 발생해도 latency를 측정하여 임계값 초과 시 WARN 로그를 남기고 예외를 재전파한다")
    void shouldLogAndRethrowWhenChainThrows() {
        // given
        SlowRequestLoggingFilter filter = new SlowRequestLoggingFilter(THRESHOLD_GUARDED_MIN);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/orders");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain throwingChain = (req, res) -> {
            sleepQuietly(SLEEP_MS_EXCEEDS);
            throw new ServletException("downstream failure");
        };

        // when / then
        assertThatThrownBy(() -> filter.doFilter(request, response, throwingChain))
            .isInstanceOf(ServletException.class)
            .hasMessageContaining("downstream failure");
        assertThat(appender.list).hasSize(1);
        ILoggingEvent event = appender.list.get(0);
        assertThat(event.getLevel()).isEqualTo(Level.WARN);
        Object[] args = event.getArgumentArray();
        assertThat(args[0]).isEqualTo("POST");
        assertThat(args[1]).isEqualTo("/api/orders");
    }

    @Test
    @DisplayName("query string이 null이면 query=\"\"(빈 문자열)로 출력한다")
    void shouldOutputEmptyQueryWhenQueryStringIsNull() throws ServletException, IOException {
        // given
        SlowRequestLoggingFilter filter = new SlowRequestLoggingFilter(THRESHOLD_GUARDED_MIN);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/me");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, sleepingChain(SLEEP_MS_EXCEEDS));

        // then
        assertThat(appender.list).hasSize(1);
        Object[] args = appender.list.get(0).getArgumentArray();
        assertThat(args[2]).isEqualTo("");
        assertThat(appender.list.get(0).getFormattedMessage()).contains(", query=, status=");
    }

    @Test
    @DisplayName("query string이 공백이면 query=\"\"(빈 문자열)로 출력한다")
    void shouldOutputEmptyQueryWhenQueryStringIsBlank() throws ServletException, IOException {
        // given
        SlowRequestLoggingFilter filter = new SlowRequestLoggingFilter(THRESHOLD_GUARDED_MIN);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/me");
        request.setQueryString("   ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, sleepingChain(SLEEP_MS_EXCEEDS));

        // then
        assertThat(appender.list).hasSize(1);
        Object[] args = appender.list.get(0).getArgumentArray();
        assertThat(args[2]).isEqualTo("");
        assertThat(appender.list.get(0).getFormattedMessage()).contains(", query=, status=");
    }

    @Test
    @DisplayName("query string에 email 파라미터가 있으면 PII 마스킹된 형태(f***@bar.com)로 로그에 출력한다")
    void shouldMaskEmailInQueryString() throws ServletException, IOException {
        // given
        SlowRequestLoggingFilter filter = new SlowRequestLoggingFilter(THRESHOLD_GUARDED_MIN);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/search");
        request.setQueryString("email=foo@bar.com&page=0");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, sleepingChain(SLEEP_MS_EXCEEDS));

        // then
        assertThat(appender.list).hasSize(1);
        Object[] args = appender.list.get(0).getArgumentArray();
        String maskedQuery = (String) args[2];
        assertThat(maskedQuery).doesNotContain("foo@bar.com");
        assertThat(maskedQuery).contains("email=f***@bar.com");
    }

    @Test
    @DisplayName("query string에 token 파라미터가 있으면 시크릿 redact(token=***)로 로그에 출력한다")
    void shouldRedactTokenInQueryString() throws ServletException, IOException {
        // given
        SlowRequestLoggingFilter filter = new SlowRequestLoggingFilter(THRESHOLD_GUARDED_MIN);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/protected");
        request.setQueryString("token=abc123secret&page=1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, sleepingChain(SLEEP_MS_EXCEEDS));

        // then
        assertThat(appender.list).hasSize(1);
        Object[] args = appender.list.get(0).getArgumentArray();
        String maskedQuery = (String) args[2];
        assertThat(maskedQuery).doesNotContain("abc123secret");
        assertThat(maskedQuery).contains("token=***");
        assertThat(maskedQuery).contains("page=1");
    }

    @Test
    @DisplayName("query string에 password 파라미터가 있으면 시크릿 redact(password=***)로 로그에 출력한다")
    void shouldRedactPasswordInQueryString() throws ServletException, IOException {
        // given
        SlowRequestLoggingFilter filter = new SlowRequestLoggingFilter(THRESHOLD_GUARDED_MIN);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/login");
        request.setQueryString("password=plaintextSecret123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, sleepingChain(SLEEP_MS_EXCEEDS));

        // then
        assertThat(appender.list).hasSize(1);
        Object[] args = appender.list.get(0).getArgumentArray();
        String maskedQuery = (String) args[2];
        assertThat(maskedQuery).doesNotContain("plaintextSecret123");
        assertThat(maskedQuery).contains("password=***");
    }

    @Test
    @DisplayName("query string에 Authorization 파라미터(case-insensitive)가 있으면 시크릿 redact(Authorization=***)로 로그에 출력한다")
    void shouldRedactAuthorizationInQueryStringCaseInsensitive() throws ServletException, IOException {
        // given
        SlowRequestLoggingFilter filter = new SlowRequestLoggingFilter(THRESHOLD_GUARDED_MIN);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/me");
        request.setQueryString("Authorization=Bearer xyz");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, sleepingChain(SLEEP_MS_EXCEEDS));

        // then
        assertThat(appender.list).hasSize(1);
        Object[] args = appender.list.get(0).getArgumentArray();
        String maskedQuery = (String) args[2];
        assertThat(maskedQuery).doesNotContain("Bearer xyz");
        assertThat(maskedQuery).contains("Authorization=***");
    }

    @Test
    @DisplayName("민감 키가 없는 query string(page=2&size=10)은 변형 없이 그대로 출력한다")
    void shouldNotMutateNonSensitiveQueryString() throws ServletException, IOException {
        // given
        SlowRequestLoggingFilter filter = new SlowRequestLoggingFilter(THRESHOLD_GUARDED_MIN);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products");
        request.setQueryString("page=2&size=10");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, sleepingChain(SLEEP_MS_EXCEEDS));

        // then
        assertThat(appender.list).hasSize(1);
        Object[] args = appender.list.get(0).getArgumentArray();
        assertThat(args[2]).isEqualTo("page=2&size=10");
    }

    @Test
    @DisplayName("path에 CRLF가 포함되면 '_'로 치환되어 로그 라인 위조를 차단한다")
    void shouldSanitizePathContainingCrlf() throws ServletException, IOException {
        // given
        SlowRequestLoggingFilter filter = new SlowRequestLoggingFilter(THRESHOLD_GUARDED_MIN);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users\r\n[ERROR] forged");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, sleepingChain(SLEEP_MS_EXCEEDS));

        // then
        assertThat(appender.list).hasSize(1);
        Object[] args = appender.list.get(0).getArgumentArray();
        String sanitizedPath = (String) args[1];
        assertThat(sanitizedPath).doesNotContain("\r");
        assertThat(sanitizedPath).doesNotContain("\n");
        assertThat(sanitizedPath).contains("/api/users__[ERROR] forged");
        String message = appender.list.get(0).getFormattedMessage();
        assertThat(message).doesNotContain("\r");
        assertThat(message).doesNotContain("\n");
    }

    @Test
    @DisplayName("query에 CRLF가 포함되면 '_'로 치환되어 로그 라인 위조를 차단한다")
    void shouldSanitizeQueryContainingCrlf() throws ServletException, IOException {
        // given
        SlowRequestLoggingFilter filter = new SlowRequestLoggingFilter(THRESHOLD_GUARDED_MIN);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/search");
        request.setQueryString("key=value\r\nfake=injection");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, sleepingChain(SLEEP_MS_EXCEEDS));

        // then
        assertThat(appender.list).hasSize(1);
        Object[] args = appender.list.get(0).getArgumentArray();
        String sanitizedQuery = (String) args[2];
        assertThat(sanitizedQuery).doesNotContain("\r");
        assertThat(sanitizedQuery).doesNotContain("\n");
        assertThat(sanitizedQuery).contains("key=value__fake=injection");
        String message = appender.list.get(0).getFormattedMessage();
        assertThat(message).doesNotContain("\r");
        assertThat(message).doesNotContain("\n");
    }

    @Test
    @DisplayName("threshold 음수(-1)로 생성하면 최소값 100ms로 강제된다")
    void shouldClampNegativeThresholdToMinimum() {
        // given / when
        SlowRequestLoggingFilter filter = new SlowRequestLoggingFilter(-1L);

        // then
        assertThat(filter.getThresholdMs()).isEqualTo(100L);
    }

    @Test
    @DisplayName("threshold 50ms로 생성하면 최소값 100ms로 강제된다")
    void shouldClampBelowMinimumThresholdToMinimum() {
        // given / when
        SlowRequestLoggingFilter filter = new SlowRequestLoggingFilter(50L);

        // then
        assertThat(filter.getThresholdMs()).isEqualTo(100L);
    }

    @Test
    @DisplayName("threshold 1500ms로 생성하면 가드 영향 없이 1500ms 그대로 사용한다")
    void shouldKeepThresholdAboveMinimum() {
        // given / when
        SlowRequestLoggingFilter filter = new SlowRequestLoggingFilter(1500L);

        // then
        assertThat(filter.getThresholdMs()).isEqualTo(1500L);
    }

    private static FilterChain sleepingChain(long sleepMs) {
        return (req, res) -> sleepQuietly(sleepMs);
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("test sleep interrupted", ie);
        }
    }
}
