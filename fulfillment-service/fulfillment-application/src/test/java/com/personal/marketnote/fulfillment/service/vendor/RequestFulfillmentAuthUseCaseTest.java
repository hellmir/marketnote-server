package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.port.out.vendor.RequestFulfillmentAuthPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("풀필먼트 인증 요청 테스트")
class RequestFulfillmentAuthUseCaseTest {

    @Mock
    private RequestFulfillmentAuthPort requestFulfillmentAuthPort;

    private RequestFulfillmentAuthService service;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-01-01T11:00:00Z"),
                ZoneOffset.UTC
        );
        service = new RequestFulfillmentAuthService(requestFulfillmentAuthPort, fixedClock);
    }

    @Nested
    @DisplayName("requestAccessToken() 캐시 동작")
    class CacheTest {

        @Test
        @DisplayName("캐시가 비어있으면 Port를 호출하여 토큰을 발급하고 캐시에 저장한다")
        void requestAccessToken_whenCacheEmpty_invokesPortAndCachesToken() {
            FulfillmentAccessToken token = FulfillmentAccessToken.of("token-1", "20270101120000");
            when(requestFulfillmentAuthPort.requestAccessToken()).thenReturn(token);

            FulfillmentAccessToken first = service.requestAccessToken();
            FulfillmentAccessToken second = service.requestAccessToken();

            assertThat(first).isEqualTo(token);
            assertThat(second).isEqualTo(token);
            verify(requestFulfillmentAuthPort, times(1)).requestAccessToken();
        }

        @Test
        @DisplayName("유효한 캐시가 있으면 Port를 호출하지 않고 캐시 토큰을 반환한다")
        void requestAccessToken_whenCacheValid_returnsCachedTokenWithoutPortCall() {
            FulfillmentAccessToken token = FulfillmentAccessToken.of("token-2", "20270101120000");
            when(requestFulfillmentAuthPort.requestAccessToken()).thenReturn(token);

            service.requestAccessToken();
            service.requestAccessToken();
            service.requestAccessToken();
            service.requestAccessToken();

            verify(requestFulfillmentAuthPort, times(1)).requestAccessToken();
        }

        @Test
        @DisplayName("캐시 토큰이 만료되면 Port를 호출하여 재발급한다")
        void requestAccessToken_whenCachedTokenExpired_reissuesViaPort() {
            FulfillmentAccessToken expiredToken = FulfillmentAccessToken.of("expired", "20250101120000");
            FulfillmentAccessToken newToken = FulfillmentAccessToken.of("new-token", "20270101120000");
            when(requestFulfillmentAuthPort.requestAccessToken())
                    .thenReturn(expiredToken)
                    .thenReturn(newToken);

            FulfillmentAccessToken first = service.requestAccessToken();
            FulfillmentAccessToken second = service.requestAccessToken();

            assertThat(first).isEqualTo(expiredToken);
            assertThat(second).isEqualTo(newToken);
            verify(requestFulfillmentAuthPort, times(2)).requestAccessToken();
        }

        @Test
        @DisplayName("캐시 토큰이 만료 buffer 이내이면 선제 갱신한다")
        void requestAccessToken_whenCachedTokenExpiringSoon_reissuesViaPort() {
            Clock clockNearExpiry = Clock.fixed(
                    Instant.parse("2026-01-01T11:59:30Z"),
                    ZoneOffset.UTC
            );
            RequestFulfillmentAuthService nearExpiryService = new RequestFulfillmentAuthService(
                    requestFulfillmentAuthPort,
                    clockNearExpiry
            );
            FulfillmentAccessToken nearExpiryToken = FulfillmentAccessToken.of(
                    "near-expiry",
                    "20260101120000"
            );
            FulfillmentAccessToken refreshedToken = FulfillmentAccessToken.of(
                    "refreshed",
                    "20270101120000"
            );
            when(requestFulfillmentAuthPort.requestAccessToken())
                    .thenReturn(nearExpiryToken)
                    .thenReturn(refreshedToken);

            FulfillmentAccessToken first = nearExpiryService.requestAccessToken();
            FulfillmentAccessToken second = nearExpiryService.requestAccessToken();

            assertThat(first).isEqualTo(nearExpiryToken);
            assertThat(second).isEqualTo(refreshedToken);
            verify(requestFulfillmentAuthPort, times(2)).requestAccessToken();
        }

        @Test
        @DisplayName("동시 다발 요청이 들어와도 Port는 한 번만 호출된다")
        void requestAccessToken_whenConcurrentRequests_invokesPortOnce() throws InterruptedException {
            int threadCount = 16;
            FulfillmentAccessToken token = FulfillmentAccessToken.of("token-shared", "20270101120000");
            AtomicInteger portInvocations = new AtomicInteger(0);
            CountDownLatch portEntered = new CountDownLatch(1);
            CountDownLatch portRelease = new CountDownLatch(1);
            when(requestFulfillmentAuthPort.requestAccessToken()).thenAnswer(invocation -> {
                portInvocations.incrementAndGet();
                portEntered.countDown();
                portRelease.await();
                return token;
            });

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch ready = new CountDownLatch(threadCount);
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threadCount);
            try {
                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        try {
                            ready.countDown();
                            start.await();
                            service.requestAccessToken();
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        } finally {
                            done.countDown();
                        }
                    });
                }
                ready.await();
                start.countDown();
                assertThat(portEntered.await(2, TimeUnit.SECONDS)).isTrue();
                portRelease.countDown();
                assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
            } finally {
                portRelease.countDown();
                executor.shutdownNow();
                executor.awaitTermination(2, TimeUnit.SECONDS);
            }

            assertThat(portInvocations.get()).isEqualTo(1);
            verify(requestFulfillmentAuthPort, times(1)).requestAccessToken();
        }

        @Test
        @DisplayName("invalidateCache() 호출 후에는 다음 요청에서 Port를 다시 호출한다")
        void invalidateCache_afterInvalidation_reissuesViaPort() {
            FulfillmentAccessToken first = FulfillmentAccessToken.of("first", "20270101120000");
            FulfillmentAccessToken second = FulfillmentAccessToken.of("second", "20270101120000");
            when(requestFulfillmentAuthPort.requestAccessToken())
                    .thenReturn(first)
                    .thenReturn(second);

            FulfillmentAccessToken initial = service.requestAccessToken();
            service.invalidateCache();
            FulfillmentAccessToken reissued = service.requestAccessToken();

            assertThat(initial).isEqualTo(first);
            assertThat(reissued).isEqualTo(second);
            verify(requestFulfillmentAuthPort, times(2)).requestAccessToken();
            verifyNoMoreInteractions(requestFulfillmentAuthPort);
        }
    }
}
