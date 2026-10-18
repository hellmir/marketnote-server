package com.personal.marketnote.fulfillment.utility;

import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.exception.FulfillmentInvalidAccessException;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RequestFulfillmentAuthUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentAuthenticatedCaller 테스트")
class FulfillmentAuthenticatedCallerTest {

    @Mock
    private RequestFulfillmentAuthUseCase requestFulfillmentAuthUseCase;

    @InjectMocks
    private FulfillmentAuthenticatedCaller fulfillmentAuthenticatedCaller;

    private FulfillmentAccessToken firstToken;
    private FulfillmentAccessToken reissuedToken;

    @BeforeEach
    void setUp() {
        firstToken = FulfillmentAccessToken.of("first-token", "20270101120000");
        reissuedToken = FulfillmentAccessToken.of("reissued-token", "20270101120000");
    }

    @Test
    @DisplayName("첫 호출이 성공하면 invalidateCache 호출 없이 결과를 반환한다")
    void call_whenFirstCallSucceeds_returnsResultWithoutInvalidate() {
        when(requestFulfillmentAuthUseCase.requestAccessToken()).thenReturn(firstToken);

        String result = fulfillmentAuthenticatedCaller.call(token -> "success:" + token);

        assertThat(result).isEqualTo("success:first-token");
        verify(requestFulfillmentAuthUseCase, times(1)).requestAccessToken();
        verify(requestFulfillmentAuthUseCase, never()).invalidateCache();
    }

    @Test
    @DisplayName("첫 호출이 INVALID_ACCESS면 invalidateCache 후 재발급된 토큰으로 1회 재시도한다")
    void call_whenFirstCallInvalidAccess_invalidatesCacheAndRetriesOnce() {
        when(requestFulfillmentAuthUseCase.requestAccessToken())
                .thenReturn(firstToken)
                .thenReturn(reissuedToken);

        AtomicInteger invocationCount = new AtomicInteger(0);
        Function<String, String> operation = token -> {
            if (invocationCount.incrementAndGet() == 1) {
                throw new FulfillmentInvalidAccessException();
            }
            return "retried:" + token;
        };

        String result = fulfillmentAuthenticatedCaller.call(operation);

        assertThat(result).isEqualTo("retried:reissued-token");
        assertThat(invocationCount.get()).isEqualTo(2);
        InOrder inOrder = Mockito.inOrder(requestFulfillmentAuthUseCase);
        inOrder.verify(requestFulfillmentAuthUseCase).requestAccessToken();
        inOrder.verify(requestFulfillmentAuthUseCase).invalidateCache();
        inOrder.verify(requestFulfillmentAuthUseCase).requestAccessToken();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("재시도에서도 INVALID_ACCESS면 FulfillmentInvalidAccessException을 그대로 throw한다")
    void call_whenRetryAlsoInvalidAccess_rethrowsException() {
        when(requestFulfillmentAuthUseCase.requestAccessToken())
                .thenReturn(firstToken)
                .thenReturn(reissuedToken);

        AtomicInteger invocationCount = new AtomicInteger(0);
        FulfillmentInvalidAccessException retryFailure = new FulfillmentInvalidAccessException();
        Function<String, String> operation = token -> {
            int count = invocationCount.incrementAndGet();
            if (count == 1) {
                throw new FulfillmentInvalidAccessException();
            }
            throw retryFailure;
        };

        assertThatThrownBy(() -> fulfillmentAuthenticatedCaller.call(operation))
                .isSameAs(retryFailure);

        assertThat(invocationCount.get()).isEqualTo(2);
        verify(requestFulfillmentAuthUseCase, times(2)).requestAccessToken();
        verify(requestFulfillmentAuthUseCase, times(1)).invalidateCache();
        verifyNoMoreInteractions(requestFulfillmentAuthUseCase);
    }

    @Test
    @DisplayName("INVALID_ACCESS 외의 예외는 재시도 없이 그대로 전파된다")
    void call_whenOtherException_propagatesWithoutRetry() {
        when(requestFulfillmentAuthUseCase.requestAccessToken()).thenReturn(firstToken);

        RuntimeException unexpected = new RuntimeException("boom");
        Function<String, String> operation = token -> {
            throw unexpected;
        };

        assertThatThrownBy(() -> fulfillmentAuthenticatedCaller.call(operation))
                .isSameAs(unexpected);

        verify(requestFulfillmentAuthUseCase, times(1)).requestAccessToken();
        verify(requestFulfillmentAuthUseCase, never()).invalidateCache();
    }

    @Test
    @DisplayName("null 반환 연산도 정상 처리된다")
    void call_whenOperationReturnsNull_returnsNull() {
        when(requestFulfillmentAuthUseCase.requestAccessToken()).thenReturn(firstToken);

        String result = fulfillmentAuthenticatedCaller.call(token -> null);

        assertThat(result).isNull();
        verify(requestFulfillmentAuthUseCase, times(1)).requestAccessToken();
        verify(requestFulfillmentAuthUseCase, never()).invalidateCache();
    }

    @Test
    @DisplayName("연산 인자로 현재 유효한 토큰 값이 전달된다")
    void call_passesCurrentTokenValueToOperation() {
        when(requestFulfillmentAuthUseCase.requestAccessToken()).thenReturn(firstToken);

        fulfillmentAuthenticatedCaller.call(token -> {
            assertThat(token).isEqualTo("first-token");
            return null;
        });

        verify(requestFulfillmentAuthUseCase, times(1)).requestAccessToken();
        verify(requestFulfillmentAuthUseCase, never()).invalidateCache();
    }

    @Test
    @DisplayName("재시도 시 연산에 새 토큰 값이 전달된다")
    void call_retriesOperationWithNewTokenValue() {
        when(requestFulfillmentAuthUseCase.requestAccessToken())
                .thenReturn(firstToken)
                .thenReturn(reissuedToken);

        AtomicInteger invocationCount = new AtomicInteger(0);
        Function<String, String> operation = token -> {
            int count = invocationCount.incrementAndGet();
            if (count == 1) {
                assertThat(token).isEqualTo("first-token");
                throw new FulfillmentInvalidAccessException();
            }
            assertThat(token).isEqualTo("reissued-token");
            return "ok";
        };

        String result = fulfillmentAuthenticatedCaller.call(operation);
        assertThat(result).isEqualTo("ok");
        verify(requestFulfillmentAuthUseCase, times(2)).requestAccessToken();
    }
}
