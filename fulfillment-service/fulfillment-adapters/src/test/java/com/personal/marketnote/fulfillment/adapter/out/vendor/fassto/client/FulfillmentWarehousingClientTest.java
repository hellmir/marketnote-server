package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentWarehousingFailedException;
import com.personal.marketnote.fulfillment.exception.RegisterFulfillmentWarehousingFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentWarehousingCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentWarehousingGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentWarehousingItemCommand;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentWarehousingClient 테스트")
class FulfillmentWarehousingClientTest {

    private FulfillmentWarehousingClient client;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

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

        client = new FulfillmentWarehousingClient(
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

    private void stubWarehousingMutationProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getWarehousingPath()).thenReturn("/warehousing/{customerCode}");
    }

    private void stubWarehousingListProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getWarehousingListPath()).thenReturn("/warehousing/{customerCode}/{startDate}/{endDate}");
    }

    private RegisterFulfillmentWarehousingCommand buildValidRegisterCommand() {
        RegisterFulfillmentWarehousingGoodsCommand goodsCommand = RegisterFulfillmentWarehousingGoodsCommand.of(
                "PROD001", null, 10
        );
        RegisterFulfillmentWarehousingItemCommand itemCommand = RegisterFulfillmentWarehousingItemCommand.builder()
                .orderDate("20260414")
                .orderNumber("ORD001")
                .warehousingMethod("01")
                .supplierCode("SUP001")
                .products(List.of(goodsCommand))
                .build();
        return RegisterFulfillmentWarehousingCommand.of("CUST001", "valid-token", List.of(itemCommand));
    }

    @SuppressWarnings("unchecked")
    private void stubMethodChainThrows(Exception exception) {
        when(restClient.method(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(java.net.URI.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(Class.class))).thenThrow(exception);
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
    @DisplayName("registerWarehousing 실패")
    class RegisterWarehousingFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 RegisterFulfillmentWarehousingFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubWarehousingMutationProperties();
            stubPayloadGenerator();
            stubMethodChainThrows(new RuntimeException("Connection refused"));

            RegisterFulfillmentWarehousingCommand command = buildValidRegisterCommand();

            // when & then
            assertThatThrownBy(() -> client.registerWarehousing(command))
                    .isInstanceOf(RegisterFulfillmentWarehousingFailedException.class);
        }
    }

    @Nested
    @DisplayName("getWarehousing 실패")
    class GetWarehousingFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 GetFulfillmentWarehousingFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubWarehousingListProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new RuntimeException("Connection refused"));

            GetFulfillmentWarehousingCommand command = GetFulfillmentWarehousingCommand.of(
                    "CUST001", "valid-token", "20260101", "20260131"
            );

            // when & then
            assertThatThrownBy(() -> client.getWarehousing(command))
                    .isInstanceOf(GetFulfillmentWarehousingFailedException.class);
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

            GetFulfillmentWarehousingCommand command = GetFulfillmentWarehousingCommand.of(
                    "CUST001", "valid-token", "20260101", "20260131"
            );

            // when & then
            assertThatThrownBy(() -> client.getWarehousing(command))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
