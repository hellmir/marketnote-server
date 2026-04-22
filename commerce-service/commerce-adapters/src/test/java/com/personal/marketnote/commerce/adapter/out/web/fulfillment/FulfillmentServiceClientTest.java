package com.personal.marketnote.commerce.adapter.out.web.fulfillment;

import com.personal.marketnote.common.exception.FulfillmentServiceRequestFailedException;
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
@DisplayName("FulfillmentServiceClient 테스트")
class FulfillmentServiceClientTest {

    private static final String BASE_URL = "http://localhost:8083";
    private static final Long ORDER_ID = 100L;

    private MockRestServiceServer mockServer;
    private FulfillmentServiceClient fulfillmentServiceClient;

    @Mock
    private HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        fulfillmentServiceClient = new FulfillmentServiceClient(
                builder,
                BASE_URL,
                hmacServiceAuthHeaderBuilder
        );
    }

    @Nested
    @DisplayName("getWorkStatus")
    class GetWorkStatus {

        @Test
        @DisplayName("풀필먼트 작업 상태 조회에 성공하면 작업 상태를 반환한다")
        void shouldReturnWorkStatusWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/fulfillment/deliveries/work-status?order-id=100"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {
                                "content": {
                                    "orderId": 100,
                                    "workStatus": "REGISTERED"
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            String result = fulfillmentServiceClient.getWorkStatus(ORDER_ID);

            // then
            assertThat(result).isEqualTo("REGISTERED");
            mockServer.verify();
        }

        @Test
        @DisplayName("출고 미접수 상태이면 NOT_REGISTERED를 반환한다")
        void shouldReturnNotRegisteredWhenOrderNotRegistered() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/fulfillment/deliveries/work-status?order-id=100"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {
                                "content": {
                                    "orderId": 100,
                                    "workStatus": "NOT_REGISTERED"
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            String result = fulfillmentServiceClient.getWorkStatus(ORDER_ID);

            // then
            assertThat(result).isEqualTo("NOT_REGISTERED");
            mockServer.verify();
        }

        @Test
        @DisplayName("풀필먼트 서비스 요청이 실패하면 FulfillmentServiceRequestFailedException이 발생한다")
        void shouldThrowExceptionWhenRequestFails() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/fulfillment/deliveries/work-status?order-id=100"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withServerError());

            // when & then
            assertThatThrownBy(() -> fulfillmentServiceClient.getWorkStatus(ORDER_ID))
                    .isInstanceOf(FulfillmentServiceRequestFailedException.class);

            mockServer.verify();
        }
    }
}
