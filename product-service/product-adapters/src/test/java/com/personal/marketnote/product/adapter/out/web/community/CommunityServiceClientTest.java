package com.personal.marketnote.product.adapter.out.web.community;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.common.utility.http.client.restclient.RestClientErrorHandler;
import com.personal.marketnote.product.port.out.result.ProductReviewAggregateResult;
import com.personal.marketnote.product.utility.ServiceCommunicationPayloadGenerator;
import com.personal.marketnote.product.utility.ServiceCommunicationRecorder;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class CommunityServiceClientTest {

    private static final String BASE_URL = "http://localhost:8082";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private MockRestServiceServer mockServer;
    private CommunityServiceClient communityServiceClient;

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
        communityServiceClient = new CommunityServiceClient(
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
    @DisplayName("findByProductIds")
    class FindByProductIds {

        @Test
        @DisplayName("상품 ID 목록으로 리뷰 집계 조회에 성공하면 productId별 결과를 반환한다")
        void shouldReturnReviewAggregatesWhenRequestSucceeds() {
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/products/review-aggregates?productIds=1&productIds=2"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {
                                "content": {
                                    "reviewAggregates": [
                                        {"productId": 1, "totalCount": 10, "averageRating": 4.5},
                                        {"productId": 2, "totalCount": 5, "averageRating": 3.0}
                                    ]
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            Map<Long, ProductReviewAggregateResult> result =
                    communityServiceClient.findByProductIds(List.of(1L, 2L));

            assertThat(result).hasSize(2);
            assertThat(result.get(1L).totalCount()).isEqualTo(10);
            assertThat(result.get(1L).averageRating()).isEqualTo(4.5f);
            assertThat(result.get(2L).totalCount()).isEqualTo(5);
            assertThat(result.get(2L).averageRating()).isEqualTo(3.0f);

            mockServer.verify();
        }

        @Test
        @DisplayName("상품 ID 목록이 null이면 빈 Map을 반환한다")
        void shouldReturnEmptyMapWhenProductIdsIsNull() {
            Map<Long, ProductReviewAggregateResult> result =
                    communityServiceClient.findByProductIds(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("상품 ID 목록이 비어있으면 빈 Map을 반환한다")
        void shouldReturnEmptyMapWhenProductIdsIsEmpty() {
            Map<Long, ProductReviewAggregateResult> result =
                    communityServiceClient.findByProductIds(List.of());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("응답 바디가 null이면 빈 Map을 반환한다")
        void shouldReturnEmptyMapWhenResponseBodyIsNull() {
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/products/review-aggregates?productIds=1"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess());

            Map<Long, ProductReviewAggregateResult> result =
                    communityServiceClient.findByProductIds(List.of(1L));

            assertThat(result).isEmpty();

            mockServer.verify();
        }

        @Test
        @DisplayName("reviewAggregates가 null이면 빈 Map을 반환한다")
        void shouldReturnEmptyMapWhenReviewAggregatesIsNull() {
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/products/review-aggregates?productIds=1"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {"content": {"reviewAggregates": null}}
                            """, MediaType.APPLICATION_JSON));

            Map<Long, ProductReviewAggregateResult> result =
                    communityServiceClient.findByProductIds(List.of(1L));

            assertThat(result).isEmpty();

            mockServer.verify();
        }

        @Test
        @DisplayName("모든 재시도가 실패하면 빈 Map을 반환한다")
        void shouldReturnEmptyMapWhenAllRetriesFail() {
            for (int i = 0; i < 5; i++) {
                mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/products/review-aggregates?productIds=1"))
                        .andExpect(method(HttpMethod.GET))
                        .andRespond(withServerError());
            }

            Map<Long, ProductReviewAggregateResult> result =
                    communityServiceClient.findByProductIds(List.of(1L));

            assertThat(result).isEmpty();

            mockServer.verify();
        }
    }
}
