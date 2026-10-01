package com.personal.marketnote.community.adapter.out.web.commerce;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.community.exception.UnauthorizedOrderAccessException;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommerceServiceClient")
class CommerceServiceClientTest {

    private static final String BASE_URL = "http://localhost:8082";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private MockRestServiceServer mockServer;
    private CommerceServiceClient client;

    @Mock
    private HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder;

    @Mock
    private ServiceCommunicationRecorder serviceCommunicationRecorder;

    @Mock
    private ServiceCommunicationPayloadGenerator serviceCommunicationPayloadGenerator;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new CommerceServiceClient(
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
    @DisplayName("verifyOrderOwnership")
    class VerifyOrderOwnership {

        @Test
        @DisplayName("성공 시 통신 기록 없이 정상 종료한다")
        void completesSilentlyOnSuccess() {
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/orders/1/ownership?buyerId=10"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess());

            client.verifyOrderOwnership(1L, 10L);

            mockServer.verify();
            verifyNoInteractions(serviceCommunicationRecorder);
        }

        @Test
        @DisplayName("403 응답 시 UnauthorizedOrderAccessException을 던지고 재시도하지 않는다")
        void throwsUnauthorizedOnForbidden() {
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/orders/1/ownership?buyerId=10"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.FORBIDDEN));

            assertThatThrownBy(() -> client.verifyOrderOwnership(1L, 10L))
                    .isInstanceOf(UnauthorizedOrderAccessException.class);

            mockServer.verify();
            verify(serviceCommunicationRecorder, never()).record(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("4xx(403 외)일 때 HttpClientErrorException을 던지고 재시도하지 않으며 통신 오류를 기록한다")
        void throwsImmediatelyOnNon403ClientError() {
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/orders/1/ownership?buyerId=10"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.BAD_REQUEST));

            assertThatThrownBy(() -> client.verifyOrderOwnership(1L, 10L))
                    .isInstanceOf(HttpClientErrorException.class);

            mockServer.verify();
            verify(serviceCommunicationRecorder, atLeastOnce()).record(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("5xx 응답이 5회 반복되면 마지막 시도 후 예외를 전파한다")
        void retriesUpToFiveAndThrows() {
            for (int i = 0; i < 5; i++) {
                mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/orders/1/ownership?buyerId=10"))
                        .andExpect(method(HttpMethod.GET))
                        .andRespond(withServerError());
            }

            assertThatThrownBy(() -> client.verifyOrderOwnership(1L, 10L))
                    .isInstanceOf(HttpServerErrorException.class);

            mockServer.verify();
            verify(serviceCommunicationRecorder, atLeastOnce()).record(any(), any(), any(), any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("findUnitAmountByOrderIdAndPricePolicyId")
    class FindUnitAmount {

        @Test
        @DisplayName("정상 응답이면 unitAmount를 추출해 반환한다")
        void returnsUnitAmountWhenContentPresent() {
            String body = "{\"content\":{\"unitAmount\":12345}}";
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/orders/1/order-products/2"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

            Optional<Long> result = client.findUnitAmountByOrderIdAndPricePolicyId(1L, 2L);

            assertThat(result).contains(12345L);
        }

        @Test
        @DisplayName("응답에 content가 없으면 빈 Optional을 반환한다")
        void returnsEmptyWhenNoContentField() {
            String body = "{\"other\":\"x\"}";
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/orders/1/order-products/2"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

            assertThat(client.findUnitAmountByOrderIdAndPricePolicyId(1L, 2L)).isEmpty();
        }

        @Test
        @DisplayName("content가 null이거나 unitAmount가 없으면 빈 Optional을 반환한다")
        void returnsEmptyWhenContentNullOrMissing() {
            String body = "{\"content\":{\"otherField\":1}}";
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/orders/1/order-products/2"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

            assertThat(client.findUnitAmountByOrderIdAndPricePolicyId(1L, 2L)).isEmpty();
        }

        @Test
        @DisplayName("서버 에러 발생 시 예외를 삼키고 빈 Optional을 반환하며 오류를 기록한다")
        void returnsEmptyAndRecordsOnError() {
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/orders/1/order-products/2"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withServerError());

            assertThat(client.findUnitAmountByOrderIdAndPricePolicyId(1L, 2L)).isEmpty();
            verify(serviceCommunicationRecorder, atLeastOnce()).record(any(), any(), any(), any(), any(), any(), any());
        }
    }
}
