package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentStockDetailFailedException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentStocksFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStockDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStocksCommand;
import com.personal.marketnote.fulfillment.utility.VendorCommunicationFailureHandler;
import com.personal.marketnote.fulfillment.utility.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.fulfillment.utility.VendorCommunicationRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentStockClient 테스트")
class FulfillmentStockClientTest {

    private FulfillmentStockClient client;

    @Mock
    private RestClient restClient;

    @Mock
    private FulfillmentAuthProperties properties;

    @Mock
    private VendorCommunicationRecorder vendorCommunicationRecorder;

    @Mock
    private VendorCommunicationPayloadGenerator vendorCommunicationPayloadGenerator;

    @Mock
    private VendorCommunicationFailureHandler vendorCommunicationFailureHandler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.build()).thenReturn(restClient);

        client = new FulfillmentStockClient(
                builder,
                objectMapper,
                properties,
                vendorCommunicationRecorder,
                vendorCommunicationPayloadGenerator,
                vendorCommunicationFailureHandler
        );
    }

    private void stubPayloadGenerator() {
        lenient().when(vendorCommunicationPayloadGenerator.buildPayloadJson(any()))
                .thenReturn(objectMapper.createObjectNode());
    }

    private void stubStockProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getStockListPath()).thenReturn("/stock/{customerCode}");
    }

    @SuppressWarnings("unchecked")
    private void stubGetChainThrows(Exception exception) {
        RestClient.RequestHeadersUriSpec rawSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headerSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec respSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(rawSpec);
        when(rawSpec.uri(any(java.net.URI.class))).thenReturn(headerSpec);
        when(headerSpec.headers(any())).thenReturn(headerSpec);
        when(headerSpec.retrieve()).thenReturn(respSpec);
        when(respSpec.toEntity(any(Class.class))).thenThrow(exception);
    }

    @Nested
    @DisplayName("getStocks 실패")
    class GetStocksFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 GetFulfillmentStocksFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubStockProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new RuntimeException("Connection refused"));

            GetFulfillmentStocksCommand command = GetFulfillmentStocksCommand.of("CUST001", "valid-token");

            // when & then
            assertThatThrownBy(() -> client.getStocks(command))
                    .isInstanceOf(GetFulfillmentStocksFailedException.class);
        }
    }

    @Nested
    @DisplayName("getStockDetail 실패")
    class GetStockDetailFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 GetFulfillmentStockDetailFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubStockProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new RuntimeException("Connection refused"));

            GetFulfillmentStockDetailCommand command = GetFulfillmentStockDetailCommand.of(
                    "CUST001", "valid-token", "GOD001", null
            );

            // when & then
            assertThatThrownBy(() -> client.getStockDetail(command))
                    .isInstanceOf(GetFulfillmentStockDetailFailedException.class);
        }
    }

    @Nested
    @DisplayName("설정 검증 실패")
    class PropertyValidationFailure {

        @Test
        @DisplayName("baseUrl이 누락되면 IllegalStateException이 발생한다")
        void shouldThrowIllegalStateExceptionWhenBaseUrlMissing() {
            // given
            when(properties.getBaseUrl()).thenReturn(null);

            GetFulfillmentStocksCommand command = GetFulfillmentStocksCommand.of("CUST001", "valid-token");

            // when & then
            assertThatThrownBy(() -> client.getStocks(command))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
