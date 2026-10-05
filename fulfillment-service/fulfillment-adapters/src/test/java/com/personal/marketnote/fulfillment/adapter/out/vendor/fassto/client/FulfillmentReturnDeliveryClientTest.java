package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentReturnGodDetailFailedException;
import com.personal.marketnote.fulfillment.exception.RegisterFulfillmentReturnDeliveryFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentReturnGodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentReturnDeliveryItemCommand;
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
@DisplayName("FulfillmentReturnDeliveryClient 테스트")
class FulfillmentReturnDeliveryClientTest {

    private FulfillmentReturnDeliveryClient client;

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

        client = new FulfillmentReturnDeliveryClient(
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

    private void stubReturnDeliveryProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getReturnDeliveryPath()).thenReturn("/return-delivery/{customerCode}");
    }

    private void stubReturnGodDetailProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getReturnGodDetailPath()).thenReturn("/return-god-detail/{customerCode}");
    }

    private RegisterFulfillmentReturnDeliveryCommand buildValidRegisterCommand() {
        RegisterFulfillmentDeliveryGoodsCommand goodsCommand = RegisterFulfillmentDeliveryGoodsCommand.of(
                "PROD001", "20260414", 1
        );
        RegisterFulfillmentReturnDeliveryItemCommand itemCommand = RegisterFulfillmentReturnDeliveryItemCommand.builder()
                .orderDate("20260414")
                .orderNumber("ORD001")
                .courierCode("04")
                .invoiceNumber("1234567890")
                .recipientName("홍길동")
                .recipientPhoneNumber("01012345678")
                .recipientAddress("서울시 강남구")
                .returnType("01")
                .returnReason("01")
                .products(List.of(goodsCommand))
                .build();
        return RegisterFulfillmentReturnDeliveryCommand.of("CUST001", "valid-token", List.of(itemCommand));
    }

    @SuppressWarnings("unchecked")
    private void stubPostChainThrows(Exception exception) {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
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
    @DisplayName("registerReturnDelivery 실패")
    class RegisterReturnDeliveryFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 RegisterFulfillmentReturnDeliveryFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubReturnDeliveryProperties();
            stubPayloadGenerator();
            stubPostChainThrows(new RuntimeException("Connection refused"));

            RegisterFulfillmentReturnDeliveryCommand command = buildValidRegisterCommand();

            // when & then
            assertThatThrownBy(() -> client.registerReturnDelivery(command))
                    .isInstanceOf(RegisterFulfillmentReturnDeliveryFailedException.class);
        }
    }

    @Nested
    @DisplayName("getReturnGodDetail 실패")
    class GetReturnGodDetailFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 GetFulfillmentReturnGodDetailFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubReturnGodDetailProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new RuntimeException("Connection refused"));

            GetFulfillmentReturnGodDetailCommand command = GetFulfillmentReturnGodDetailCommand.of(
                    "CUST001", "valid-token", "20260101", "20260131", null, null
            );

            // when & then
            assertThatThrownBy(() -> client.getReturnGodDetail(command))
                    .isInstanceOf(GetFulfillmentReturnGodDetailFailedException.class);
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

            GetFulfillmentReturnGodDetailCommand command = GetFulfillmentReturnGodDetailCommand.of(
                    "CUST001", "valid-token", "20260101", "20260131", null, null
            );

            // when & then
            assertThatThrownBy(() -> client.getReturnGodDetail(command))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
