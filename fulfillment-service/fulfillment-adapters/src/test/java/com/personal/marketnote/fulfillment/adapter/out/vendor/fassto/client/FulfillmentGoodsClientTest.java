package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentGoodsFailedException;
import com.personal.marketnote.fulfillment.exception.RegisterFulfillmentGoodsFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentGoodsItemCommand;
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
@DisplayName("FulfillmentGoodsClient 테스트")
class FulfillmentGoodsClientTest {

    private FulfillmentGoodsClient client;

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

        client = new FulfillmentGoodsClient(
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

    private void stubGoodsProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getGoodsPath()).thenReturn("/goods/{customerCode}");
    }

    private RegisterFulfillmentGoodsCommand buildValidRegisterCommand() {
        RegisterFulfillmentGoodsItemCommand itemCommand = RegisterFulfillmentGoodsItemCommand.builder()
                .productCode("PROD001")
                .productName("테스트 상품")
                .productType("1")
                .giftDivision("01")
                .build();
        return RegisterFulfillmentGoodsCommand.of("CUST001", "valid-token", List.of(itemCommand));
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
    @DisplayName("registerGoods 실패")
    class RegisterGoodsFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 RegisterFulfillmentGoodsFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubGoodsProperties();
            stubPayloadGenerator();
            stubPostChainThrows(new RuntimeException("Connection refused"));

            RegisterFulfillmentGoodsCommand command = buildValidRegisterCommand();

            // when & then
            assertThatThrownBy(() -> client.registerGoods(command))
                    .isInstanceOf(RegisterFulfillmentGoodsFailedException.class);
        }
    }

    @Nested
    @DisplayName("getGoods 실패")
    class GetGoodsFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 GetFulfillmentGoodsFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubGoodsProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new RuntimeException("Connection refused"));

            GetFulfillmentGoodsCommand command = GetFulfillmentGoodsCommand.of("CUST001", "valid-token");

            // when & then
            assertThatThrownBy(() -> client.getGoods(command))
                    .isInstanceOf(GetFulfillmentGoodsFailedException.class);
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

            GetFulfillmentGoodsCommand command = GetFulfillmentGoodsCommand.of("CUST001", "valid-token");

            // when & then
            assertThatThrownBy(() -> client.getGoods(command))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
