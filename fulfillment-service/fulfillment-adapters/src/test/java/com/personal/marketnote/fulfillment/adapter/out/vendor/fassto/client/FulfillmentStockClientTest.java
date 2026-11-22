package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.external.log.ExternalSystem;
import com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.encoder.FasstoCustomerGoodsCodeEncoder;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationSenderType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationTargetType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorName;
import com.personal.marketnote.fulfillment.exception.FulfillmentConfigurationException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentStockDetailFailedException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentStocksFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStockDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStocksCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentStocksResult;
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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentStockClient 테스트")
class FulfillmentStockClientTest {

    private FulfillmentStockClient client;

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

        client = new FulfillmentStockClient(
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

    private void stubStockProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getStockListPath()).thenReturn("/stock/{customerCode}");
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

    @Nested
    @DisplayName("getStocks 실패")
    class GetStocksFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 GetFulfillmentStocksFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubStockProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new ResourceAccessException("Connection refused"));

            GetFulfillmentStocksCommand command = GetFulfillmentStocksCommand.of("CUST001", "valid-token");

            // when & then
            assertThatThrownBy(() -> client.getStocks(command))
                    .isInstanceOf(GetFulfillmentStocksFailedException.class);
        }
    }

    @Nested
    @DisplayName("getStockDetail 실패")
    class GetStockDetailFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 GetFulfillmentStockDetailFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubStockProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new ResourceAccessException("Connection refused"));

            GetFulfillmentStockDetailCommand command = GetFulfillmentStockDetailCommand.of(
                    "CUST001", "valid-token", "GOD001", null
            );

            // when & then
            assertThatThrownBy(() -> client.getStockDetail(command))
                    .isInstanceOf(GetFulfillmentStockDetailFailedException.class);
        }
    }

    @Nested
    @DisplayName("sensitive path 분기")
    class SensitivePathDispatch {

        @Test
        @DisplayName("sensitivePaths에 등록된 stock 경로는 recordSensitive로 위임된다")
        void shouldDispatchRecordSensitiveWhenPathIsSensitive() {
            // given
            stubStockProperties();
            stubPayloadGenerator();
            String path = "/stock/{customerCode}";
            when(properties.getSensitivePaths()).thenReturn(Set.of(path));
            stubGetChainThrows(new ResourceAccessException("Connection refused"));

            GetFulfillmentStocksCommand command = GetFulfillmentStocksCommand.of("CUST001", "valid-token");

            // when
            assertThatThrownBy(() -> client.getStocks(command))
                    .isInstanceOf(GetFulfillmentStocksFailedException.class);

            // then
            verify(externalCallLogger).recordSensitive(
                    eq(ExternalSystem.FASSTO), eq("getStocks"), any(Map.class), any(Supplier.class)
            );
            verify(externalCallLogger, never()).record(
                    eq(ExternalSystem.FASSTO), eq("getStocks"), any(Map.class), any(Supplier.class)
            );
        }

        @Test
        @DisplayName("sensitivePaths에 미등록된 stock 경로는 record로 위임된다")
        void shouldDispatchRecordWhenPathIsNotSensitive() {
            // given
            stubStockProperties();
            stubPayloadGenerator();
            when(properties.getSensitivePaths()).thenReturn(Set.of());
            stubGetChainThrows(new ResourceAccessException("Connection refused"));

            GetFulfillmentStocksCommand command = GetFulfillmentStocksCommand.of("CUST001", "valid-token");

            // when
            assertThatThrownBy(() -> client.getStocks(command))
                    .isInstanceOf(GetFulfillmentStocksFailedException.class);

            // then
            verify(externalCallLogger).record(
                    eq(ExternalSystem.FASSTO), eq("getStocks"), any(Map.class), any(Supplier.class)
            );
            verify(externalCallLogger, never()).recordSensitive(
                    eq(ExternalSystem.FASSTO), eq("getStocks"), any(Map.class), any(Supplier.class)
            );
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

            GetFulfillmentStocksCommand command = GetFulfillmentStocksCommand.of("CUST001", "valid-token");

            // when & then
            assertThatThrownBy(() -> client.getStocks(command))
                    .isInstanceOf(FulfillmentConfigurationException.class);
        }
    }

    @Nested
    @DisplayName("cstGodCd raw 통과")
    class CstGodCdDecoding {

        private static final UUID TEST_PRODUCT_KEY = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        private static final String TEST_ENCODED_CST_GOD_CD = FasstoCustomerGoodsCodeEncoder.encode(TEST_PRODUCT_KEY);
        private static final String FALLBACK_PRODUCT_CODE = "PROD01";
        private static final String LEGACY_UUID_RAW = "550e8400-e29b-41d4-a716-446655440000";

        @Test
        @DisplayName("cstGodCd 22자 Base62 응답 → customerProductCode 22자 raw 그대로 통과")
        void shouldPassThroughBase62CstGodCdRaw() {
            // given
            stubStockProperties();
            stubPayloadGenerator();
            String body = String.format("""
                    {
                      "header": {"code": "200", "msg": "success", "dataCount": 1},
                      "data": [{
                        "whCd": "WH01",
                        "godCd": "GOD001",
                        "cstGodCd": "%s",
                        "godNm": "테스트 상품"
                      }]
                    }
                    """, TEST_ENCODED_CST_GOD_CD);
            stubGetChainReturns(new ResponseEntity<>(body, HttpStatus.OK));

            GetFulfillmentStocksCommand command = GetFulfillmentStocksCommand.of("CUST001", "valid-token");

            // when
            GetFulfillmentStocksResult result = client.getStocks(command);

            // then
            assertThat(result.stocks()).hasSize(1);
            assertThat(result.stocks().get(0).customerProductCode()).isEqualTo(TEST_ENCODED_CST_GOD_CD);
        }

        @Test
        @DisplayName("cstGodCd 폴백 productCode 6자 응답 → customerProductCode raw 통과")
        void shouldPassThroughFallbackProductCode() {
            // given
            stubStockProperties();
            stubPayloadGenerator();
            String body = String.format("""
                    {
                      "header": {"code": "200", "msg": "success", "dataCount": 1},
                      "data": [{
                        "whCd": "WH01",
                        "godCd": "GOD001",
                        "cstGodCd": "%s",
                        "godNm": "테스트 상품"
                      }]
                    }
                    """, FALLBACK_PRODUCT_CODE);
            stubGetChainReturns(new ResponseEntity<>(body, HttpStatus.OK));

            GetFulfillmentStocksCommand command = GetFulfillmentStocksCommand.of("CUST001", "valid-token");

            // when
            GetFulfillmentStocksResult result = client.getStocks(command);

            // then
            assertThat(result.stocks().get(0).customerProductCode()).isEqualTo(FALLBACK_PRODUCT_CODE);
        }

        @Test
        @DisplayName("cstGodCd 36자 UUID 형태 응답(이전 마이그레이션 잔존) → raw 통과")
        void shouldPassThroughLegacyUuidRaw() {
            // given
            stubStockProperties();
            stubPayloadGenerator();
            String body = String.format("""
                    {
                      "header": {"code": "200", "msg": "success", "dataCount": 1},
                      "data": [{
                        "whCd": "WH01",
                        "godCd": "GOD001",
                        "cstGodCd": "%s",
                        "godNm": "테스트 상품"
                      }]
                    }
                    """, LEGACY_UUID_RAW);
            stubGetChainReturns(new ResponseEntity<>(body, HttpStatus.OK));

            GetFulfillmentStocksCommand command = GetFulfillmentStocksCommand.of("CUST001", "valid-token");

            // when
            GetFulfillmentStocksResult result = client.getStocks(command);

            // then
            assertThat(result.stocks().get(0).customerProductCode()).isEqualTo(LEGACY_UUID_RAW);
        }
    }

    @Nested
    @DisplayName("record targetId(productKey) 전달")
    class RecordTargetIdProductKey {

        private static final UUID TEST_PRODUCT_KEY = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        private String successBody() {
            return """
                    {"header": {"code": "200", "msg": "OK", "dataCount": 0}, "data": [], "meta": null}
                    """;
        }

        @Test
        @DisplayName("getStockDetail 성공 시 cstGodCd 22자 raw가 record targetId로 전달된다")
        void shouldPassRawCstGodCdAsTargetIdOnGetStockDetailSuccess() {
            // given
            stubStockProperties();
            stubPayloadGenerator();
            stubGetChainReturns(new ResponseEntity<>(successBody(), HttpStatus.OK));

            GetFulfillmentStockDetailCommand command = GetFulfillmentStockDetailCommand.forProductKey(
                    "CUST001", "valid-token", TEST_PRODUCT_KEY, null
            );

            // when
            client.getStockDetail(command);

            // then
            // forProductKey는 fasstoProductCode=null + productKey 채움 → 매퍼 폴백이 encodeOrFallback(productKey, productKey.toString()) → encode(productKey) 22자 Base62 반환
            String expectedTargetId = FasstoCustomerGoodsCodeEncoder.encode(TEST_PRODUCT_KEY);
            verify(vendorCommunicationRecorder).record(
                    eq(FulfillmentVendorCommunicationTargetType.STOCK),
                    eq(FulfillmentVendorCommunicationType.REQUEST),
                    eq(FulfillmentVendorCommunicationSenderType.SERVER),
                    eq(expectedTargetId),
                    eq(FulfillmentVendorName.FASSTO),
                    any(String.class),
                    any(),
                    isNull()
            );
            verify(vendorCommunicationRecorder).record(
                    eq(FulfillmentVendorCommunicationTargetType.STOCK),
                    eq(FulfillmentVendorCommunicationType.RESPONSE),
                    eq(FulfillmentVendorCommunicationSenderType.VENDOR),
                    eq(expectedTargetId),
                    eq(FulfillmentVendorName.FASSTO),
                    any(String.class),
                    any(),
                    isNull()
            );
        }

        @Test
        @DisplayName("getStockDetail 호출 시 customerProductCode 22자 Base62는 raw 그대로 targetId에 전달된다")
        void shouldPassRawBase62CustomerProductCodeForTargetId() {
            // given
            stubStockProperties();
            stubPayloadGenerator();
            String encoded = FasstoCustomerGoodsCodeEncoder.encode(TEST_PRODUCT_KEY);
            stubGetChainReturns(new ResponseEntity<>(successBody(), HttpStatus.OK));

            GetFulfillmentStockDetailCommand command = GetFulfillmentStockDetailCommand.of(
                    "CUST001", "valid-token", encoded, null
            );

            // when
            client.getStockDetail(command);

            // then
            verify(vendorCommunicationRecorder, times(2)).record(
                    eq(FulfillmentVendorCommunicationTargetType.STOCK),
                    any(FulfillmentVendorCommunicationType.class),
                    any(FulfillmentVendorCommunicationSenderType.class),
                    eq(encoded),
                    eq(FulfillmentVendorName.FASSTO),
                    any(String.class),
                    any(),
                    isNull()
            );
        }

        @Test
        @DisplayName("getStocks(list) 호출은 cstGodCd가 없어 record targetId에 null이 전달된다")
        void shouldPassNullTargetIdOnGetStocksList() {
            // given
            stubStockProperties();
            stubPayloadGenerator();
            stubGetChainReturns(new ResponseEntity<>(successBody(), HttpStatus.OK));

            GetFulfillmentStocksCommand command = GetFulfillmentStocksCommand.of("CUST001", "valid-token");

            // when
            client.getStocks(command);

            // then
            verify(vendorCommunicationRecorder, times(2)).record(
                    eq(FulfillmentVendorCommunicationTargetType.STOCK),
                    any(FulfillmentVendorCommunicationType.class),
                    any(FulfillmentVendorCommunicationSenderType.class),
                    isNull(),
                    eq(FulfillmentVendorName.FASSTO),
                    any(String.class),
                    any(),
                    isNull()
            );
        }
    }
}
