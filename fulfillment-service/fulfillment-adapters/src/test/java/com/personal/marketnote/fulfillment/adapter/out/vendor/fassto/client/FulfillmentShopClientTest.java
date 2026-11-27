package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.external.log.ExternalSystem;
import com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.*;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.exception.FulfillmentConfigurationException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentShopsFailedException;
import com.personal.marketnote.fulfillment.exception.RegisterFulfillmentShopFailedException;
import com.personal.marketnote.fulfillment.exception.UpdateFulfillmentShopFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentShopsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentShopCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentShopCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentShopsResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentShopResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentShopResult;
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
import org.springframework.http.HttpMethod;
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
@DisplayName("FulfillmentShopClient 테스트")
class FulfillmentShopClientTest {

    private FulfillmentShopClient client;

    @Mock
    private RestClient restClient;

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

        client = new FulfillmentShopClient(
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

    private void stubShopProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getShopPath()).thenReturn("/marketnote/{customerCode}");
    }

    @SuppressWarnings("unchecked")
    private void stubPatchChainThrows(Exception exception) {
        RestClient.RequestBodyUriSpec methodSpec = mock(RestClient.RequestBodyUriSpec.class);
        when(restClient.method(HttpMethod.PATCH)).thenReturn(methodSpec);
        when(methodSpec.uri(any(java.net.URI.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
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

    @SuppressWarnings("unchecked")
    private void stubGetChain(ResponseEntity<?> response) {
        RestClient.RequestHeadersUriSpec rawSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headerSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec respSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(rawSpec);
        when(rawSpec.uri(any(java.net.URI.class))).thenReturn(headerSpec);
        when(headerSpec.headers(any())).thenReturn(headerSpec);
        when(headerSpec.retrieve()).thenReturn(respSpec);
        when(respSpec.toEntity(any(Class.class))).thenReturn(response);
    }

    @SuppressWarnings("unchecked")
    private void stubPatchChain(ResponseEntity<?> response) {
        RestClient.RequestBodyUriSpec methodSpec = mock(RestClient.RequestBodyUriSpec.class);
        when(restClient.method(HttpMethod.PATCH)).thenReturn(methodSpec);
        when(methodSpec.uri(any(java.net.URI.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(response);
    }

    private RegisterFulfillmentShopCommand buildValidRegisterCommand() {
        return RegisterFulfillmentShopCommand.builder()
            .customerCode("CUST001")
            .accessToken("valid-token")
            .shopName("테스트 매장")
            .customerShopCode("SHOP001")
            .dealStartDate("20260101")
            .dealEndDate("20261231")
            .zipCode("12345")
            .address1("서울시")
            .address2("강남구")
            .ceoName("홍길동")
            .businessNumber("123-45-67890")
            .phoneNumber("02-1234-5678")
            .unloadMethod("01")
            .inspectionMethod("01")
            .standbyYn("Y")
            .formType("01")
            .managerName("김철수")
            .managerPosition("과장")
            .managerPhoneNumber("010-1234-5678")
            .useYn("Y")
            .build();
    }

    private UpdateFulfillmentShopCommand buildValidUpdateCommand() {
        return UpdateFulfillmentShopCommand.builder()
            .customerCode("CUST001")
            .accessToken("valid-token")
            .shopCode("SHOP001")
            .shopName("테스트 매장")
            .customerShopCode("SHOP001")
            .dealStartDate("20260101")
            .dealEndDate("20261231")
            .zipCode("12345")
            .address1("서울시")
            .address2("강남구")
            .ceoName("홍길동")
            .businessNumber("123-45-67890")
            .phoneNumber("02-1234-5678")
            .unloadMethod("01")
            .inspectionMethod("01")
            .standbyYn("Y")
            .formType("01")
            .managerName("김철수")
            .managerPosition("과장")
            .managerPhoneNumber("010-1234-5678")
            .useYn("Y")
            .build();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Nested
    @DisplayName("registerShop 실패")
    class RegisterShopFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 RegisterFulfillmentShopFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubShopProperties();
            stubPayloadGenerator();
            stubPatchChainThrows(new ResourceAccessException("Connection refused"));

            RegisterFulfillmentShopCommand command = RegisterFulfillmentShopCommand.builder()
                .customerCode("CUST001")
                .accessToken("valid-token")
                .shopName("테스트 매장")
                .customerShopCode("SHOP001")
                .dealStartDate("20260101")
                .dealEndDate("20261231")
                .zipCode("12345")
                .address1("서울시")
                .address2("강남구")
                .ceoName("홍길동")
                .businessNumber("123-45-67890")
                .phoneNumber("02-1234-5678")
                .unloadMethod("01")
                .inspectionMethod("01")
                .standbyYn("Y")
                .formType("01")
                .managerName("김철수")
                .managerPosition("과장")
                .managerPhoneNumber("010-1234-5678")
                .useYn("Y")
                .build();

            // when & then
            assertThatThrownBy(() -> client.registerShop(command))
                .isInstanceOf(RegisterFulfillmentShopFailedException.class);
        }
    }

    @Nested
    @DisplayName("getShops 실패")
    class GetShopsFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 GetFulfillmentShopsFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubShopProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new ResourceAccessException("Connection refused"));

            GetFulfillmentShopsCommand command = GetFulfillmentShopsCommand.of("CUST001", "valid-token");

            // when & then
            assertThatThrownBy(() -> client.getShops(command))
                .isInstanceOf(GetFulfillmentShopsFailedException.class);
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

            GetFulfillmentShopsCommand command = GetFulfillmentShopsCommand.of("CUST001", "valid-token");

            // when & then
            assertThatThrownBy(() -> client.getShops(command))
                .isInstanceOf(FulfillmentConfigurationException.class);
        }
    }

    @Nested
    @DisplayName("registerShop 성공")
    class RegisterShopSuccess {

        @Test
        @DisplayName("정상 응답 시 RegisterFulfillmentShopResult를 반환한다")
        void shouldReturnRegisterResultOnSuccessResponse() {
            // given
            stubShopProperties();
            stubPayloadGenerator();
            RegisterFulfillmentShopResponse payload = new RegisterFulfillmentShopResponse(
                new FulfillmentResponseHeader("200", "success", 1),
                new RegisterFulfillmentShopDataResponse("OK", "200", "SHOP001"),
                null
            );
            stubPatchChain(ResponseEntity.ok(toJson(payload)));

            // when
            RegisterFulfillmentShopResult result = client.registerShop(buildValidRegisterCommand());

            // then
            assertThat(result.code()).isEqualTo("200");
            assertThat(result.shopCode()).isEqualTo("SHOP001");
        }
    }

    @Nested
    @DisplayName("updateShop 성공/실패")
    class UpdateShopTest {

        @Test
        @DisplayName("정상 응답 시 UpdateFulfillmentShopResult를 반환한다")
        void shouldReturnUpdateResultOnSuccessResponse() {
            // given
            stubShopProperties();
            stubPayloadGenerator();
            RegisterFulfillmentShopResponse payload = new RegisterFulfillmentShopResponse(
                new FulfillmentResponseHeader("200", "success", 1),
                new RegisterFulfillmentShopDataResponse("OK", "200", "SHOP001"),
                null
            );
            stubPatchChain(ResponseEntity.ok(toJson(payload)));

            // when
            UpdateFulfillmentShopResult result = client.updateShop(buildValidUpdateCommand());

            // then
            assertThat(result.shopCode()).isEqualTo("SHOP001");
        }

        @Test
        @DisplayName("4xx 응답 시 UpdateFulfillmentShopFailedException이 발생한다")
        void shouldThrowOn4xx() {
            // given
            stubShopProperties();
            stubPayloadGenerator();
            stubPatchChainThrows(new HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "client error".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
            ));

            // when & then
            assertThatThrownBy(() -> client.updateShop(buildValidUpdateCommand()))
                .isInstanceOf(UpdateFulfillmentShopFailedException.class);
        }
    }

    @Nested
    @DisplayName("getShops 성공")
    class GetShopsSuccess {

        @Test
        @DisplayName("정상 응답 시 GetFulfillmentShopsResult를 반환한다")
        void shouldReturnShopsResultOnSuccessResponse() {
            // given
            stubShopProperties();
            stubPayloadGenerator();
            FulfillmentShopItemResponse item = new FulfillmentShopItemResponse(
                "SHOP001", "테스트 매장", "SHOPCUST", "CUST001", "Cust", "01",
                "20260101", "20261231", "12345", "서울시", "강남구",
                "홍길동", "123-45-67890", "02-1234-5678",
                "01", "01", "Y", "01",
                "김철수", "과장", "010-1234-5678", "Y"
            );
            FulfillmentShopsResponse payload = new FulfillmentShopsResponse(
                new FulfillmentResponseHeader("200", "success", 1),
                List.of(item),
                null
            );
            stubGetChain(ResponseEntity.ok(toJson(payload)));

            GetFulfillmentShopsCommand command = GetFulfillmentShopsCommand.of("CUST001", "valid-token");

            // when
            GetFulfillmentShopsResult result = client.getShops(command);

            // then
            assertThat(result.dataCount()).isEqualTo(1);
            assertThat(result.shops()).hasSize(1);
            assertThat(result.shops().get(0).shopCode()).isEqualTo("SHOP001");
        }
    }

    @Nested
    @DisplayName("registerShop 실패 추가 분기")
    class RegisterShopExtraFailure {

        @Test
        @DisplayName("4xx 응답 시 RegisterFulfillmentShopFailedException이 발생한다")
        void shouldThrowOn4xx() {
            // given
            stubShopProperties();
            stubPayloadGenerator();
            stubPatchChainThrows(new HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "client error".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
            ));

            // when & then
            assertThatThrownBy(() -> client.registerShop(buildValidRegisterCommand()))
                .isInstanceOf(RegisterFulfillmentShopFailedException.class);
        }

        @Test
        @DisplayName("5xx 응답 시 RegisterFulfillmentShopFailedException이 발생한다")
        void shouldThrowOn5xx() {
            // given
            stubShopProperties();
            stubPayloadGenerator();
            stubPatchChainThrows(new HttpServerErrorException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "server boom".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
            ));

            // when & then
            assertThatThrownBy(() -> client.registerShop(buildValidRegisterCommand()))
                .isInstanceOf(RegisterFulfillmentShopFailedException.class);
        }

        @Test
        @DisplayName("200 응답이지만 header 실패면 RegisterFulfillmentShopFailedException이 발생한다")
        void shouldThrowOnHeaderFailure() {
            // given
            stubShopProperties();
            stubPayloadGenerator();
            RegisterFulfillmentShopResponse payload = new RegisterFulfillmentShopResponse(
                new FulfillmentResponseHeader("500", "vendor failure", 0),
                null,
                null
            );
            stubPatchChain(ResponseEntity.ok(toJson(payload)));

            // when & then
            assertThatThrownBy(() -> client.registerShop(buildValidRegisterCommand()))
                .isInstanceOf(RegisterFulfillmentShopFailedException.class);
        }

        @Test
        @DisplayName("200 응답이지만 data.code가 200이 아니면 RegisterFulfillmentShopFailedException이 발생한다")
        void shouldThrowOnDataCodeFailure() {
            // given
            stubShopProperties();
            stubPayloadGenerator();
            RegisterFulfillmentShopResponse payload = new RegisterFulfillmentShopResponse(
                new FulfillmentResponseHeader("200", "success", 1),
                new RegisterFulfillmentShopDataResponse("vendor data error", "400", null),
                null
            );
            stubPatchChain(ResponseEntity.ok(toJson(payload)));

            // when & then
            assertThatThrownBy(() -> client.registerShop(buildValidRegisterCommand()))
                .isInstanceOf(RegisterFulfillmentShopFailedException.class)
                .hasMessageContaining("vendor data error");
        }

        @Test
        @DisplayName("응답 본문이 깨진 JSON이면 재시도 후 RegisterFulfillmentShopFailedException이 발생한다")
        void shouldThrowOnBrokenJson() {
            // given
            stubShopProperties();
            stubPayloadGenerator();
            stubPatchChain(ResponseEntity.ok("not-a-json"));

            // when & then
            assertThatThrownBy(() -> client.registerShop(buildValidRegisterCommand()))
                .isInstanceOf(RegisterFulfillmentShopFailedException.class);
        }
    }

    @Nested
    @DisplayName("getShops 실패 추가 분기")
    class GetShopsExtraFailure {

        @Test
        @DisplayName("4xx 응답 시 GetFulfillmentShopsFailedException이 발생한다")
        void shouldThrowOn4xx() {
            // given
            stubShopProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "client error".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
            ));

            GetFulfillmentShopsCommand command = GetFulfillmentShopsCommand.of("CUST001", "valid-token");

            // when & then
            assertThatThrownBy(() -> client.getShops(command))
                .isInstanceOf(GetFulfillmentShopsFailedException.class);
        }

        @Test
        @DisplayName("200 응답이지만 header 실패 + errorInfo 메시지 존재 시 vendor 메시지를 포함한 GetFulfillmentShopsFailedException이 발생한다")
        void shouldIncludeVendorMessageOnHeaderFailureWithErrorInfo() {
            // given
            stubShopProperties();
            stubPayloadGenerator();
            FulfillmentShopsResponse payload = new FulfillmentShopsResponse(
                new FulfillmentResponseHeader("500", "fail", 0),
                List.of(),
                new FulfillmentErrorInfo("E_INTERNAL", "vendor outage", null)
            );
            stubGetChain(ResponseEntity.ok(toJson(payload)));

            GetFulfillmentShopsCommand command = GetFulfillmentShopsCommand.of("CUST001", "valid-token");

            // when & then
            assertThatThrownBy(() -> client.getShops(command))
                .isInstanceOf(GetFulfillmentShopsFailedException.class)
                .hasMessageContaining("vendor outage");
        }

        @Test
        @DisplayName("응답 본문이 깨진 JSON이면 재시도 후 GetFulfillmentShopsFailedException이 발생한다")
        void shouldThrowOnBrokenJson() {
            // given
            stubShopProperties();
            stubPayloadGenerator();
            stubGetChain(ResponseEntity.ok("not-a-json"));

            GetFulfillmentShopsCommand command = GetFulfillmentShopsCommand.of("CUST001", "valid-token");

            // when & then
            assertThatThrownBy(() -> client.getShops(command))
                .isInstanceOf(GetFulfillmentShopsFailedException.class);
        }

        @Test
        @DisplayName("응답 헤더가 INVALID_ACCESS이면 FulfillmentInvalidAccessException이 발생한다")
        void shouldThrowInvalidAccessOnInvalidAccessHeader() {
            // given
            stubShopProperties();
            stubPayloadGenerator();
            FulfillmentShopsResponse payload = new FulfillmentShopsResponse(
                new FulfillmentResponseHeader("401", "INVALID_ACCESS", 0),
                List.of(),
                null
            );
            stubGetChain(ResponseEntity.ok(toJson(payload)));

            GetFulfillmentShopsCommand command = GetFulfillmentShopsCommand.of("CUST001", "stale-token");

            // when & then
            assertThatThrownBy(() -> client.getShops(command))
                .isInstanceOf(com.personal.marketnote.fulfillment.exception.FulfillmentInvalidAccessException.class);
        }

        @Test
        @DisplayName("RestClientResponseException 본문에 errorInfo가 있으면 vendor 메시지를 사용한다")
        void shouldUseVendorMessageFromExceptionBody() {
            // given
            stubShopProperties();
            stubPayloadGenerator();
            String errorBody = "{\"errorInfo\":{\"errorCode\":\"E_VENDOR\",\"errorMessage\":\"vendor down\"}}";
            stubGetChainThrows(new HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                errorBody.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
            ));

            GetFulfillmentShopsCommand command = GetFulfillmentShopsCommand.of("CUST001", "valid-token");

            // when & then
            assertThatThrownBy(() -> client.getShops(command))
                .isInstanceOf(GetFulfillmentShopsFailedException.class)
                .hasMessageContaining("vendor down");
        }
    }

    @Nested
    @DisplayName("설정 검증 추가 분기")
    class PropertyValidationExtra {

        @Test
        @DisplayName("shopPath가 누락되면 FulfillmentConfigurationException이 발생한다")
        void shouldThrowWhenShopPathMissing() {
            // given
            when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
            when(properties.getShopPath()).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> client.registerShop(buildValidRegisterCommand()))
                .isInstanceOf(FulfillmentConfigurationException.class);
        }

        @Test
        @DisplayName("shopPath에 customerCode 플레이스홀더가 없으면 FulfillmentConfigurationException이 발생한다")
        void shouldThrowWhenShopPathMissesPlaceholder() {
            // given
            when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
            when(properties.getShopPath()).thenReturn("/marketnote");

            // when & then
            assertThatThrownBy(() -> client.registerShop(buildValidRegisterCommand()))
                .isInstanceOf(FulfillmentConfigurationException.class);
        }
    }

    @Nested
    @DisplayName("sensitive path 분기")
    class SensitivePathDispatch {

        @Test
        @DisplayName("sensitivePaths에 등록된 shop 경로는 recordSensitive로 위임된다")
        void shouldDispatchRecordSensitiveWhenPathIsSensitive() {
            // given
            stubShopProperties();
            stubPayloadGenerator();
            String path = "/marketnote/{customerCode}";
            when(properties.getSensitivePaths()).thenReturn(Set.of(path));
            stubGetChainThrows(new ResourceAccessException("Connection refused"));

            GetFulfillmentShopsCommand command = GetFulfillmentShopsCommand.of("CUST001", "valid-token");

            // when
            assertThatThrownBy(() -> client.getShops(command))
                .isInstanceOf(GetFulfillmentShopsFailedException.class);

            // then
            verify(externalCallLogger).recordSensitive(
                eq(ExternalSystem.FASSTO), eq("getShops"), any(Map.class), any(Supplier.class)
            );
            verify(externalCallLogger, never()).record(
                eq(ExternalSystem.FASSTO), eq("getShops"), any(Map.class), any(Supplier.class)
            );
        }

        @Test
        @DisplayName("sensitivePaths에 미등록된 shop 경로는 record로 위임된다")
        void shouldDispatchRecordWhenPathIsNotSensitive() {
            // given
            stubShopProperties();
            stubPayloadGenerator();
            when(properties.getSensitivePaths()).thenReturn(Set.of());
            stubGetChainThrows(new ResourceAccessException("Connection refused"));

            GetFulfillmentShopsCommand command = GetFulfillmentShopsCommand.of("CUST001", "valid-token");

            // when
            assertThatThrownBy(() -> client.getShops(command))
                .isInstanceOf(GetFulfillmentShopsFailedException.class);

            // then
            verify(externalCallLogger).record(
                eq(ExternalSystem.FASSTO), eq("getShops"), any(Map.class), any(Supplier.class)
            );
            verify(externalCallLogger, never()).recordSensitive(
                eq(ExternalSystem.FASSTO), eq("getShops"), any(Map.class), any(Supplier.class)
            );
        }
    }
}
