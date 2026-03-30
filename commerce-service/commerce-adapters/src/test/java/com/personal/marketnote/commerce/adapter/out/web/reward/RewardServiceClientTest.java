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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
                0.01f,
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
}
