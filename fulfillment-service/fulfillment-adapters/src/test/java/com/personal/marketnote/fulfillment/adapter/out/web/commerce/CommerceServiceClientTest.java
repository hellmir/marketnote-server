package com.personal.marketnote.fulfillment.adapter.out.web.commerce;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.exception.CommerceServiceRequestFailedException;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import com.personal.marketnote.fulfillment.port.out.commerce.UpdateCommerceInventoryCommand;
import com.personal.marketnote.fulfillment.port.out.commerce.UpdateCommerceInventoryItemCommand;
import com.personal.marketnote.fulfillment.utility.ServiceCommunicationPayloadGenerator;
import com.personal.marketnote.fulfillment.utility.ServiceCommunicationRecorder;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
@DisplayName("fulfillment CommerceServiceClient 테스트")
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
        RestClient.Builder builder = RestClient.builder();
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
    @DisplayName("updateInventories")
    class UpdateInventories {

        @Test
        @DisplayName("재고 동기화 요청이 성공하면 정상적으로 완료된다")
        void shouldCompleteSuccessfullyWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/inventories/fulfillment/vendors/stocks/sync"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

            UpdateCommerceInventoryCommand command = UpdateCommerceInventoryCommand.of(
                    List.of(UpdateCommerceInventoryItemCommand.of(1L, 100))
            );

            // when
            commerceServiceClient.updateInventories(command);

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("command가 null이면 IllegalArgumentException이 발생한다")
        void shouldThrowExceptionWhenCommandIsNull() {
            assertThatThrownBy(() -> commerceServiceClient.updateInventories(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("모든 재시도가 실패하면 CommerceServiceRequestFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            for (int i = 0; i < 5; i++) {
                mockServer.expect(requestTo(BASE_URL + "/api/v1/internal/inventories/fulfillment/vendors/stocks/sync"))
                        .andExpect(method(HttpMethod.POST))
                        .andRespond(withServerError());
            }

            UpdateCommerceInventoryCommand command = UpdateCommerceInventoryCommand.of(
                    List.of(UpdateCommerceInventoryItemCommand.of(1L, 100))
            );

            // when & then
            assertThatThrownBy(() -> commerceServiceClient.updateInventories(command))
                    .isInstanceOf(CommerceServiceRequestFailedException.class);

            mockServer.verify();
        }
    }
}
