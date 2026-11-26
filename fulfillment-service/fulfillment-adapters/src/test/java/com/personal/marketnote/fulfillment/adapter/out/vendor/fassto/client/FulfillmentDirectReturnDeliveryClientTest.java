package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.external.log.ExternalSystem;
import com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentResponseHeader;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.RegisterFulfillmentDeliveryItemResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.RegisterFulfillmentDeliveryResponse;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.exception.FulfillmentConfigurationException;
import com.personal.marketnote.fulfillment.exception.RegisterFulfillmentDirectReturnDeliveryFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDirectReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDirectReturnDeliveryItemCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
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
                null, "PROD001", "PROD001", "20260414", 1
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

    @SuppressWarnings("unchecked")
    private void stubPostChain(ResponseEntity<?> response) {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(java.net.URI.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(response);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
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
    @DisplayName("registerDirectReturnDelivery 성공")
    class RegisterDirectReturnDeliverySuccess {

        @Test
        @DisplayName("정상 응답 시 RegisterFulfillmentDeliveryResult를 반환한다")
        void shouldReturnDeliveryResultOnSuccess() {
            // given
            stubDirectReturnDeliveryProperties();
            stubPayloadGenerator();
            RegisterFulfillmentDeliveryResponse payload = new RegisterFulfillmentDeliveryResponse(
                    new FulfillmentResponseHeader("200", "success", 1),
                    List.of(new RegisterFulfillmentDeliveryItemResponse("FMS001", "ORD001", "OK", "200", null)),
                    null
            );
            stubPostChain(ResponseEntity.ok(toJson(payload)));

            // when
            RegisterFulfillmentDeliveryResult result = client.registerDirectReturnDelivery(buildValidRegisterCommand());

            // then
            assertThat(result.dataCount()).isEqualTo(1);
            assertThat(result.deliveries()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("registerDirectReturnDelivery 실패 추가 분기")
    class RegisterDirectReturnDeliveryExtraFailure {

        @Test
        @DisplayName("4xx 응답 시 RegisterFulfillmentDirectReturnDeliveryFailedException이 발생한다")
        void shouldThrowOn4xx() {
            // given
            stubDirectReturnDeliveryProperties();
            stubPayloadGenerator();
            stubPostChainThrows(new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Bad Request",
                    "client error".getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8
            ));

            // when & then
            assertThatThrownBy(() -> client.registerDirectReturnDelivery(buildValidRegisterCommand()))
                    .isInstanceOf(RegisterFulfillmentDirectReturnDeliveryFailedException.class);
        }

        @Test
        @DisplayName("5xx 응답 시 RegisterFulfillmentDirectReturnDeliveryFailedException이 발생한다")
        void shouldThrowOn5xx() {
            // given
            stubDirectReturnDeliveryProperties();
            stubPayloadGenerator();
            stubPostChainThrows(new HttpServerErrorException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal Server Error",
                    "boom".getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8
            ));

            // when & then
            assertThatThrownBy(() -> client.registerDirectReturnDelivery(buildValidRegisterCommand()))
                    .isInstanceOf(RegisterFulfillmentDirectReturnDeliveryFailedException.class);
        }

        @Test
        @DisplayName("200 응답이지만 header 실패면 RegisterFulfillmentDirectReturnDeliveryFailedException이 발생한다")
        void shouldThrowOnHeaderFailure() {
            // given
            stubDirectReturnDeliveryProperties();
            stubPayloadGenerator();
            RegisterFulfillmentDeliveryResponse payload = new RegisterFulfillmentDeliveryResponse(
                    new FulfillmentResponseHeader("500", "vendor failure", 0),
                    List.of(),
                    null
            );
            stubPostChain(ResponseEntity.ok(toJson(payload)));

            // when & then
            assertThatThrownBy(() -> client.registerDirectReturnDelivery(buildValidRegisterCommand()))
                    .isInstanceOf(RegisterFulfillmentDirectReturnDeliveryFailedException.class);
        }

        @Test
        @DisplayName("200 응답이지만 item.code가 200이 아니면 vendor 메시지를 포함한 RegisterFulfillmentDirectReturnDeliveryFailedException이 발생한다")
        void shouldThrowOnItemCodeFailureWithVendorMessage() {
            // given
            stubDirectReturnDeliveryProperties();
            stubPayloadGenerator();
            RegisterFulfillmentDeliveryResponse payload = new RegisterFulfillmentDeliveryResponse(
                    new FulfillmentResponseHeader("200", "success", 1),
                    List.of(new RegisterFulfillmentDeliveryItemResponse(
                            null, "ORD001", "item-level vendor error", "400", null
                    )),
                    null
            );
            stubPostChain(ResponseEntity.ok(toJson(payload)));

            // when & then
            assertThatThrownBy(() -> client.registerDirectReturnDelivery(buildValidRegisterCommand()))
                    .isInstanceOf(RegisterFulfillmentDirectReturnDeliveryFailedException.class)
                    .hasMessageContaining("item-level vendor error");
        }

        @Test
        @DisplayName("응답 본문이 깨진 JSON이면 재시도 후 RegisterFulfillmentDirectReturnDeliveryFailedException이 발생한다")
        void shouldThrowOnBrokenJson() {
            // given
            stubDirectReturnDeliveryProperties();
            stubPayloadGenerator();
            stubPostChain(ResponseEntity.ok("not-a-json"));

            // when & then
            assertThatThrownBy(() -> client.registerDirectReturnDelivery(buildValidRegisterCommand()))
                    .isInstanceOf(RegisterFulfillmentDirectReturnDeliveryFailedException.class);
        }

        @Test
        @DisplayName("응답 헤더가 INVALID_ACCESS이면 FulfillmentInvalidAccessException이 발생한다")
        void shouldThrowInvalidAccessOnInvalidAccessHeader() {
            // given
            stubDirectReturnDeliveryProperties();
            stubPayloadGenerator();
            RegisterFulfillmentDeliveryResponse payload = new RegisterFulfillmentDeliveryResponse(
                    new FulfillmentResponseHeader("401", "INVALID_ACCESS", 0),
                    List.of(),
                    null
            );
            stubPostChain(ResponseEntity.ok(toJson(payload)));

            // when & then
            assertThatThrownBy(() -> client.registerDirectReturnDelivery(buildValidRegisterCommand()))
                    .isInstanceOf(com.personal.marketnote.fulfillment.exception.FulfillmentInvalidAccessException.class);
        }

        @Test
        @DisplayName("RestClientResponseException 본문에 errorInfo가 있으면 vendor 메시지를 사용한다")
        void shouldUseVendorMessageFromExceptionBody() {
            // given
            stubDirectReturnDeliveryProperties();
            stubPayloadGenerator();
            String errorBody = "{\"errorInfo\":{\"errorCode\":\"E_VENDOR\",\"errorMessage\":\"vendor down\"}}";
            stubPostChainThrows(new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Bad Request",
                    errorBody.getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8
            ));

            // when & then
            assertThatThrownBy(() -> client.registerDirectReturnDelivery(buildValidRegisterCommand()))
                    .isInstanceOf(RegisterFulfillmentDirectReturnDeliveryFailedException.class)
                    .hasMessageContaining("vendor down");
        }
    }

    @Nested
    @DisplayName("설정 검증 추가 분기")
    class PropertyValidationExtra {

        @Test
        @DisplayName("returnDirectDeliveryPath가 누락되면 FulfillmentConfigurationException이 발생한다")
        void shouldThrowWhenPathMissing() {
            // given
            when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
            when(properties.getReturnDirectDeliveryPath()).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> client.registerDirectReturnDelivery(buildValidRegisterCommand()))
                    .isInstanceOf(FulfillmentConfigurationException.class);
        }

        @Test
        @DisplayName("returnDirectDeliveryPath에 customerCode 플레이스홀더가 없으면 FulfillmentConfigurationException이 발생한다")
        void shouldThrowWhenPlaceholderMissing() {
            // given
            when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
            when(properties.getReturnDirectDeliveryPath()).thenReturn("/direct-return-delivery");

            // when & then
            assertThatThrownBy(() -> client.registerDirectReturnDelivery(buildValidRegisterCommand()))
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
