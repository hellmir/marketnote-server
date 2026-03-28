package com.personal.marketnote.community.adapter.out.web.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.common.utility.http.client.restclient.RestClientErrorHandler;
import com.personal.marketnote.community.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.community.utility.ServiceCommunicationPayloadGenerator;
import com.personal.marketnote.community.utility.ServiceCommunicationRecorder;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class ProductServiceClientTest {

    private static final String BASE_URL = "http://localhost:8081";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private MockRestServiceServer mockServer;
    private ProductServiceClient productServiceClient;

    @Mock
    private HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder;

    @Mock
    private ServiceCommunicationRecorder serviceCommunicationRecorder;

    @Mock
    private ServiceCommunicationPayloadGenerator serviceCommunicationPayloadGenerator;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .defaultStatusHandler(RestClientErrorHandler::isError, RestClientErrorHandler.noOpErrorHandler());
        mockServer = MockRestServiceServer.bindTo(builder).build();
        productServiceClient = new ProductServiceClient(
                builder,
                BASE_URL,
                hmacServiceAuthHeaderBuilder,
                serviceCommunicationRecorder,
                serviceCommunicationPayloadGenerator
        );

        lenient().when(serviceCommunicationPayloadGenerator.buildRequestPayloadJson(any(), any(), any(), anyInt()))
                .thenReturn(objectMapper.createObjectNode());
        lenient().when(serviceCommunicationPayloadGenerator.buildErrorPayloadJson(anyString(), any(), anyInt()))
                .thenReturn(objectMapper.createObjectNode());
    }

    @Nested
    @DisplayName("findByPricePolicyIds")
    class FindByPricePolicyIds {

        @Test
        @DisplayName("상품 정보 조회에 성공하면 가격정책ID별 ProductInfoResult Map을 반환한다")
        void shouldReturnProductInfoMapWhenRequestSucceeds() {
            mockServer.expect(requestTo(BASE_URL + "/api/v1/products?pricePolicyIds=1&pricePolicyIds=2&pageSize=2"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {
                                "content": {
                                    "products": {
                                        "items": [
                                            {
                                                "sellerId": 10,
                                                "name": "테스트 상품",
                                                "brandName": "테스트 브랜드",
                                                "pricePolicy": {
                                                    "id": 1,
                                                    "price": 10000,
                                                    "discountPrice": 8000,
                                                    "discountRate": 20.0,
                                                    "accumulatedPoint": 100
                                                },
                                                "selectedOptions": [],
                                                "catalogImage": null
                                            }
                                        ],
                                        "nextCursor": null
                                    }
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            Map<Long, ProductInfoResult> result = productServiceClient.findByPricePolicyIds(List.of(1L, 2L));

            assertThat(result).hasSize(1);
            assertThat(result.get(1L).name()).isEqualTo("테스트 상품");
            assertThat(result.get(1L).sellerId()).isEqualTo(10L);

            mockServer.verify();
        }

        @Test
        @DisplayName("응답에 상품이 없으면 빈 Map을 반환한다")
        void shouldReturnEmptyMapWhenNoProducts() {
            mockServer.expect(requestTo(BASE_URL + "/api/v1/products?pricePolicyIds=1&pageSize=1"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {"content": {"products": {"items": [], "nextCursor": null}}}
                            """, MediaType.APPLICATION_JSON));

            Map<Long, ProductInfoResult> result = productServiceClient.findByPricePolicyIds(List.of(1L));

            assertThat(result).isEmpty();

            mockServer.verify();
        }

        @Test
        @DisplayName("pricePolicyIds가 비어있으면 요청 없이 빈 Map을 반환한다")
        void shouldReturnEmptyMapWhenPricePolicyIdsEmpty() {
            Map<Long, ProductInfoResult> result = productServiceClient.findByPricePolicyIds(List.of());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("pricePolicyIds가 null이면 요청 없이 빈 Map을 반환한다")
        void shouldReturnEmptyMapWhenPricePolicyIdsNull() {
            Map<Long, ProductInfoResult> result = productServiceClient.findByPricePolicyIds(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("모든 재시도가 실패하면 빈 Map을 반환한다")
        void shouldReturnEmptyMapWhenAllRetriesFail() {
            for (int i = 0; i < 5; i++) {
                mockServer.expect(requestTo(BASE_URL + "/api/v1/products?pricePolicyIds=1&pageSize=1"))
                        .andExpect(method(HttpMethod.GET))
                        .andRespond(withServerError());
            }

            Map<Long, ProductInfoResult> result = productServiceClient.findByPricePolicyIds(List.of(1L));

            assertThat(result).isEmpty();

            mockServer.verify();
        }
    }
}
