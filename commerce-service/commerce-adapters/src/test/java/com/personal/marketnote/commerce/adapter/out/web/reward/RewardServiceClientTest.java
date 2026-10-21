package com.personal.marketnote.commerce.adapter.out.web.reward;

import com.personal.marketnote.common.exception.RewardServiceRequestFailedException;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
@DisplayName("RewardServiceClient 테스트")
class RewardServiceClientTest {

    private static final String BASE_URL = "http://localhost:8085";
    private static final Long USER_ID = 1L;
    private static final Long ORDER_ID = 100L;
    private static final BigDecimal SHARE_POINT_RATE = new BigDecimal("0.1");

    private MockRestServiceServer mockServer;
    private RewardServiceClient rewardServiceClient;

    @Mock
    private HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        rewardServiceClient = new RewardServiceClient(
                builder,
                BASE_URL,
                SHARE_POINT_RATE,
                hmacServiceAuthHeaderBuilder
        );
    }

    private RewardServiceClient buildClient(BigDecimal rate) {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        return new RewardServiceClient(
                builder,
                BASE_URL,
                rate,
                hmacServiceAuthHeaderBuilder
        );
    }

    @Nested
    @DisplayName("getAvailablePoints")
    class GetAvailablePoints {

        @Test
        @DisplayName("포인트 잔액 조회에 성공하면 금액을 반환한다")
        void shouldReturnAmountWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/users/1/points"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {
                                "content": {
                                    "amount": 5000
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            Long result = rewardServiceClient.getAvailablePoints(USER_ID);

            // then
            assertThat(result).isEqualTo(5000L);
            mockServer.verify();
        }

        @Test
        @DisplayName("모든 재시도가 실패하면 RewardServiceRequestFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            for (int i = 0; i < 5; i++) {
                mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/users/1/points"))
                        .andExpect(method(HttpMethod.GET))
                        .andRespond(withServerError());
            }

            // when & then
            assertThatThrownBy(() -> rewardServiceClient.getAvailablePoints(USER_ID))
                    .isInstanceOf(RewardServiceRequestFailedException.class);

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("deductOrderPoints")
    class DeductOrderPoints {

        @Test
        @DisplayName("주문 포인트 차감 요청이 성공하면 정상적으로 완료된다")
        void shouldCompleteSuccessfullyWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/users/1/points"))
                    .andExpect(method(HttpMethod.PATCH))
                    .andRespond(withSuccess());

            // when
            rewardServiceClient.deductOrderPoints(USER_ID, 1000L, ORDER_ID);

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("amount가 0 이하이면 요청을 보내지 않는다")
        void shouldNotSendRequestWhenAmountIsZeroOrLess() {
            rewardServiceClient.deductOrderPoints(USER_ID, 0L, ORDER_ID);

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("refundOrderPoints")
    class RefundOrderPoints {

        @Test
        @DisplayName("주문 포인트 환불 요청이 성공하면 정상적으로 완료된다")
        void shouldCompleteSuccessfullyWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/users/1/points"))
                    .andExpect(method(HttpMethod.PATCH))
                    .andRespond(withSuccess());

            // when
            rewardServiceClient.refundOrderPoints(USER_ID, 1000L, ORDER_ID);

            // then
            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("addPendingProductAccumulationPoints")
    class AddPendingProductAccumulationPoints {

        @Test
        @DisplayName("적립 예정 포인트 추가 요청이 성공하면 정상적으로 완료된다")
        void shouldCompleteSuccessfullyWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/users/1/points/pending"))
                    .andExpect(method(HttpMethod.PATCH))
                    .andRespond(withSuccess());

            // when
            rewardServiceClient.addPendingProductAccumulationPoints(USER_ID, 500L, ORDER_ID);

            // then
            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("confirmPendingPoints")
    class ConfirmPendingPoints {

        @Test
        @DisplayName("적립 예정 포인트 확정 요청이 성공하면 정상적으로 완료된다")
        void shouldCompleteSuccessfullyWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/users/1/points/pending/confirm"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess());

            // when
            rewardServiceClient.confirmPendingPoints(USER_ID, ORDER_ID);

            // then
            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("revokePendingPoints")
    class RevokePendingPoints {

        @Test
        @DisplayName("적립 예정 포인트 취소 요청이 성공하면 정상적으로 완료된다")
        void shouldCompleteSuccessfullyWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/users/1/points/pending/cancel"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess());

            // when
            rewardServiceClient.revokePendingPoints(USER_ID, ORDER_ID);

            // then
            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("addPendingSharedPurchasePoints")
    class AddPendingSharedPurchasePoints {

        private static final UUID SHARER_KEY = UUID.fromString("11111111-1111-1111-1111-111111111111");

        @Test
        @DisplayName("공유 적립 포인트 계산 시 totalAmount=30000, rate=0.1이면 3000원을 적립한다")
        void shouldAccrue3000WhenTotalAmountIs30000AndRateIs0_1() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/users/" + SHARER_KEY + "/points/pending"))
                    .andExpect(method(HttpMethod.PATCH))
                    .andExpect(jsonPath("$.changeType").value("ACCRUAL"))
                    .andExpect(jsonPath("$.amount").value(3000))
                    .andRespond(withSuccess());

            // when
            rewardServiceClient.addPendingSharedPurchasePoints(List.of(SHARER_KEY), 30000L, ORDER_ID);

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("공유 적립 포인트 계산 시 totalAmount=3333, rate=0.1이면 HALF_UP 반올림으로 333원을 적립한다")
        void shouldAccrue333WhenTotalAmountIs3333AndRateIs0_1() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/users/" + SHARER_KEY + "/points/pending"))
                    .andExpect(method(HttpMethod.PATCH))
                    .andExpect(jsonPath("$.amount").value(333))
                    .andRespond(withSuccess());

            // when
            rewardServiceClient.addPendingSharedPurchasePoints(List.of(SHARER_KEY), 3333L, ORDER_ID);

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("float 이진 오차 regression: totalAmount=16777225, rate=0.1이면 BigDecimal HALF_UP으로 1677723원을 적립한다")
        void shouldAccrueBigDecimalHalfUpResultWhenFloatWouldProduceOffByOne() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/users/" + SHARER_KEY + "/points/pending"))
                    .andExpect(method(HttpMethod.PATCH))
                    .andExpect(jsonPath("$.amount").value(1677723))
                    .andRespond(withSuccess());

            // when
            rewardServiceClient.addPendingSharedPurchasePoints(List.of(SHARER_KEY), 16777225L, ORDER_ID);

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("totalAmount가 0 이하면 요청을 보내지 않는다")
        void shouldNotSendRequestWhenTotalAmountIsZeroOrLess() {
            // when
            rewardServiceClient.addPendingSharedPurchasePoints(List.of(SHARER_KEY), 0L, ORDER_ID);

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("sharerKeys가 비어 있으면 요청을 보내지 않는다")
        void shouldNotSendRequestWhenSharerKeysIsEmpty() {
            // when
            rewardServiceClient.addPendingSharedPurchasePoints(List.of(), 30000L, ORDER_ID);

            // then
            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("reducePartialPendingSharedPurchasePoints")
    class ReducePartialPendingSharedPurchasePoints {

        private static final UUID SHARER_KEY = UUID.fromString("22222222-2222-2222-2222-222222222222");

        @Test
        @DisplayName("부분 취소 비율 계산 시 paymentAmount=30000, cancelAmount=30000이면 적립(3000)과 대칭으로 3000원을 차감한다")
        void shouldDeduct3000WhenFullyCancelledAndRateIs0_1() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/users/" + SHARER_KEY + "/points/pending"))
                    .andExpect(method(HttpMethod.PATCH))
                    .andExpect(jsonPath("$.changeType").value("DEDUCTION"))
                    .andExpect(jsonPath("$.amount").value(3000))
                    .andRespond(withSuccess());

            // when
            rewardServiceClient.reducePartialPendingSharedPurchasePoints(
                    List.of(SHARER_KEY), 30000L, 30000L, ORDER_ID
            );

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("부분 취소 비율 계산 시 paymentAmount=30000, cancelAmount=15000이면 절반인 1500원을 차감한다")
        void shouldDeduct1500WhenHalfCancelledAndRateIs0_1() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/users/" + SHARER_KEY + "/points/pending"))
                    .andExpect(method(HttpMethod.PATCH))
                    .andExpect(jsonPath("$.amount").value(1500))
                    .andRespond(withSuccess());

            // when
            rewardServiceClient.reducePartialPendingSharedPurchasePoints(
                    List.of(SHARER_KEY), 30000L, 15000L, ORDER_ID
            );

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("부분 취소 비율 계산 시 paymentAmount=3333, cancelAmount=1111이면 HALF_UP 반올림으로 111원을 차감한다")
        void shouldDeduct111WhenPartiallyCancelledWithRounding() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/users/" + SHARER_KEY + "/points/pending"))
                    .andExpect(method(HttpMethod.PATCH))
                    .andExpect(jsonPath("$.amount").value(111))
                    .andRespond(withSuccess());

            // when
            rewardServiceClient.reducePartialPendingSharedPurchasePoints(
                    List.of(SHARER_KEY), 3333L, 1111L, ORDER_ID
            );

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("cancelAmount가 0이면 요청을 보내지 않는다")
        void shouldNotSendRequestWhenCancelAmountIsZero() {
            // when
            rewardServiceClient.reducePartialPendingSharedPurchasePoints(
                    List.of(SHARER_KEY), 30000L, 0L, ORDER_ID
            );

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("paymentAmount가 0이면 요청을 보내지 않는다")
        void shouldNotSendRequestWhenPaymentAmountIsZero() {
            // when
            rewardServiceClient.reducePartialPendingSharedPurchasePoints(
                    List.of(SHARER_KEY), 0L, 10000L, ORDER_ID
            );

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("원본 적립 포인트가 0이 되는 매우 작은 paymentAmount면 요청을 보내지 않는다")
        void shouldNotSendRequestWhenOriginalSharePointIsZero() {
            // given
            RewardServiceClient client = buildClient(new BigDecimal("0.1"));

            // when
            client.reducePartialPendingSharedPurchasePoints(
                    List.of(SHARER_KEY), 4L, 4L, ORDER_ID
            );

            // then
            mockServer.verify();
        }
    }
}
