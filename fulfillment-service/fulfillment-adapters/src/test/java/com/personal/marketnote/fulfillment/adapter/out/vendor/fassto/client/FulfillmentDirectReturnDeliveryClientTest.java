package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.external.log.ExternalSystem;
import com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.exception.FulfillmentConfigurationException;
import com.personal.marketnote.fulfillment.exception.RegisterFulfillmentDirectReturnDeliveryFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDirectReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDirectReturnDeliveryItemCommand;
import com.personal.marketnote.fulfillment.utility.NoOpSleeper;
import com.personal.marketnote.fulfillment.utility.VendorCommunicationFailureHandler;
import com.personal.marketnote.fulfillment.utility.VendorCommunicationRecorder;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentDirectReturnDeliveryClient 테스트")
class FulfillmentDirectReturnDeliveryClientTest {

    private FulfillmentDirectReturnDeliveryClient client;

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
    private ExternalCallLogger externalCallLogger;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.build()).thenReturn(restClient);

        externalCallLogger = spy(new ExternalCallLogger(new SimpleMeterRegistry()));

        client = new FulfillmentDirectReturnDeliveryClient(
                builder,
                objectMapper,
                properties,
                vendorCommunicationRecorder,
                vendorCommunicationPayloadGenerator,
                vendorCommunicationFailureHandler,
                externalCallLogger,
                new NoOpSleeper()
        );
    }

    private void stubPayloadGenerator() {
        lenient().when(vendorCommunicationPayloadGenerator.buildPayloadJson(any()))
                .thenReturn(objectMapper.createObjectNode());
    }

    private void stubDirectReturnDeliveryProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getReturnDirectDeliveryPath()).thenReturn("/direct-return-delivery/{customerCode}");
    }

    private RegisterFulfillmentDirectReturnDeliveryCommand buildValidRegisterCommand() {
        RegisterFulfillmentDeliveryGoodsCommand goodsCommand = RegisterFulfillmentDeliveryGoodsCommand.of(
                "PROD001", "20260414", 1
        );
        RegisterFulfillmentDirectReturnDeliveryItemCommand itemCommand = RegisterFulfillmentDirectReturnDeliveryItemCommand.builder()
                .orderDate("20260414")
                .supplierCode("SUP001")
                .originalCourierCode("04")
                .originalInvoiceNumber("1234567890")
                .returnReceiveMethod("01")
                .recipientName("홍길동")
                .returnType("01")
                .returnReason("01")
                .products(List.of(goodsCommand))
                .build();
        return RegisterFulfillmentDirectReturnDeliveryCommand.of("CUST001", "valid-token", List.of(itemCommand));
    }

    @SuppressWarnings("unchecked")
    private void stubPostChainThrows(Exception exception) {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(java.net.URI.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(Class.class))).thenThrow(exception);
    }

    @Nested
    @DisplayName("registerDirectReturnDelivery 실패")
    class RegisterDirectReturnDeliveryFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 RegisterFulfillmentDirectReturnDeliveryFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubDirectReturnDeliveryProperties();
            stubPayloadGenerator();
            stubPostChainThrows(new ResourceAccessException("Connection refused"));

            RegisterFulfillmentDirectReturnDeliveryCommand command = buildValidRegisterCommand();

            // when & then
            assertThatThrownBy(() -> client.registerDirectReturnDelivery(command))
                    .isInstanceOf(RegisterFulfillmentDirectReturnDeliveryFailedException.class);
        }
    }

    @Nested
    @DisplayName("설정 검증 실패")
    class PropertyValidationFailure {

        @Test
        @DisplayName("baseUrl이 누락되면 FulfillmentConfigurationException이 발생한다")
        void shouldThrowFulfillmentConfigurationExceptionWhenBaseUrlMissing() {
            // given
            when(properties.getBaseUrl()).thenReturn(null);

            RegisterFulfillmentDirectReturnDeliveryCommand command = buildValidRegisterCommand();

            // when & then
            assertThatThrownBy(() -> client.registerDirectReturnDelivery(command))
                    .isInstanceOf(FulfillmentConfigurationException.class);
        }
    }

    @Nested
    @DisplayName("sensitive path 분기")
    class SensitivePathDispatch {

        @Test
        @DisplayName("sensitivePaths에 등록된 returnDirectDelivery 경로는 recordSensitive로 위임된다")
        void shouldDispatchRecordSensitiveWhenPathIsSensitive() {
            // given
            stubDirectReturnDeliveryProperties();
            stubPayloadGenerator();
            String path = "/direct-return-delivery/{customerCode}";
            when(properties.getSensitivePaths()).thenReturn(Set.of(path));
            stubPostChainThrows(new ResourceAccessException("Connection refused"));

            // when
            assertThatThrownBy(() -> client.registerDirectReturnDelivery(buildValidRegisterCommand()))
                    .isInstanceOf(RegisterFulfillmentDirectReturnDeliveryFailedException.class);

            // then
            verify(externalCallLogger).recordSensitive(
                    eq(ExternalSystem.FASSTO), eq("registerDirectReturnDelivery"), any(Map.class), any(Supplier.class)
            );
            verify(externalCallLogger, never()).record(
                    eq(ExternalSystem.FASSTO), eq("registerDirectReturnDelivery"), any(Map.class), any(Supplier.class)
            );
        }

        @Test
        @DisplayName("sensitivePaths에 미등록된 returnDirectDelivery 경로는 record로 위임된다")
        void shouldDispatchRecordWhenPathIsNotSensitive() {
            // given
            stubDirectReturnDeliveryProperties();
            stubPayloadGenerator();
            when(properties.getSensitivePaths()).thenReturn(Set.of());
            stubPostChainThrows(new ResourceAccessException("Connection refused"));

            // when
            assertThatThrownBy(() -> client.registerDirectReturnDelivery(buildValidRegisterCommand()))
                    .isInstanceOf(RegisterFulfillmentDirectReturnDeliveryFailedException.class);

            // then
            verify(externalCallLogger).record(
                    eq(ExternalSystem.FASSTO), eq("registerDirectReturnDelivery"), any(Map.class), any(Supplier.class)
            );
            verify(externalCallLogger, never()).recordSensitive(
                    eq(ExternalSystem.FASSTO), eq("registerDirectReturnDelivery"), any(Map.class), any(Supplier.class)
            );
        }
    }
}
