package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.external.log.ExternalSystem;
import com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.*;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.exception.FulfillmentConfigurationException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentSuppliersFailedException;
import com.personal.marketnote.fulfillment.exception.RegisterFulfillmentSupplierFailedException;
import com.personal.marketnote.fulfillment.exception.UpdateFulfillmentSupplierFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentSuppliersCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentSupplierCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentSupplierCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentSuppliersResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentSupplierResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentSupplierResult;
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
@DisplayName("FulfillmentSupplierClient 테스트")
class FulfillmentSupplierClientTest {

    private FulfillmentSupplierClient client;

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

        client = new FulfillmentSupplierClient(
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

    private void stubSupplierProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getSupplierPath()).thenReturn("/supplier/{customerCode}");
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

    private RegisterFulfillmentSupplierCommand buildValidRegisterCommand() {
        return RegisterFulfillmentSupplierCommand.builder()
            .customerCode("CUST001")
            .accessToken("valid-token")
            .supplierName("테스트 공급사")
            .customerSupplierCode("SUP001")
            .useYn("Y")
            .dealStartDate("20260101")
            .dealEndDate("20261231")
            .zipCode("12345")
            .address1("서울시")
            .address2("강남구")
            .ceoName("홍길동")
            .businessNumber("123-45-67890")
            .businessCategory("도매")
            .businessType("유통")
            .phoneNumber("02-1234-5678")
            .faxNumber("02-1234-5679")
            .primaryManagerName("김철수")
            .primaryManagerPosition("과장")
            .primaryManagerPhoneNumber("010-1234-5678")
            .primaryManagerEmail("test@test.com")
            .build();
    }

    private UpdateFulfillmentSupplierCommand buildValidUpdateCommand() {
        return UpdateFulfillmentSupplierCommand.builder()
            .customerCode("CUST001")
            .accessToken("valid-token")
            .supplierCode("SUP001")
            .supplierName("테스트 공급사")
            .customerSupplierCode("SUP001")
            .useYn("Y")
            .dealStartDate("20260101")
            .dealEndDate("20261231")
            .zipCode("12345")
            .address1("서울시")
            .address2("강남구")
            .ceoName("홍길동")
            .businessNumber("123-45-67890")
            .businessCategory("도매")
            .businessType("유통")
            .phoneNumber("02-1234-5678")
            .faxNumber("02-1234-5679")
            .primaryManagerName("김철수")
            .primaryManagerPosition("과장")
            .primaryManagerPhoneNumber("010-1234-5678")
            .primaryManagerEmail("test@test.com")
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
    @DisplayName("registerSupplier 실패")
    class RegisterSupplierFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 RegisterFulfillmentSupplierFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubSupplierProperties();
            stubPayloadGenerator();
            stubPatchChainThrows(new ResourceAccessException("Connection refused"));

            RegisterFulfillmentSupplierCommand command = RegisterFulfillmentSupplierCommand.builder()
                .customerCode("CUST001")
                .accessToken("valid-token")
                .supplierName("테스트 공급사")
                .customerSupplierCode("SUP001")
                .useYn("Y")
                .dealStartDate("20260101")
                .dealEndDate("20261231")
                .zipCode("12345")
                .address1("서울시")
                .address2("강남구")
                .ceoName("홍길동")
                .businessNumber("123-45-67890")
                .businessCategory("도매")
                .businessType("유통")
                .phoneNumber("02-1234-5678")
                .faxNumber("02-1234-5679")
                .primaryManagerName("김철수")
                .primaryManagerPosition("과장")
                .primaryManagerPhoneNumber("010-1234-5678")
                .primaryManagerEmail("test@test.com")
                .build();

            // when & then
            assertThatThrownBy(() -> client.registerSupplier(command))
                .isInstanceOf(RegisterFulfillmentSupplierFailedException.class);
        }
    }

    @Nested
    @DisplayName("getSuppliers 실패")
    class GetSuppliersFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 GetFulfillmentSuppliersFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubSupplierProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new ResourceAccessException("Connection refused"));

            GetFulfillmentSuppliersCommand command = GetFulfillmentSuppliersCommand.of("CUST001", "valid-token");

            // when & then
            assertThatThrownBy(() -> client.getSuppliers(command))
                .isInstanceOf(GetFulfillmentSuppliersFailedException.class);
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

            GetFulfillmentSuppliersCommand command = GetFulfillmentSuppliersCommand.of("CUST001", "valid-token");

            // when & then
            assertThatThrownBy(() -> client.getSuppliers(command))
                .isInstanceOf(FulfillmentConfigurationException.class);
        }
    }

    @Nested
    @DisplayName("registerSupplier 성공")
    class RegisterSupplierSuccess {

        @Test
        @DisplayName("정상 응답 시 RegisterFulfillmentSupplierResult를 반환한다")
        void shouldReturnRegisterResultOnSuccessResponse() {
            // given
            stubSupplierProperties();
            stubPayloadGenerator();
            RegisterFulfillmentSupplierResponse payload = new RegisterFulfillmentSupplierResponse(
                new FulfillmentResponseHeader("200", "success", 1),
                new RegisterFulfillmentSupplierDataResponse("OK", "200", "SUP001"),
                null
            );
            stubPatchChain(ResponseEntity.ok(toJson(payload)));

            // when
            RegisterFulfillmentSupplierResult result = client.registerSupplier(buildValidRegisterCommand());

            // then
            assertThat(result.code()).isEqualTo("200");
            assertThat(result.supplierCode()).isEqualTo("SUP001");
        }
    }

    @Nested
    @DisplayName("updateSupplier 성공/실패")
    class UpdateSupplierTest {

        @Test
        @DisplayName("정상 응답 시 UpdateFulfillmentSupplierResult를 반환한다")
        void shouldReturnUpdateResultOnSuccessResponse() {
            // given
            stubSupplierProperties();
            stubPayloadGenerator();
            RegisterFulfillmentSupplierResponse payload = new RegisterFulfillmentSupplierResponse(
                new FulfillmentResponseHeader("200", "success", 1),
                new RegisterFulfillmentSupplierDataResponse("OK", "200", "SUP001"),
                null
            );
            stubPatchChain(ResponseEntity.ok(toJson(payload)));

            // when
            UpdateFulfillmentSupplierResult result = client.updateSupplier(buildValidUpdateCommand());

            // then
            assertThat(result.supplierCode()).isEqualTo("SUP001");
        }

        @Test
        @DisplayName("4xx 응답 시 UpdateFulfillmentSupplierFailedException이 발생한다")
        void shouldThrowOn4xx() {
            // given
            stubSupplierProperties();
            stubPayloadGenerator();
            stubPatchChainThrows(new HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "client error".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
            ));

            // when & then
            assertThatThrownBy(() -> client.updateSupplier(buildValidUpdateCommand()))
                .isInstanceOf(UpdateFulfillmentSupplierFailedException.class);
        }
    }

    @Nested
    @DisplayName("getSuppliers 성공")
    class GetSuppliersSuccess {

        @Test
        @DisplayName("정상 응답 시 GetFulfillmentSuppliersResult를 반환한다")
        void shouldReturnSuppliersResultOnSuccessResponse() {
            // given
            stubSupplierProperties();
            stubPayloadGenerator();
            FulfillmentSuppliersItemResponse item = new FulfillmentSuppliersItemResponse(
                "SUP001", "테스트 공급사", "SUPCUST", "CUST001", "Cust",
                "20260101", "20261231", "12345", "서울시", "강남구",
                "홍길동", "123-45-67890", "도매", "유통",
                "02-1234-5678", "02-1234-5679",
                "김철수", "과장", "010-1234-5678", "test@test.com",
                null, null, null, null, "Y"
            );
            FulfillmentSuppliersResponse payload = new FulfillmentSuppliersResponse(
                new FulfillmentResponseHeader("200", "success", 1),
                List.of(item),
                null
            );
            stubGetChain(ResponseEntity.ok(toJson(payload)));

            GetFulfillmentSuppliersCommand command = GetFulfillmentSuppliersCommand.of("CUST001", "valid-token");

            // when
            GetFulfillmentSuppliersResult result = client.getSuppliers(command);

            // then
            assertThat(result.dataCount()).isEqualTo(1);
            assertThat(result.suppliers()).hasSize(1);
            assertThat(result.suppliers().get(0).supplierCode()).isEqualTo("SUP001");
        }
    }

    @Nested
    @DisplayName("registerSupplier 실패 추가 분기")
    class RegisterSupplierExtraFailure {

        @Test
        @DisplayName("4xx 응답 시 RegisterFulfillmentSupplierFailedException이 발생한다")
        void shouldThrowOn4xx() {
            // given
            stubSupplierProperties();
            stubPayloadGenerator();
            stubPatchChainThrows(new HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "client error".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
            ));

            // when & then
            assertThatThrownBy(() -> client.registerSupplier(buildValidRegisterCommand()))
                .isInstanceOf(RegisterFulfillmentSupplierFailedException.class);
        }

        @Test
        @DisplayName("5xx 응답 시 RegisterFulfillmentSupplierFailedException이 발생한다")
        void shouldThrowOn5xx() {
            // given
            stubSupplierProperties();
            stubPayloadGenerator();
            stubPatchChainThrows(new HttpServerErrorException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "server boom".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
            ));

            // when & then
            assertThatThrownBy(() -> client.registerSupplier(buildValidRegisterCommand()))
                .isInstanceOf(RegisterFulfillmentSupplierFailedException.class);
        }

        @Test
        @DisplayName("200 응답이지만 header 실패면 RegisterFulfillmentSupplierFailedException이 발생한다")
        void shouldThrowOnHeaderFailure() {
            // given
            stubSupplierProperties();
            stubPayloadGenerator();
            RegisterFulfillmentSupplierResponse payload = new RegisterFulfillmentSupplierResponse(
                new FulfillmentResponseHeader("500", "vendor failure", 0),
                null,
                null
            );
            stubPatchChain(ResponseEntity.ok(toJson(payload)));

            // when & then
            assertThatThrownBy(() -> client.registerSupplier(buildValidRegisterCommand()))
                .isInstanceOf(RegisterFulfillmentSupplierFailedException.class);
        }

        @Test
        @DisplayName("200 응답이지만 data.code가 200이 아니면 RegisterFulfillmentSupplierFailedException이 발생한다")
        void shouldThrowOnDataCodeFailure() {
            // given
            stubSupplierProperties();
            stubPayloadGenerator();
            RegisterFulfillmentSupplierResponse payload = new RegisterFulfillmentSupplierResponse(
                new FulfillmentResponseHeader("200", "success", 1),
                new RegisterFulfillmentSupplierDataResponse("vendor data error", "400", null),
                null
            );
            stubPatchChain(ResponseEntity.ok(toJson(payload)));

            // when & then
            assertThatThrownBy(() -> client.registerSupplier(buildValidRegisterCommand()))
                .isInstanceOf(RegisterFulfillmentSupplierFailedException.class)
                .hasMessageContaining("vendor data error");
        }

        @Test
        @DisplayName("응답 본문이 깨진 JSON이면 재시도 후 RegisterFulfillmentSupplierFailedException이 발생한다")
        void shouldThrowOnBrokenJson() {
            // given
            stubSupplierProperties();
            stubPayloadGenerator();
            stubPatchChain(ResponseEntity.ok("not-a-json"));

            // when & then
            assertThatThrownBy(() -> client.registerSupplier(buildValidRegisterCommand()))
                .isInstanceOf(RegisterFulfillmentSupplierFailedException.class);
        }
    }

    @Nested
    @DisplayName("getSuppliers 실패 추가 분기")
    class GetSuppliersExtraFailure {

        @Test
        @DisplayName("4xx 응답 시 GetFulfillmentSuppliersFailedException이 발생한다")
        void shouldThrowOn4xx() {
            // given
            stubSupplierProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "client error".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
            ));

            GetFulfillmentSuppliersCommand command = GetFulfillmentSuppliersCommand.of("CUST001", "valid-token");

            // when & then
            assertThatThrownBy(() -> client.getSuppliers(command))
                .isInstanceOf(GetFulfillmentSuppliersFailedException.class);
        }

        @Test
        @DisplayName("200 응답이지만 header 실패 + errorInfo 메시지 존재 시 vendor 메시지를 포함한 GetFulfillmentSuppliersFailedException이 발생한다")
        void shouldIncludeVendorMessageOnHeaderFailureWithErrorInfo() {
            // given
            stubSupplierProperties();
            stubPayloadGenerator();
            FulfillmentSuppliersResponse payload = new FulfillmentSuppliersResponse(
                new FulfillmentResponseHeader("500", "fail", 0),
                List.of(),
                new FulfillmentErrorInfo("E_INTERNAL", "vendor outage", null)
            );
            stubGetChain(ResponseEntity.ok(toJson(payload)));

            GetFulfillmentSuppliersCommand command = GetFulfillmentSuppliersCommand.of("CUST001", "valid-token");

            // when & then
            assertThatThrownBy(() -> client.getSuppliers(command))
                .isInstanceOf(GetFulfillmentSuppliersFailedException.class)
                .hasMessageContaining("vendor outage");
        }

        @Test
        @DisplayName("응답 본문이 깨진 JSON이면 재시도 후 GetFulfillmentSuppliersFailedException이 발생한다")
        void shouldThrowOnBrokenJson() {
            // given
            stubSupplierProperties();
            stubPayloadGenerator();
            stubGetChain(ResponseEntity.ok("not-a-json"));

            GetFulfillmentSuppliersCommand command = GetFulfillmentSuppliersCommand.of("CUST001", "valid-token");

            // when & then
            assertThatThrownBy(() -> client.getSuppliers(command))
                .isInstanceOf(GetFulfillmentSuppliersFailedException.class);
        }

        @Test
        @DisplayName("응답 헤더가 INVALID_ACCESS이면 FulfillmentInvalidAccessException이 발생한다")
        void shouldThrowInvalidAccessOnInvalidAccessHeader() {
            // given
            stubSupplierProperties();
            stubPayloadGenerator();
            FulfillmentSuppliersResponse payload = new FulfillmentSuppliersResponse(
                new FulfillmentResponseHeader("401", "INVALID_ACCESS", 0),
                List.of(),
                null
            );
            stubGetChain(ResponseEntity.ok(toJson(payload)));

            GetFulfillmentSuppliersCommand command = GetFulfillmentSuppliersCommand.of("CUST001", "stale-token");

            // when & then
            assertThatThrownBy(() -> client.getSuppliers(command))
                .isInstanceOf(com.personal.marketnote.fulfillment.exception.FulfillmentInvalidAccessException.class);
        }

        @Test
        @DisplayName("RestClientResponseException 본문에 errorInfo가 있으면 vendor 메시지를 사용한다")
        void shouldUseVendorMessageFromExceptionBody() {
            // given
            stubSupplierProperties();
            stubPayloadGenerator();
            String errorBody = "{\"errorInfo\":{\"errorCode\":\"E_VENDOR\",\"errorMessage\":\"vendor down\"}}";
            stubGetChainThrows(new HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                errorBody.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
            ));

            GetFulfillmentSuppliersCommand command = GetFulfillmentSuppliersCommand.of("CUST001", "valid-token");

            // when & then
            assertThatThrownBy(() -> client.getSuppliers(command))
                .isInstanceOf(GetFulfillmentSuppliersFailedException.class)
                .hasMessageContaining("vendor down");
        }
    }

    @Nested
    @DisplayName("설정 검증 추가 분기")
    class PropertyValidationExtra {

        @Test
        @DisplayName("supplierPath가 누락되면 FulfillmentConfigurationException이 발생한다")
        void shouldThrowWhenSupplierPathMissing() {
            // given
            when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
            when(properties.getSupplierPath()).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> client.registerSupplier(buildValidRegisterCommand()))
                .isInstanceOf(FulfillmentConfigurationException.class);
        }

        @Test
        @DisplayName("supplierPath에 customerCode 플레이스홀더가 없으면 FulfillmentConfigurationException이 발생한다")
        void shouldThrowWhenSupplierPathMissesPlaceholder() {
            // given
            when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
            when(properties.getSupplierPath()).thenReturn("/supplier");

            // when & then
            assertThatThrownBy(() -> client.registerSupplier(buildValidRegisterCommand()))
                .isInstanceOf(FulfillmentConfigurationException.class);
        }
    }

    @Nested
    @DisplayName("sensitive path 분기")
    class SensitivePathDispatch {

        @Test
        @DisplayName("sensitivePaths에 등록된 supplier 경로는 recordSensitive로 위임된다")
        void shouldDispatchRecordSensitiveWhenPathIsSensitive() {
            // given
            stubSupplierProperties();
            stubPayloadGenerator();
            String path = "/supplier/{customerCode}";
            when(properties.getSensitivePaths()).thenReturn(Set.of(path));
            stubGetChainThrows(new ResourceAccessException("Connection refused"));

            GetFulfillmentSuppliersCommand command = GetFulfillmentSuppliersCommand.of("CUST001", "valid-token");

            // when
            assertThatThrownBy(() -> client.getSuppliers(command))
                .isInstanceOf(GetFulfillmentSuppliersFailedException.class);

            // then
            verify(externalCallLogger).recordSensitive(
                eq(ExternalSystem.FASSTO), eq("getSuppliers"), any(Map.class), any(Supplier.class)
            );
            verify(externalCallLogger, never()).record(
                eq(ExternalSystem.FASSTO), eq("getSuppliers"), any(Map.class), any(Supplier.class)
            );
        }

        @Test
        @DisplayName("sensitivePaths에 미등록된 supplier 경로는 record로 위임된다")
        void shouldDispatchRecordWhenPathIsNotSensitive() {
            // given
            stubSupplierProperties();
            stubPayloadGenerator();
            when(properties.getSensitivePaths()).thenReturn(Set.of());
            stubGetChainThrows(new ResourceAccessException("Connection refused"));

            GetFulfillmentSuppliersCommand command = GetFulfillmentSuppliersCommand.of("CUST001", "valid-token");

            // when
            assertThatThrownBy(() -> client.getSuppliers(command))
                .isInstanceOf(GetFulfillmentSuppliersFailedException.class);

            // then
            verify(externalCallLogger).record(
                eq(ExternalSystem.FASSTO), eq("getSuppliers"), any(Map.class), any(Supplier.class)
            );
            verify(externalCallLogger, never()).recordSensitive(
                eq(ExternalSystem.FASSTO), eq("getSuppliers"), any(Map.class), any(Supplier.class)
            );
        }
    }
}
