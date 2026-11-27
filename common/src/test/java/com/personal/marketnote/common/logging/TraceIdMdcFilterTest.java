package com.personal.marketnote.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TraceIdMdcFilterTest {

    private TraceIdMdcFilter filter;

    @BeforeEach
    void setUp() {
        filter = new TraceIdMdcFilter();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("X-Request-ID 헤더가 있으면 헤더 값을 MDC와 응답 헤더에 그대로 주입한다")
    void shouldUseHeaderTraceIdWhenProvided() throws ServletException, IOException {
        // given
        String headerTraceId = "client-trace-id-1234";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TraceIdMdcFilter.REQUEST_ID_HEADER, headerTraceId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> capturedMdc = new AtomicReference<>();
        FilterChain chain = (req, res) -> capturedMdc.set(MDC.get(TraceIdMdcFilter.REQUEST_ID_MDC_KEY));

        // when
        filter.doFilter(request, response, chain);

        // then
        assertThat(capturedMdc.get()).isEqualTo(headerTraceId);
        assertThat(response.getHeader(TraceIdMdcFilter.REQUEST_ID_HEADER)).isEqualTo(headerTraceId);
    }

    @Test
    @DisplayName("X-Request-ID 헤더가 없으면 UUID를 발급해 MDC와 응답 헤더에 동일 값으로 주입한다")
    void shouldGenerateUuidWhenHeaderMissing() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> capturedMdc = new AtomicReference<>();
        FilterChain chain = (req, res) -> capturedMdc.set(MDC.get(TraceIdMdcFilter.REQUEST_ID_MDC_KEY));

        // when
        filter.doFilter(request, response, chain);

        // then
        String responseHeader = response.getHeader(TraceIdMdcFilter.REQUEST_ID_HEADER);
        assertThat(responseHeader).isNotBlank();
        assertThat(capturedMdc.get()).isEqualTo(responseHeader);
        assertThatNoExceptionParsingUuid(responseHeader);
    }

    @Test
    @DisplayName("X-Request-ID 헤더가 공백 문자열이면 UUID를 발급한다")
    void shouldGenerateUuidWhenHeaderBlank() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TraceIdMdcFilter.REQUEST_ID_HEADER, "   ");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> capturedMdc = new AtomicReference<>();
        FilterChain chain = (req, res) -> capturedMdc.set(MDC.get(TraceIdMdcFilter.REQUEST_ID_MDC_KEY));

        // when
        filter.doFilter(request, response, chain);

        // then
        String responseHeader = response.getHeader(TraceIdMdcFilter.REQUEST_ID_HEADER);
        assertThat(responseHeader).isNotBlank();
        assertThat(responseHeader.trim()).isNotEmpty();
        assertThat(capturedMdc.get()).isEqualTo(responseHeader);
        assertThatNoExceptionParsingUuid(responseHeader);
    }

    @Test
    @DisplayName("필터 체인 처리 후 MDC 의 requestId 키가 자동 제거된다")
    void shouldClearMdcAfterChain() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        // when
        filter.doFilter(request, response, chain);

        // then
        assertThat(MDC.get(TraceIdMdcFilter.REQUEST_ID_MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("필터 체인에서 예외가 발생해도 MDC requestId 키는 제거된다")
    void shouldClearMdcWhenChainThrows() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain throwingChain = (req, res) -> {
            throw new ServletException("chain failure");
        };

        // when / then
        assertThatThrownBy(() -> filter.doFilter(request, response, throwingChain))
            .isInstanceOf(ServletException.class);
        assertThat(MDC.get(TraceIdMdcFilter.REQUEST_ID_MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("X-Request-ID 헤더에 CRLF 가 포함되면 UUID 로 폴백한다 (log injection / header splitting 방지)")
    void shouldFallbackToUuidWhenHeaderContainsCrlf() throws ServletException, IOException {
        // given
        String malicious = "valid-id\r\nX-Injected: malicious";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TraceIdMdcFilter.REQUEST_ID_HEADER, malicious);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> capturedMdc = new AtomicReference<>();
        FilterChain chain = (req, res) -> capturedMdc.set(MDC.get(TraceIdMdcFilter.REQUEST_ID_MDC_KEY));

        // when
        filter.doFilter(request, response, chain);

        // then
        String responseHeader = response.getHeader(TraceIdMdcFilter.REQUEST_ID_HEADER);
        assertThat(responseHeader).isNotEqualTo(malicious);
        assertThat(responseHeader).doesNotContain("\r").doesNotContain("\n");
        assertThat(capturedMdc.get()).isEqualTo(responseHeader);
        assertThatNoExceptionParsingUuid(responseHeader);
    }

    @Test
    @DisplayName("X-Request-ID 헤더에 컨트롤/특수 문자가 포함되면 UUID 로 폴백한다")
    void shouldFallbackToUuidWhenHeaderContainsControlChars() throws ServletException, IOException {
        // given
        String malicious = "abc[31mXYZ";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TraceIdMdcFilter.REQUEST_ID_HEADER, malicious);
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, new MockFilterChain());

        // then
        String responseHeader = response.getHeader(TraceIdMdcFilter.REQUEST_ID_HEADER);
        assertThat(responseHeader).isNotEqualTo(malicious);
        assertThatNoExceptionParsingUuid(responseHeader);
    }

    @Test
    @DisplayName("X-Request-ID 헤더가 64자를 초과하면 UUID 로 폴백한다 (로그 폭주 방지)")
    void shouldFallbackToUuidWhenHeaderExceedsMaxLength() throws ServletException, IOException {
        // given
        String tooLong = "a".repeat(65);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TraceIdMdcFilter.REQUEST_ID_HEADER, tooLong);
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, new MockFilterChain());

        // then
        String responseHeader = response.getHeader(TraceIdMdcFilter.REQUEST_ID_HEADER);
        assertThat(responseHeader).isNotEqualTo(tooLong);
        assertThat(responseHeader.length()).isLessThanOrEqualTo(64);
        assertThatNoExceptionParsingUuid(responseHeader);
    }

    @Test
    @DisplayName("X-Request-ID 헤더가 안전한 식별자 문자(영숫자/._-) 64자 이하면 그대로 사용한다")
    void shouldAcceptSafeIdentifierHeader() throws ServletException, IOException {
        // given
        String safeId = "req-abc_12345.6789";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TraceIdMdcFilter.REQUEST_ID_HEADER, safeId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> capturedMdc = new AtomicReference<>();
        FilterChain chain = (req, res) -> capturedMdc.set(MDC.get(TraceIdMdcFilter.REQUEST_ID_MDC_KEY));

        // when
        filter.doFilter(request, response, chain);

        // then
        assertThat(response.getHeader(TraceIdMdcFilter.REQUEST_ID_HEADER)).isEqualTo(safeId);
        assertThat(capturedMdc.get()).isEqualTo(safeId);
    }

    @Test
    @DisplayName("X-Correlation-ID 헤더가 있으면 MDC correlationId에 주입된다")
    void shouldUseHeaderCorrelationIdWhenProvided() throws ServletException, IOException {
        // given
        String correlationId = "corr-id-7890";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TraceIdMdcFilter.CORRELATION_ID_HEADER, correlationId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> capturedMdc = new AtomicReference<>();
        FilterChain chain = (req, res) -> capturedMdc.set(MDC.get(TraceIdMdcFilter.CORRELATION_ID_MDC_KEY));

        // when
        filter.doFilter(request, response, chain);

        // then
        assertThat(capturedMdc.get()).isEqualTo(correlationId);
        assertThat(response.getHeader(TraceIdMdcFilter.CORRELATION_ID_HEADER)).isEqualTo(correlationId);
    }

    @Test
    @DisplayName("X-Correlation-ID 헤더가 없으면 requestId 값으로 fallback된다")
    void shouldFallbackToTraceIdWhenCorrelationIdHeaderMissing() throws ServletException, IOException {
        // given
        String traceId = "trace-fallback-1";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TraceIdMdcFilter.REQUEST_ID_HEADER, traceId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> capturedTrace = new AtomicReference<>();
        AtomicReference<String> capturedCorrelation = new AtomicReference<>();
        FilterChain chain = (req, res) -> {
            capturedTrace.set(MDC.get(TraceIdMdcFilter.REQUEST_ID_MDC_KEY));
            capturedCorrelation.set(MDC.get(TraceIdMdcFilter.CORRELATION_ID_MDC_KEY));
        };

        // when
        filter.doFilter(request, response, chain);

        // then
        assertThat(capturedTrace.get()).isEqualTo(traceId);
        assertThat(capturedCorrelation.get()).isEqualTo(traceId);
        assertThat(response.getHeader(TraceIdMdcFilter.CORRELATION_ID_HEADER)).isEqualTo(traceId);
    }

    @Test
    @DisplayName("X-Causation-ID 헤더가 있으면 MDC causationId에 주입된다")
    void shouldUseHeaderCausationIdWhenProvided() throws ServletException, IOException {
        // given
        String causationId = "cause-id-555";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TraceIdMdcFilter.CAUSATION_ID_HEADER, causationId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> capturedMdc = new AtomicReference<>();
        FilterChain chain = (req, res) -> capturedMdc.set(MDC.get(TraceIdMdcFilter.CAUSATION_ID_MDC_KEY));

        // when
        filter.doFilter(request, response, chain);

        // then
        assertThat(capturedMdc.get()).isEqualTo(causationId);
    }

    @Test
    @DisplayName("X-Causation-ID 헤더가 없으면 MDC causationId는 set되지 않는다")
    void shouldNotSetCausationIdWhenHeaderMissing() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> capturedMdc = new AtomicReference<>();
        FilterChain chain = (req, res) -> capturedMdc.set(MDC.get(TraceIdMdcFilter.CAUSATION_ID_MDC_KEY));

        // when
        filter.doFilter(request, response, chain);

        // then
        assertThat(capturedMdc.get()).isNull();
    }

    @Test
    @DisplayName("응답 종료 후 requestId/correlationId/causationId 모든 MDC 키가 remove된다")
    void shouldClearAllMdcKeysAfterChain() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TraceIdMdcFilter.REQUEST_ID_HEADER, "trace-id");
        request.addHeader(TraceIdMdcFilter.CORRELATION_ID_HEADER, "corr-id");
        request.addHeader(TraceIdMdcFilter.CAUSATION_ID_HEADER, "cause-id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, new MockFilterChain());

        // then
        assertThat(MDC.get(TraceIdMdcFilter.REQUEST_ID_MDC_KEY)).isNull();
        assertThat(MDC.get(TraceIdMdcFilter.CORRELATION_ID_MDC_KEY)).isNull();
        assertThat(MDC.get(TraceIdMdcFilter.CAUSATION_ID_MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("응답 헤더에 X-Correlation-ID가 echo된다")
    void shouldEchoCorrelationIdInResponseHeader() throws ServletException, IOException {
        // given
        String correlationId = "echo-corr-id";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TraceIdMdcFilter.CORRELATION_ID_HEADER, correlationId);
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, new MockFilterChain());

        // then
        assertThat(response.getHeader(TraceIdMdcFilter.CORRELATION_ID_HEADER)).isEqualTo(correlationId);
    }

    private static void assertThatNoExceptionParsingUuid(String value) {
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException iae) {
            throw new AssertionError("traceId is not a valid UUID: " + value, iae);
        }
    }
}
