package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.external.log.ExternalSystem;
import com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentErrorInfo;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentResponseHeader;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentSettlementDailyCostItemResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentSettlementDailyCostListResponse;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.exception.FulfillmentConfigurationException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentSettlementDailyCostsFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentSettlementDailyCostsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentSettlementDailyCostsResult;
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
@DisplayName("FulfillmentSettlementClient 테스트")
class FulfillmentSettlementClientTest {

    private FulfillmentSettlementClient client;

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
    private ExternalCallLogger externalCallLogger;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.build()).thenReturn(restClient);

        externalCallLogger = spy(new ExternalCallLogger(new SimpleMeterRegistry()));

        client = new FulfillmentSettlementClient(
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

    private void stubSettlementProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getSettlementDailyCostPath()).thenReturn("/settlement/{yearMonth}/{whCd}/{customerCode}");
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

    private GetFulfillmentSettlementDailyCostsCommand buildCommand() {
        return GetFulfillmentSettlementDailyCostsCommand.of(
            "202601", "WH001", "CUST001", "valid-token"
        );
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Nested
    @DisplayName("getDailyCosts 실패")
    class GetDailyCostsFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 GetFulfillmentSettlementDailyCostsFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubSettlementProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new ResourceAccessException("Connection refused"));

            GetFulfillmentSettlementDailyCostsCommand command = GetFulfillmentSettlementDailyCostsCommand.of(
                "202601", "WH001", "CUST001", "valid-token"
            );

            // when & then
            assertThatThrownBy(() -> client.getDailyCosts(command))
                .isInstanceOf(GetFulfillmentSettlementDailyCostsFailedException.class);
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

            GetFulfillmentSettlementDailyCostsCommand command = GetFulfillmentSettlementDailyCostsCommand.of(
                "202601", "WH001", "CUST001", "valid-token"
            );

            // when & then
            assertThatThrownBy(() -> client.getDailyCosts(command))
                .isInstanceOf(FulfillmentConfigurationException.class);
        }
    }

    @Nested
    @DisplayName("getDailyCosts 성공")
    class GetDailyCostsSuccess {

        @Test
        @DisplayName("정상 응답 시 GetFulfillmentSettlementDailyCostsResult를 반환한다")
        void shouldReturnDailyCostsResultOnSuccess() {
            // given
            stubSettlementProperties();
            stubPayloadGenerator();
            FulfillmentSettlementDailyCostItemResponse item = new FulfillmentSettlementDailyCostItemResponse(
                "20260101", "WH001", "CUST001",
                "1000", "2000", "300", "400", "500",
                "600", "700", "800", "900", "10000"
            );
            FulfillmentSettlementDailyCostListResponse payload = new FulfillmentSettlementDailyCostListResponse(
                new FulfillmentResponseHeader("200", "success", 1),
                List.of(item),
                null
            );
            stubGetChain(ResponseEntity.ok(toJson(payload)));

            // when
            GetFulfillmentSettlementDailyCostsResult result = client.getDailyCosts(buildCommand());

            // then
            assertThat(result.dataCount()).isEqualTo(1);
            assertThat(result.dailyCosts()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getDailyCosts 실패 추가 분기")
    class GetDailyCostsExtraFailure {

        @Test
        @DisplayName("4xx 응답 시 GetFulfillmentSettlementDailyCostsFailedException이 발생한다")
        void shouldThrowOn4xx() {
            // given
            stubSettlementProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "client error".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
            ));

            // when & then
            assertThatThrownBy(() -> client.getDailyCosts(buildCommand()))
                .isInstanceOf(GetFulfillmentSettlementDailyCostsFailedException.class);
        }

        @Test
        @DisplayName("5xx 응답 시 GetFulfillmentSettlementDailyCostsFailedException이 발생한다")
        void shouldThrowOn5xx() {
            // given
            stubSettlementProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new HttpServerErrorException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "boom".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
            ));

            // when & then
            assertThatThrownBy(() -> client.getDailyCosts(buildCommand()))
                .isInstanceOf(GetFulfillmentSettlementDailyCostsFailedException.class);
        }

        @Test
        @DisplayName("200 응답이지만 header 실패면 GetFulfillmentSettlementDailyCostsFailedException이 발생한다")
        void shouldThrowOnHeaderFailure() {
            // given
            stubSettlementProperties();
            stubPayloadGenerator();
            FulfillmentSettlementDailyCostListResponse payload = new FulfillmentSettlementDailyCostListResponse(
                new FulfillmentResponseHeader("500", "vendor failure", 0),
                List.of(),
                null
            );
            stubGetChain(ResponseEntity.ok(toJson(payload)));

            // when & then
            assertThatThrownBy(() -> client.getDailyCosts(buildCommand()))
                .isInstanceOf(GetFulfillmentSettlementDailyCostsFailedException.class);
        }

        @Test
        @DisplayName("200 응답이지만 header 실패 + errorInfo 메시지 존재 시 vendor 메시지를 포함한 GetFulfillmentSettlementDailyCostsFailedException이 발생한다")
        void shouldIncludeVendorMessageOnHeaderFailureWithErrorInfo() {
            // given
            stubSettlementProperties();
            stubPayloadGenerator();
            FulfillmentSettlementDailyCostListResponse payload = new FulfillmentSettlementDailyCostListResponse(
                new FulfillmentResponseHeader("500", "fail", 0),
                List.of(),
                new FulfillmentErrorInfo("E_INTERNAL", "vendor outage", null)
            );
            stubGetChain(ResponseEntity.ok(toJson(payload)));

            // when & then
            assertThatThrownBy(() -> client.getDailyCosts(buildCommand()))
                .isInstanceOf(GetFulfillmentSettlementDailyCostsFailedException.class)
                .hasMessageContaining("vendor outage");
        }

        @Test
        @DisplayName("응답 본문이 깨진 JSON이면 재시도 후 GetFulfillmentSettlementDailyCostsFailedException이 발생한다")
        void shouldThrowOnBrokenJson() {
            // given
            stubSettlementProperties();
            stubPayloadGenerator();
            stubGetChain(ResponseEntity.ok("not-a-json"));

            // when & then
            assertThatThrownBy(() -> client.getDailyCosts(buildCommand()))
                .isInstanceOf(GetFulfillmentSettlementDailyCostsFailedException.class);
        }

        @Test
        @DisplayName("응답 헤더가 INVALID_ACCESS이면 FulfillmentInvalidAccessException이 발생한다")
        void shouldThrowInvalidAccessOnInvalidAccessHeader() {
            // given
            stubSettlementProperties();
            stubPayloadGenerator();
            FulfillmentSettlementDailyCostListResponse payload = new FulfillmentSettlementDailyCostListResponse(
                new FulfillmentResponseHeader("401", "INVALID_ACCESS", 0),
                List.of(),
                null
            );
            stubGetChain(ResponseEntity.ok(toJson(payload)));

            // when & then
            assertThatThrownBy(() -> client.getDailyCosts(buildCommand()))
                .isInstanceOf(com.personal.marketnote.fulfillment.exception.FulfillmentInvalidAccessException.class);
        }

        @Test
        @DisplayName("RestClientResponseException 본문에 errorInfo가 있으면 vendor 메시지를 사용한다")
        void shouldUseVendorMessageFromExceptionBody() {
            // given
            stubSettlementProperties();
            stubPayloadGenerator();
            String errorBody = "{\"errorInfo\":{\"errorCode\":\"E_VENDOR\",\"errorMessage\":\"vendor down\"}}";
            stubGetChainThrows(new HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                errorBody.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
            ));

            // when & then
            assertThatThrownBy(() -> client.getDailyCosts(buildCommand()))
                .isInstanceOf(GetFulfillmentSettlementDailyCostsFailedException.class)
                .hasMessageContaining("vendor down");
        }
    }

    @Nested
    @DisplayName("설정 검증 추가 분기")
    class PropertyValidationExtra {

        @Test
        @DisplayName("settlementDailyCostPath가 누락되면 FulfillmentConfigurationException이 발생한다")
        void shouldThrowWhenPathMissing() {
            // given
            when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
            when(properties.getSettlementDailyCostPath()).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> client.getDailyCosts(buildCommand()))
                .isInstanceOf(FulfillmentConfigurationException.class);
        }

        @Test
        @DisplayName("settlementDailyCostPath에 yearMonth 플레이스홀더가 없으면 FulfillmentConfigurationException이 발생한다")
        void shouldThrowWhenYearMonthPlaceholderMissing() {
            // given
            when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
            when(properties.getSettlementDailyCostPath()).thenReturn("/settlement/{whCd}/{customerCode}");

            // when & then
            assertThatThrownBy(() -> client.getDailyCosts(buildCommand()))
                .isInstanceOf(FulfillmentConfigurationException.class);
        }

        @Test
        @DisplayName("settlementDailyCostPath에 whCd 플레이스홀더가 없으면 FulfillmentConfigurationException이 발생한다")
        void shouldThrowWhenWhCdPlaceholderMissing() {
            // given
            when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
            when(properties.getSettlementDailyCostPath()).thenReturn("/settlement/{yearMonth}/{customerCode}");

            // when & then
            assertThatThrownBy(() -> client.getDailyCosts(buildCommand()))
                .isInstanceOf(FulfillmentConfigurationException.class);
        }

        @Test
        @DisplayName("settlementDailyCostPath에 customerCode 플레이스홀더가 없으면 FulfillmentConfigurationException이 발생한다")
        void shouldThrowWhenCustomerCodePlaceholderMissing() {
            // given
            when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
            when(properties.getSettlementDailyCostPath()).thenReturn("/settlement/{yearMonth}/{whCd}");

            // when & then
            assertThatThrownBy(() -> client.getDailyCosts(buildCommand()))
                .isInstanceOf(FulfillmentConfigurationException.class);
        }
    }

    @Nested
    @DisplayName("sensitive path 분기")
    class SensitivePathDispatch {

        @Test
        @DisplayName("sensitivePaths에 등록된 settlementDailyCost 경로는 recordSensitive로 위임된다")
        void shouldDispatchRecordSensitiveWhenPathIsSensitive() {
            // given
            stubSettlementProperties();
            stubPayloadGenerator();
            String path = "/settlement/{yearMonth}/{whCd}/{customerCode}";
            when(properties.getSensitivePaths()).thenReturn(Set.of(path));
            stubGetChainThrows(new ResourceAccessException("Connection refused"));

            GetFulfillmentSettlementDailyCostsCommand command = GetFulfillmentSettlementDailyCostsCommand.of(
                "202601", "WH001", "CUST001", "valid-token"
            );

            // when
            assertThatThrownBy(() -> client.getDailyCosts(command))
                .isInstanceOf(GetFulfillmentSettlementDailyCostsFailedException.class);

            // then
            verify(externalCallLogger).recordSensitive(
                eq(ExternalSystem.FASSTO), eq("getDailyCosts"), any(Map.class), any(Supplier.class)
            );
            verify(externalCallLogger, never()).record(
                eq(ExternalSystem.FASSTO), eq("getDailyCosts"), any(Map.class), any(Supplier.class)
            );
        }

        @Test
        @DisplayName("sensitivePaths에 미등록된 settlementDailyCost 경로는 record로 위임된다")
        void shouldDispatchRecordWhenPathIsNotSensitive() {
            // given
            stubSettlementProperties();
            stubPayloadGenerator();
            when(properties.getSensitivePaths()).thenReturn(Set.of());
            stubGetChainThrows(new ResourceAccessException("Connection refused"));

            GetFulfillmentSettlementDailyCostsCommand command = GetFulfillmentSettlementDailyCostsCommand.of(
                "202601", "WH001", "CUST001", "valid-token"
            );

            // when
            assertThatThrownBy(() -> client.getDailyCosts(command))
                .isInstanceOf(GetFulfillmentSettlementDailyCostsFailedException.class);

            // then
            verify(externalCallLogger).record(
                eq(ExternalSystem.FASSTO), eq("getDailyCosts"), any(Map.class), any(Supplier.class)
            );
            verify(externalCallLogger, never()).recordSensitive(
                eq(ExternalSystem.FASSTO), eq("getDailyCosts"), any(Map.class), any(Supplier.class)
            );
        }
    }
}
