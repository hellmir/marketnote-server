package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.external.log.ExternalSystem;
import com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentErrorInfo;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentResponseHeader;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.RegisterFulfillmentDeliveryItemResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.RegisterFulfillmentDeliveryResponse;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.fassto.FasstoCustomerGoodsCodeEncoder;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.exception.FulfillmentConfigurationException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentReturnGodDetailFailedException;
import com.personal.marketnote.fulfillment.exception.RegisterFulfillmentReturnDeliveryFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentReturnGodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentReturnDeliveryItemCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentReturnGodDetailResult;
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
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
    private ExternalCallLogger externalCallLogger;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.build()).thenReturn(restClient);

        externalCallLogger = spy(new ExternalCallLogger(new SimpleMeterRegistry()));

        client = new FulfillmentReturnDeliveryClient(
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
                null, "PROD001", "PROD001", "20260414", 1
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
    private void stubGetChainReturns(ResponseEntity<String> entity) {
        RestClient.RequestHeadersUriSpec rawSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headerSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec respSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(rawSpec);
        when(rawSpec.uri(any(java.net.URI.class))).thenReturn(headerSpec);
        when(headerSpec.headers(any())).thenReturn(headerSpec);
        when(headerSpec.retrieve()).thenReturn(respSpec);
        when(respSpec.toEntity(any(Class.class))).thenReturn(entity);
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
    @DisplayName("registerReturnDelivery 실패")
    class RegisterReturnDeliveryFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 RegisterFulfillmentReturnDeliveryFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubReturnDeliveryProperties();
            stubPayloadGenerator();
            stubPostChainThrows(new ResourceAccessException("Connection refused"));

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
            stubGetChainThrows(new ResourceAccessException("Connection refused"));

            GetFulfillmentReturnGodDetailCommand command = GetFulfillmentReturnGodDetailCommand.builder()
                    .customerCode("CUST001")
                    .accessToken("valid-token")
                    .startDate("20260101")
                    .endDate("20260131")
                    .build();

            // when & then
            assertThatThrownBy(() -> client.getReturnGodDetail(command))
                    .isInstanceOf(GetFulfillmentReturnGodDetailFailedException.class);
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

            GetFulfillmentReturnGodDetailCommand command = GetFulfillmentReturnGodDetailCommand.builder()
                    .customerCode("CUST001")
                    .accessToken("valid-token")
                    .startDate("20260101")
                    .endDate("20260131")
                    .build();

            // when & then
            assertThatThrownBy(() -> client.getReturnGodDetail(command))
                    .isInstanceOf(FulfillmentConfigurationException.class);
        }
    }

    @Nested
    @DisplayName("registerReturnDelivery 성공")
    class RegisterReturnDeliverySuccess {

        @Test
        @DisplayName("정상 응답 시 RegisterFulfillmentDeliveryResult를 반환한다")
        void shouldReturnDeliveryResultOnSuccess() {
            // given
            stubReturnDeliveryProperties();
            stubPayloadGenerator();
            RegisterFulfillmentDeliveryResponse payload = new RegisterFulfillmentDeliveryResponse(
                    new FulfillmentResponseHeader("200", "success", 1),
                    List.of(new RegisterFulfillmentDeliveryItemResponse("FMS001", "ORD001", "OK", "200", null)),
                    null
            );
            stubPostChain(ResponseEntity.ok(toJson(payload)));

            // when
            RegisterFulfillmentDeliveryResult result = client.registerReturnDelivery(buildValidRegisterCommand());

            // then
            assertThat(result.dataCount()).isEqualTo(1);
            assertThat(result.deliveries()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("registerReturnDelivery 실패 추가 분기")
    class RegisterReturnDeliveryExtraFailure {

        @Test
        @DisplayName("4xx 응답 시 RegisterFulfillmentReturnDeliveryFailedException이 발생한다")
        void shouldThrowOn4xx() {
            // given
            stubReturnDeliveryProperties();
            stubPayloadGenerator();
            stubPostChainThrows(new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Bad Request",
                    "client error".getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8
            ));

            // when & then
            assertThatThrownBy(() -> client.registerReturnDelivery(buildValidRegisterCommand()))
                    .isInstanceOf(RegisterFulfillmentReturnDeliveryFailedException.class);
        }

        @Test
        @DisplayName("5xx 응답 시 RegisterFulfillmentReturnDeliveryFailedException이 발생한다")
        void shouldThrowOn5xx() {
            // given
            stubReturnDeliveryProperties();
            stubPayloadGenerator();
            stubPostChainThrows(new HttpServerErrorException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal Server Error",
                    "boom".getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8
            ));

            // when & then
            assertThatThrownBy(() -> client.registerReturnDelivery(buildValidRegisterCommand()))
                    .isInstanceOf(RegisterFulfillmentReturnDeliveryFailedException.class);
        }

        @Test
        @DisplayName("200 응답이지만 header 실패면 RegisterFulfillmentReturnDeliveryFailedException이 발생한다")
        void shouldThrowOnHeaderFailure() {
            // given
            stubReturnDeliveryProperties();
            stubPayloadGenerator();
            RegisterFulfillmentDeliveryResponse payload = new RegisterFulfillmentDeliveryResponse(
                    new FulfillmentResponseHeader("500", "vendor failure", 0),
                    List.of(),
                    null
            );
            stubPostChain(ResponseEntity.ok(toJson(payload)));

            // when & then
            assertThatThrownBy(() -> client.registerReturnDelivery(buildValidRegisterCommand()))
                    .isInstanceOf(RegisterFulfillmentReturnDeliveryFailedException.class);
        }

        @Test
        @DisplayName("200 응답이지만 item.code가 200이 아니면 vendor 메시지를 포함한 RegisterFulfillmentReturnDeliveryFailedException이 발생한다")
        void shouldThrowOnItemCodeFailureWithVendorMessage() {
            // given
            stubReturnDeliveryProperties();
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
            assertThatThrownBy(() -> client.registerReturnDelivery(buildValidRegisterCommand()))
                    .isInstanceOf(RegisterFulfillmentReturnDeliveryFailedException.class)
                    .hasMessageContaining("item-level vendor error");
        }

        @Test
        @DisplayName("응답 본문이 깨진 JSON이면 재시도 후 RegisterFulfillmentReturnDeliveryFailedException이 발생한다")
        void shouldThrowOnBrokenJson() {
            // given
            stubReturnDeliveryProperties();
            stubPayloadGenerator();
            stubPostChain(ResponseEntity.ok("not-a-json"));

            // when & then
            assertThatThrownBy(() -> client.registerReturnDelivery(buildValidRegisterCommand()))
                    .isInstanceOf(RegisterFulfillmentReturnDeliveryFailedException.class);
        }

        @Test
        @DisplayName("응답 헤더가 INVALID_ACCESS이면 FulfillmentInvalidAccessException이 발생한다")
        void shouldThrowInvalidAccessOnInvalidAccessHeader() {
            // given
            stubReturnDeliveryProperties();
            stubPayloadGenerator();
            RegisterFulfillmentDeliveryResponse payload = new RegisterFulfillmentDeliveryResponse(
                    new FulfillmentResponseHeader("401", "INVALID_ACCESS", 0),
                    List.of(),
                    null
            );
            stubPostChain(ResponseEntity.ok(toJson(payload)));

            // when & then
            assertThatThrownBy(() -> client.registerReturnDelivery(buildValidRegisterCommand()))
                    .isInstanceOf(com.personal.marketnote.fulfillment.exception.FulfillmentInvalidAccessException.class);
        }
    }

    @Nested
    @DisplayName("getReturnGodDetail 실패 추가 분기")
    class GetReturnGodDetailExtraFailure {

        private GetFulfillmentReturnGodDetailCommand buildCommand() {
            return GetFulfillmentReturnGodDetailCommand.builder()
                    .customerCode("CUST001")
                    .accessToken("valid-token")
                    .startDate("20260101")
                    .endDate("20260131")
                    .build();
        }

        @Test
        @DisplayName("4xx 응답 시 GetFulfillmentReturnGodDetailFailedException이 발생한다")
        void shouldThrowOn4xx() {
            // given
            stubReturnGodDetailProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Bad Request",
                    "client error".getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8
            ));

            // when & then
            assertThatThrownBy(() -> client.getReturnGodDetail(buildCommand()))
                    .isInstanceOf(GetFulfillmentReturnGodDetailFailedException.class);
        }

        @Test
        @DisplayName("200 응답이지만 header 실패 + errorInfo 메시지 존재 시 vendor 메시지를 포함한 GetFulfillmentReturnGodDetailFailedException이 발생한다")
        void shouldIncludeVendorMessageOnHeaderFailureWithErrorInfo() {
            // given
            stubReturnGodDetailProperties();
            stubPayloadGenerator();
            String body = String.format(
                    "{\"header\":{\"code\":\"500\",\"msg\":\"fail\",\"dataCount\":0},\"data\":[],\"errorInfo\":{\"errorCode\":\"E_INTERNAL\",\"errorMessage\":\"%s\"}}",
                    "vendor outage"
            );
            stubGetChainReturns(new ResponseEntity<>(body, HttpStatus.OK));

            // when & then
            assertThatThrownBy(() -> client.getReturnGodDetail(buildCommand()))
                    .isInstanceOf(GetFulfillmentReturnGodDetailFailedException.class)
                    .hasMessageContaining("vendor outage");
        }

        @Test
        @DisplayName("응답 본문이 깨진 JSON이면 재시도 후 GetFulfillmentReturnGodDetailFailedException이 발생한다")
        void shouldThrowOnBrokenJson() {
            // given
            stubReturnGodDetailProperties();
            stubPayloadGenerator();
            stubGetChainReturns(new ResponseEntity<>("not-a-json", HttpStatus.OK));

            // when & then
            assertThatThrownBy(() -> client.getReturnGodDetail(buildCommand()))
                    .isInstanceOf(GetFulfillmentReturnGodDetailFailedException.class);
        }
    }

    @Nested
    @DisplayName("설정 검증 추가 분기")
    class PropertyValidationExtra {

        @Test
        @DisplayName("returnDeliveryPath가 누락되면 FulfillmentConfigurationException이 발생한다")
        void shouldThrowWhenReturnDeliveryPathMissing() {
            // given
            when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
            when(properties.getReturnDeliveryPath()).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> client.registerReturnDelivery(buildValidRegisterCommand()))
                    .isInstanceOf(FulfillmentConfigurationException.class);
        }

        @Test
        @DisplayName("returnDeliveryPath에 customerCode 플레이스홀더가 없으면 FulfillmentConfigurationException이 발생한다")
        void shouldThrowWhenReturnDeliveryPathMissesPlaceholder() {
            // given
            when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
            when(properties.getReturnDeliveryPath()).thenReturn("/return-delivery");

            // when & then
            assertThatThrownBy(() -> client.registerReturnDelivery(buildValidRegisterCommand()))
                    .isInstanceOf(FulfillmentConfigurationException.class);
        }

        @Test
        @DisplayName("returnGodDetailPath가 누락되면 FulfillmentConfigurationException이 발생한다")
        void shouldThrowWhenReturnGodDetailPathMissing() {
            // given
            when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
            when(properties.getReturnGodDetailPath()).thenReturn(null);

            GetFulfillmentReturnGodDetailCommand command = GetFulfillmentReturnGodDetailCommand.builder()
                    .customerCode("CUST001")
                    .accessToken("valid-token")
                    .startDate("20260101")
                    .endDate("20260131")
                    .build();

            // when & then
            assertThatThrownBy(() -> client.getReturnGodDetail(command))
                    .isInstanceOf(FulfillmentConfigurationException.class);
        }
    }

    @Nested
    @DisplayName("sensitive path 분기")
    class SensitivePathDispatch {

        @Test
        @DisplayName("sensitivePaths에 등록된 returnDelivery 경로는 recordSensitive로 위임된다")
        void shouldDispatchRecordSensitiveForRegisterReturnDelivery() {
            // given
            stubReturnDeliveryProperties();
            stubPayloadGenerator();
            String path = "/return-delivery/{customerCode}";
            when(properties.getSensitivePaths()).thenReturn(Set.of(path));
            stubPostChainThrows(new ResourceAccessException("Connection refused"));

            // when
            assertThatThrownBy(() -> client.registerReturnDelivery(buildValidRegisterCommand()))
                    .isInstanceOf(RegisterFulfillmentReturnDeliveryFailedException.class);

            // then
            verify(externalCallLogger).recordSensitive(
                    eq(ExternalSystem.FASSTO), eq("registerReturnDelivery"), any(Map.class), any(Supplier.class)
            );
            verify(externalCallLogger, never()).record(
                    eq(ExternalSystem.FASSTO), eq("registerReturnDelivery"), any(Map.class), any(Supplier.class)
            );
        }

        @Test
        @DisplayName("sensitivePaths에 미등록된 returnGodDetail 경로는 record로 위임된다")
        void shouldDispatchRecordForReturnGodDetailWhenNotSensitive() {
            // given
            stubReturnGodDetailProperties();
            stubPayloadGenerator();
            when(properties.getSensitivePaths()).thenReturn(Set.of());
            stubGetChainThrows(new ResourceAccessException("Connection refused"));

            GetFulfillmentReturnGodDetailCommand command = GetFulfillmentReturnGodDetailCommand.builder()
                    .customerCode("CUST001")
                    .accessToken("valid-token")
                    .startDate("20260101")
                    .endDate("20260131")
                    .build();

            // when
            assertThatThrownBy(() -> client.getReturnGodDetail(command))
                    .isInstanceOf(GetFulfillmentReturnGodDetailFailedException.class);

            // then
            verify(externalCallLogger).record(
                    eq(ExternalSystem.FASSTO), eq("getReturnGodDetail"), any(Map.class), any(Supplier.class)
            );
            verify(externalCallLogger, never()).recordSensitive(
                    eq(ExternalSystem.FASSTO), eq("getReturnGodDetail"), any(Map.class), any(Supplier.class)
            );
        }
    }

    @Nested
    @DisplayName("cstGodCd raw 통과")
    class CstGodCdDecoding {

        private static final UUID TEST_PRODUCT_KEY = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        private static final String TEST_ENCODED_CST_GOD_CD = FasstoCustomerGoodsCodeEncoder.encode(TEST_PRODUCT_KEY);
        private static final String FALLBACK_PRODUCT_CODE = "PROD01";
        private static final String LEGACY_UUID_RAW = "550e8400-e29b-41d4-a716-446655440000";

        private GetFulfillmentReturnGodDetailCommand buildCommand() {
            return GetFulfillmentReturnGodDetailCommand.builder()
                    .customerCode("CUST001")
                    .accessToken("valid-token")
                    .startDate("20260101")
                    .endDate("20260131")
                    .build();
        }

        @Test
        @DisplayName("cstGodCd 22자 Base62 응답 → customerProductCode 22자 raw 그대로 통과")
        void shouldPassThroughBase62CstGodCdRaw() {
            // given
            stubReturnGodDetailProperties();
            stubPayloadGenerator();
            String body = String.format("""
                    {
                      "header": {"code": "200", "msg": "success", "dataCount": 1},
                      "data": [{
                        "ordNo": "ORD001",
                        "godList": [{
                          "cstGodCd": "%s",
                          "godNm": "테스트 상품"
                        }]
                      }]
                    }
                    """, TEST_ENCODED_CST_GOD_CD);
            stubGetChainReturns(new ResponseEntity<>(body, HttpStatus.OK));

            // when
            GetFulfillmentReturnGodDetailResult result = client.getReturnGodDetail(buildCommand());

            // then
            assertThat(result.returnGodInfos()).hasSize(1);
            assertThat(result.returnGodInfos().get(0).products()).hasSize(1);
            assertThat(result.returnGodInfos().get(0).products().get(0).customerProductCode())
                    .isEqualTo(TEST_ENCODED_CST_GOD_CD);
        }

        @Test
        @DisplayName("cstGodCd 폴백 productCode 6자 응답 → customerProductCode raw 통과")
        void shouldPassThroughFallbackProductCode() {
            // given
            stubReturnGodDetailProperties();
            stubPayloadGenerator();
            String body = String.format("""
                    {
                      "header": {"code": "200", "msg": "success", "dataCount": 1},
                      "data": [{
                        "ordNo": "ORD001",
                        "godList": [{
                          "cstGodCd": "%s",
                          "godNm": "테스트 상품"
                        }]
                      }]
                    }
                    """, FALLBACK_PRODUCT_CODE);
            stubGetChainReturns(new ResponseEntity<>(body, HttpStatus.OK));

            // when
            GetFulfillmentReturnGodDetailResult result = client.getReturnGodDetail(buildCommand());

            // then
            assertThat(result.returnGodInfos().get(0).products().get(0).customerProductCode())
                    .isEqualTo(FALLBACK_PRODUCT_CODE);
        }

        @Test
        @DisplayName("cstGodCd 36자 UUID 형태 응답(이전 마이그레이션 잔존) → raw 통과")
        void shouldPassThroughLegacyUuidRaw() {
            // given
            stubReturnGodDetailProperties();
            stubPayloadGenerator();
            String body = String.format("""
                    {
                      "header": {"code": "200", "msg": "success", "dataCount": 1},
                      "data": [{
                        "ordNo": "ORD001",
                        "godList": [{
                          "cstGodCd": "%s",
                          "godNm": "테스트 상품"
                        }]
                      }]
                    }
                    """, LEGACY_UUID_RAW);
            stubGetChainReturns(new ResponseEntity<>(body, HttpStatus.OK));

            // when
            GetFulfillmentReturnGodDetailResult result = client.getReturnGodDetail(buildCommand());

            // then
            assertThat(result.returnGodInfos().get(0).products().get(0).customerProductCode())
                    .isEqualTo(LEGACY_UUID_RAW);
        }
    }
}
