package com.personal.marketnote.common.application.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.CodeSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

    @InjectMocks
    private LoggingAspect loggingAspect;

    @Test
    @DisplayName("민감하지 않은 파라미터는 원본 값이 로그에 포함된다")
    void shouldLogOriginalValueForNonSensitiveParam() throws Throwable {
        // given
        ProceedingJoinPoint joinPoint = createJoinPoint(
                new String[]{"name", "age"},
                new Object[]{"홍길동", 25},
                "result"
        );

        // when
        Object result = loggingAspect.serviceLogAroundForStringValue(joinPoint);

        // then
        assertThat(result).isEqualTo("result");
    }

    @Test
    @DisplayName("password 파라미터는 마스킹 처리된다")
    void shouldMaskPasswordParameter() throws Throwable {
        // given
        ProceedingJoinPoint joinPoint = createJoinPoint(
                new String[]{"username", "password"},
                new Object[]{"user1", "secret123"},
                "ok"
        );

        // when
        Object result = loggingAspect.serviceLogAroundForStringValue(joinPoint);

        // then
        assertThat(result).isEqualTo("ok");
    }

    @Test
    @DisplayName("token 파라미터는 마스킹 처리된다")
    void shouldMaskTokenParameter() throws Throwable {
        // given
        ProceedingJoinPoint joinPoint = createJoinPoint(
                new String[]{"accessToken"},
                new Object[]{"eyJhbGciOiJIUzI1NiJ9"},
                "ok"
        );

        // when
        Object result = loggingAspect.serviceLogAroundForStringValue(joinPoint);

        // then
        assertThat(result).isEqualTo("ok");
    }

    @Test
    @DisplayName("apiKey 파라미터는 마스킹 처리된다")
    void shouldMaskApiKeyParameter() throws Throwable {
        // given
        ProceedingJoinPoint joinPoint = createJoinPoint(
                new String[]{"apiKey"},
                new Object[]{"sk-abc123"},
                "ok"
        );

        // when
        Object result = loggingAspect.serviceLogAroundForStringValue(joinPoint);

        // then
        assertThat(result).isEqualTo("ok");
    }

    @Test
    @DisplayName("credential 파라미터는 마스킹 처리된다")
    void shouldMaskCredentialParameter() throws Throwable {
        // given
        ProceedingJoinPoint joinPoint = createJoinPoint(
                new String[]{"userCredential"},
                new Object[]{"cred-value"},
                "ok"
        );

        // when
        Object result = loggingAspect.serviceLogAroundForStringValue(joinPoint);

        // then
        assertThat(result).isEqualTo("ok");
    }

    @Test
    @DisplayName("authorization 파라미터는 마스킹 처리된다")
    void shouldMaskAuthorizationParameter() throws Throwable {
        // given
        ProceedingJoinPoint joinPoint = createJoinPoint(
                new String[]{"authorization"},
                new Object[]{"Bearer token123"},
                "ok"
        );

        // when
        Object result = loggingAspect.serviceLogAroundForStringValue(joinPoint);

        // then
        assertThat(result).isEqualTo("ok");
    }

    @Test
    @DisplayName("파라미터가 없는 메서드도 정상적으로 로깅된다")
    void shouldLogSuccessfullyWhenNoParameters() throws Throwable {
        // given
        ProceedingJoinPoint joinPoint = createJoinPoint(
                new String[]{},
                new Object[]{},
                "empty"
        );

        // when
        Object result = loggingAspect.serviceLogAroundForStringValue(joinPoint);

        // then
        assertThat(result).isEqualTo("empty");
    }

    @Test
    @DisplayName("joinPoint 실행 중 예외 발생 시 예외를 다시 던진다")
    void shouldRethrowExceptionFromJoinPoint() throws Throwable {
        // given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        CodeSignature codeSignature = mock(CodeSignature.class);

        when(joinPoint.getSignature()).thenReturn(codeSignature);
        when(codeSignature.getName()).thenReturn("testMethod");
        when(codeSignature.getDeclaringType()).thenReturn(LoggingAspectTest.class);
        when(codeSignature.getParameterNames()).thenReturn(new String[]{});
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.proceed()).thenThrow(new RuntimeException("execution failed"));

        // when & then
        assertThatThrownBy(() -> loggingAspect.serviceLogAroundForStringValue(joinPoint))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("execution failed");
    }

    @Test
    @DisplayName("clientLogAroundForStringValue도 동일한 로깅을 수행한다")
    void shouldPerformLoggingForClientPointcut() throws Throwable {
        // given
        ProceedingJoinPoint joinPoint = createJoinPoint(
                new String[]{"url"},
                new Object[]{"http://api.example.com"},
                "response"
        );

        // when
        Object result = loggingAspect.clientLogAroundForStringValue(joinPoint);

        // then
        assertThat(result).isEqualTo("response");
    }

    private ProceedingJoinPoint createJoinPoint(String[] paramNames, Object[] paramValues, Object returnValue) throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        CodeSignature codeSignature = mock(CodeSignature.class);

        when(joinPoint.getSignature()).thenReturn(codeSignature);
        when(codeSignature.getName()).thenReturn("testMethod");
        when(codeSignature.getDeclaringType()).thenReturn(LoggingAspectTest.class);
        when(codeSignature.getParameterNames()).thenReturn(paramNames);
        when(joinPoint.getArgs()).thenReturn(paramValues);
        when(joinPoint.proceed()).thenReturn(returnValue);

        return joinPoint;
    }
}
