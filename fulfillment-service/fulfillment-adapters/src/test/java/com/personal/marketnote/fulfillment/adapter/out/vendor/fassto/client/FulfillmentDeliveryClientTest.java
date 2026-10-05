package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentDeliveriesFailedException;
import com.personal.marketnote.fulfillment.exception.RegisterFulfillmentDeliveryFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveriesCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryItemCommand;
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
@DisplayName("FulfillmentDeliveryClient 테스트")
class FulfillmentDeliveryClientTest {

    private FulfillmentDeliveryClient client;

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

        client = new FulfillmentDeliveryClient(
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

    private void stubDeliveryMutationProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getDeliveryPath()).thenReturn("/delivery/{customerCode}");
    }

    private void stubDeliveryListProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getDeliveryListPath()).thenReturn("/delivery/{customerCode}/{startDate}/{endDate}/{status}/{outDiv}");
    }

    private RegisterFulfillmentDeliveryCommand buildValidRegisterCommand() {
        RegisterFulfillmentDeliveryGoodsCommand goodsCommand = RegisterFulfillmentDeliveryGoodsCommand.of(
                "PROD001", "20260414", 1
        );
        RegisterFulfillmentDeliveryItemCommand itemCommand = RegisterFulfillmentDeliveryItemCommand.builder()
                .orderNumber("ORD001")
                .orderDate("20260414")
                .recipientName("홍길동")
                .recipientPhoneNumber("01012345678")
                .recipientAddress("서울시 강남구")
                .releaseMethod("01")
                .products(List.of(goodsCommand))
                .build();
        return RegisterFulfillmentDeliveryCommand.of("CUST001", "valid-token", List.of(itemCommand));
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
    @DisplayName("registerDelivery 실패")
    class RegisterDeliveryFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 RegisterFulfillmentDeliveryFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubDeliveryMutationProperties();
            stubPayloadGenerator();
            stubMethodChainThrows(new RuntimeException("Connection refused"));

            RegisterFulfillmentDeliveryCommand command = buildValidRegisterCommand();

            // when & then
            assertThatThrownBy(() -> client.registerDelivery(command))
                    .isInstanceOf(RegisterFulfillmentDeliveryFailedException.class);
        }
    }

    @Nested
    @DisplayName("getDeliveries 실패")
    class GetDeliveriesFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 GetFulfillmentDeliveriesFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubDeliveryListProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new RuntimeException("Connection refused"));

            GetFulfillmentDeliveriesCommand command = GetFulfillmentDeliveriesCommand.of(
                    "CUST001", "valid-token", "20260101", "20260131", "01", "01", null
            );

            // when & then
            assertThatThrownBy(() -> client.getDeliveries(command))
                    .isInstanceOf(GetFulfillmentDeliveriesFailedException.class);
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

            GetFulfillmentDeliveriesCommand command = GetFulfillmentDeliveriesCommand.of(
                    "CUST001", "valid-token", "20260101", "20260131", "01", "01", null
            );

            // when & then
            assertThatThrownBy(() -> client.getDeliveries(command))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
