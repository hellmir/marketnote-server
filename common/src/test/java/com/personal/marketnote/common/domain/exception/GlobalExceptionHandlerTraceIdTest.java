package com.personal.marketnote.common.domain.exception;

import com.personal.marketnote.common.logging.TraceIdMdcFilter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler traceId 응답 주입")
class GlobalExceptionHandlerTraceIdTest {

    private final MeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(meterRegistry);

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    @DisplayName("MDC에 traceId가 설정된 상태에서 DomainNotFoundException 핸들러는 응답 body에 동일한 traceId를 포함한다")
    void domainNotFoundIncludesTraceId() {
        String traceId = "trace-1";
        MDC.put(TraceIdMdcFilter.TRACE_ID_MDC_KEY, traceId);

        ResponseEntity<ErrorResponse> response = handler.handleDomainNotFoundException(
                new DomainNotFoundException("ERR_TEST_NF::대상 없음")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTraceId()).isEqualTo(traceId);
    }

    @Test
    @DisplayName("MDC에 traceId가 설정된 상태에서 IllegalArgumentException 핸들러는 응답 body에 동일한 traceId를 포함한다")
    void illegalArgumentIncludesTraceId() {
        String traceId = "trace-2";
        MDC.put(TraceIdMdcFilter.TRACE_ID_MDC_KEY, traceId);

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(
                new IllegalArgumentException("ERR_TEST_BAD::잘못된 인자")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTraceId()).isEqualTo(traceId);
    }

    @Test
    @DisplayName("MDC에 traceId가 설정된 상태에서 MethodArgumentNotValidException 핸들러는 응답 body에 동일한 traceId를 포함한다")
    void methodArgumentNotValidIncludesTraceId() throws NoSuchMethodException {
        String traceId = "trace-3";
        MDC.put(TraceIdMdcFilter.TRACE_ID_MDC_KEY, traceId);

        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "name", "필수입니다"));
        Method method = DummyController.class.getDeclaredMethod("dummy", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValidException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTraceId()).isEqualTo(traceId);
    }

    @Test
    @DisplayName("MDC에 traceId가 설정된 상태에서 AccessDeniedException 핸들러는 응답 body에 동일한 traceId를 포함한다")
    void accessDeniedIncludesTraceId() {
        String traceId = "trace-4";
        MDC.put(TraceIdMdcFilter.TRACE_ID_MDC_KEY, traceId);

        ResponseEntity<ErrorResponse> response = handler.handleAccessDeniedException(
                new AccessDeniedException("denied")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTraceId()).isEqualTo(traceId);
    }

    @Test
    @DisplayName("MDC에 traceId가 설정된 상태에서 generic Exception 핸들러는 응답 body에 동일한 traceId를 포함한다")
    void genericExceptionIncludesTraceId() {
        String traceId = "trace-5";
        MDC.put(TraceIdMdcFilter.TRACE_ID_MDC_KEY, traceId);

        ResponseEntity<ErrorResponse> response = handler.handleException(
                new BadCredentialsException("bad creds wrapped as generic")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTraceId()).isEqualTo(traceId);
    }

    @Test
    @DisplayName("MDC에 traceId가 없을 때 응답 body의 traceId는 null이다")
    void responseTraceIdIsNullWhenMdcEmpty() {
        ResponseEntity<ErrorResponse> response = handler.handleDomainNotFoundException(
                new DomainNotFoundException("ERR_TEST_NF::대상 없음")
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTraceId()).isNull();
    }

    @Test
    @DisplayName("AsyncRequestNotUsableException 핸들러는 본문 없이 noContent를 반환한다 (회귀 방지)")
    void asyncRequestNotUsableReturnsNoContent() {
        MDC.put(TraceIdMdcFilter.TRACE_ID_MDC_KEY, "trace-async");

        ResponseEntity<Void> response = handler.handleAsyncRequestNotUsableException(
                new AsyncRequestNotUsableException("client closed")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    private static class DummyController {
        @SuppressWarnings("unused")
        void dummy(String name) {
        }
    }
}
