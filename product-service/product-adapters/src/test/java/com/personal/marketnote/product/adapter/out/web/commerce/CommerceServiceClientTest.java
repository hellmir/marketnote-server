package com.personal.marketnote.product.adapter.out.web.commerce;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.exception.CommerceServiceRequestFailedException;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.common.utility.http.client.restclient.RestClientErrorHandler;
import com.personal.marketnote.product.port.out.result.GetInventoryResult;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class CommerceServiceClientTest {

    private static final String BASE_URL = "http://localhost:8083";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private MockRestServiceServer mockServer;
    private CommerceServiceClient commerceServiceClient;

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
        commerceServiceClient = new CommerceServiceClient(
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
    @DisplayName("registerInventory")
    class RegisterInventory {

        @Test
        @DisplayName("재고 등록 요청이 성공하면 정상적으로 완료된다")
        void shouldCompleteSuccessfullyWhenRequestSucceeds() {
            mockServer.expect(requestTo(BASE_URL + "/api/v1/inventories"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andRespond(withSuccess());

            commerceServiceClient.registerInventory(1L, 100L);

            mockServer.verify();
        }

        @Test
        @DisplayName("모든 재시도가 실패하면 CommerceServiceRequestFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            for (int i = 0; i < 5; i++) {
                mockServer.expect(requestTo(BASE_URL + "/api/v1/inventories"))
                        .andExpect(method(HttpMethod.POST))
                        .andRespond(withServerError());
            }

            assertThatThrownBy(() -> commerceServiceClient.registerInventory(1L, 100L))
                    .isInstanceOf(CommerceServiceRequestFailedException.class);

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("findByPricePolicyIds")
    class FindByPricePolicyIds {

        @Test
        @DisplayName("가격정책 ID 목록으로 재고 조회에 성공하면 재고 결과를 반환한다")
        void shouldReturnInventoriesWhenRequestSucceeds() {
            mockServer.expect(requestTo(BASE_URL + "/api/v1/inventories?pricePolicyIds=1&pricePolicyIds=2"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {
                                "content": {
                                    "inventories": [
                                        {"pricePolicyId": 1, "stock": 100},
                                        {"pricePolicyId": 2, "stock": 50}
                                    ]
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            Set<GetInventoryResult> result =
                    commerceServiceClient.findByPricePolicyIds(List.of(1L, 2L));

            assertThat(result).hasSize(2);

            mockServer.verify();
        }

        @Test
        @DisplayName("응답 바디가 null이면 CommerceServiceRequestFailedException 후 폴백 결과를 반환한다")
        void shouldReturnFallbackResultsWhenResponseBodyIsNull() {
            for (int i = 0; i < 5; i++) {
                mockServer.expect(requestTo(BASE_URL + "/api/v1/inventories?pricePolicyIds=1"))
                        .andExpect(method(HttpMethod.GET))
                        .andRespond(withSuccess());
            }

            Set<GetInventoryResult> result =
                    commerceServiceClient.findByPricePolicyIds(List.of(1L));

            assertThat(result).hasSize(1);
            assertThat(result.iterator().next().stock()).isNull();

            mockServer.verify();
        }

        @Test
        @DisplayName("모든 재시도가 실패하면 stock이 null인 폴백 결과를 반환한다")
        void shouldReturnFallbackResultsWhenAllRetriesFail() {
            for (int i = 0; i < 5; i++) {
                mockServer.expect(requestTo(BASE_URL + "/api/v1/inventories?pricePolicyIds=1&pricePolicyIds=2"))
                        .andExpect(method(HttpMethod.GET))
                        .andRespond(withServerError());
            }

            Set<GetInventoryResult> result =
                    commerceServiceClient.findByPricePolicyIds(List.of(1L, 2L));

            assertThat(result).hasSize(2);
            result.forEach(item -> assertThat(item.stock()).isNull());

            mockServer.verify();
        }
    }
}
