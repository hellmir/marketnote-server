package com.personal.marketnote.commerce.adapter.out.web.product;

import com.personal.marketnote.commerce.port.out.result.shipping.ShippingPolicyInfoResult;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.common.utility.http.client.restclient.RestClientErrorHandler;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class ShippingPolicyServiceClientTest {

    private static final String BASE_URL = "http://localhost:8081";

    private MockRestServiceServer mockServer;
    private ShippingPolicyServiceClient shippingPolicyServiceClient;

    @Mock
    private HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .defaultStatusHandler(RestClientErrorHandler::isError, RestClientErrorHandler.noOpErrorHandler());
        mockServer = MockRestServiceServer.bindTo(builder).build();
        shippingPolicyServiceClient = new ShippingPolicyServiceClient(
                builder,
                BASE_URL,
                hmacServiceAuthHeaderBuilder
        );
    }

    @Nested
    @DisplayName("findBySellerIds")
    class FindBySellerIds {

        @Test
        @DisplayName("판매자 ID 목록으로 배송비 정책 조회에 성공하면 sellerId별 ShippingPolicyInfoResult Map을 반환한다")
        void shouldReturnShippingPolicyMapWhenRequestSucceeds() {
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/shipping-policies/sellers?sellerIds=10&sellerIds=20"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {
                                "content": {
                                    "shippingPolicies": [
                                        {
                                            "sellerId": 10,
                                            "shippingFee": 3000,
                                            "freeShippingThreshold": 20000
                                        },
                                        {
                                            "sellerId": 20,
                                            "shippingFee": 2500,
                                            "freeShippingThreshold": 30000
                                        }
                                    ]
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            Map<Long, ShippingPolicyInfoResult> result = shippingPolicyServiceClient.findBySellerIds(List.of(10L, 20L));

            assertThat(result).hasSize(2);
            assertThat(result.get(10L).shippingFee()).isEqualTo(3000L);
            assertThat(result.get(10L).freeShippingThreshold()).isEqualTo(20000L);
            assertThat(result.get(20L).shippingFee()).isEqualTo(2500L);
            assertThat(result.get(20L).freeShippingThreshold()).isEqualTo(30000L);

            mockServer.verify();
        }

        @Test
        @DisplayName("sellerIds가 null이면 요청 없이 빈 Map을 반환한다")
        void shouldReturnEmptyMapWhenSellerIdsNull() {
            Map<Long, ShippingPolicyInfoResult> result = shippingPolicyServiceClient.findBySellerIds(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("sellerIds가 비어있으면 요청 없이 빈 Map을 반환한다")
        void shouldReturnEmptyMapWhenSellerIdsEmpty() {
            Map<Long, ShippingPolicyInfoResult> result = shippingPolicyServiceClient.findBySellerIds(List.of());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("응답 본문이 빈 경우 빈 Map을 반환한다")
        void shouldReturnEmptyMapWhenResponseBodyIsEmpty() {
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/shipping-policies/sellers?sellerIds=10"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {
                                "content": {
                                    "shippingPolicies": []
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            Map<Long, ShippingPolicyInfoResult> result = shippingPolicyServiceClient.findBySellerIds(List.of(10L));

            assertThat(result).isEmpty();

            mockServer.verify();
        }

        @Test
        @DisplayName("모든 재시도가 실패하면 빈 Map을 반환한다")
        void shouldReturnEmptyMapWhenAllRetriesFail() {
            for (int i = 0; i < 5; i++) {
                mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/shipping-policies/sellers?sellerIds=10"))
                        .andExpect(method(HttpMethod.GET))
                        .andRespond(withServerError());
            }

            Map<Long, ShippingPolicyInfoResult> result = shippingPolicyServiceClient.findBySellerIds(List.of(10L));

            assertThat(result).isEmpty();

            mockServer.verify();
        }
    }
}
