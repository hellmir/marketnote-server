package com.personal.marketnote.common.adapter.in.interception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingInterceptorTest {

    private final LoggingInterceptor loggingInterceptor = new LoggingInterceptor();

    @Test
    @DisplayName("preHandle은 항상 true를 반환하여 요청을 통과시킨다")
    void shouldReturnTrueFromPreHandle() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        boolean result = loggingInterceptor.preHandle(request, response, new Object());

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("preHandle에서 beforeMemory 속성을 request에 설정한다")
    void shouldSetBeforeMemoryAttributeInPreHandle() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        loggingInterceptor.preHandle(request, response, new Object());

        // then
        assertThat(request.getAttribute("beforeMemory")).isNotNull();
        assertThat(request.getAttribute("beforeMemory")).isInstanceOf(Long.class);
    }

    @Test
    @DisplayName("preHandle에서 startedAt 속성을 request에 설정한다")
    void shouldSetStartedAtAttributeInPreHandle() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        loggingInterceptor.preHandle(request, response, new Object());

        // then
        assertThat(request.getAttribute("startedAt")).isNotNull();
        assertThat(request.getAttribute("startedAt")).isInstanceOf(Long.class);
    }

    @Test
    @DisplayName("afterCompletion에서 request 속성을 읽어 로깅을 수행한다")
    void shouldReadAttributesAndLogInAfterCompletion() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        loggingInterceptor.preHandle(request, response, new Object());

        // when & then (예외 없이 정상 실행)
        loggingInterceptor.afterCompletion(request, response, new Object(), null);
    }

    @Test
    @DisplayName("afterCompletion에서 예외 객체가 전달되어도 정상적으로 로깅한다")
    void shouldLogSuccessfullyWhenExceptionIsProvided() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/orders");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(500);

        loggingInterceptor.preHandle(request, response, new Object());
        Exception exception = new RuntimeException("Internal error");

        // when & then (예외 없이 정상 실행)
        loggingInterceptor.afterCompletion(request, response, new Object(), exception);
    }
}
