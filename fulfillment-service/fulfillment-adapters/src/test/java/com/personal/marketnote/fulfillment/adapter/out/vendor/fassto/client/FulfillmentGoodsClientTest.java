package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.goods.FulfillmentGoodsDetailQuery;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.goods.FulfillmentGoodsElementQuery;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentErrorInfo;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentGoodsElementResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentGoodsElementsResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentGoodsItemResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentGoodsListResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentResponseHeader;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.RegisterFulfillmentGoodsItemResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.RegisterFulfillmentGoodsResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.UpdateFulfillmentGoodsItemResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.UpdateFulfillmentGoodsResponse;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import com.personal.marketnote.fulfillment.exception.FulfillmentConfigurationException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentGoodsDetailFailedException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentGoodsElementsFailedException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentGoodsFailedException;
import com.personal.marketnote.fulfillment.exception.RegisterFulfillmentGoodsFailedException;
import com.personal.marketnote.fulfillment.exception.UpdateFulfillmentGoodsFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentGoodsDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentGoodsElementsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentGoodsItemCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentGoodsItemCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentGoodsElementsResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentGoodsResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentGoodsResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentGoodsResult;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    private void stubGoodsElementProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getGoodsElementPath()).thenReturn("/goods/element/{customerCode}");
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

    private UpdateFulfillmentGoodsCommand buildValidUpdateCommand() {
        UpdateFulfillmentGoodsItemCommand itemCommand = UpdateFulfillmentGoodsItemCommand.of(
                null,
                "PROD001",
                "테스트 상품",
                "1",
                "01",
                null, null, null, null, "SUP001",
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null, null
        );
        return UpdateFulfillmentGoodsCommand.of("CUST001", "valid-token", List.of(itemCommand));
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
    private void stubPatchChain(ResponseEntity<?> response) {
        when(restClient.method(HttpMethod.PATCH)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(java.net.URI.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(response);
    }

    @SuppressWarnings("unchecked")
    private void stubPatchChainThrows(Exception exception) {
        when(restClient.method(HttpMethod.PATCH)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(java.net.URI.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(Class.class))).thenThrow(exception);
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

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private FulfillmentGoodsItemResponse buildGoodsItemResponse() {
        return new FulfillmentGoodsItemResponse(
                "GOD001", "1", "테스트 상품", "Type", "N",
                "CPROD001", null, null, "CUST001", "Cust",
                "SUP001", "Sup", "CAT001", "Category", "SEASON",
                "GENDER", "1000", "800", "1200", "NORMAL",
                "PF", "01", "GiftDiv", "10", "20", "30",
                "2026", "600", "100", "60", "18000",
                "BARCD", "20", "40", "50", "40000", "200",
                "INBARCD", "10", "20", "2000", "15", "50", "10",
                "10", "5", "2", "KR", "Y", "365", "30",
                "30", "BOX", "N", "UP", "20260101", "Y",
                "N", "1", "Y", "10"
        );
    }

    @Nested
    @DisplayName("registerGoods 성공")
    class RegisterGoodsSuccess {

        @Test
        @DisplayName("정상 응답 시 RegisterFulfillmentGoodsResult를 반환한다")
        void shouldReturnRegisterResultOnSuccessResponse() {
            // given
            stubGoodsProperties();
            stubPayloadGenerator();
            RegisterFulfillmentGoodsResponse payload = new RegisterFulfillmentGoodsResponse(
                    new FulfillmentResponseHeader("200", "success", 1),
                    List.of(new RegisterFulfillmentGoodsItemResponse("OK", "200", "CPROD001")),
                    null
            );
            stubPostChain(ResponseEntity.ok(toJson(payload)));

            // when
            RegisterFulfillmentGoodsResult result = client.registerGoods(buildValidRegisterCommand());

            // then
            assertThat(result.dataCount()).isEqualTo(1);
            assertThat(result.goods()).hasSize(1);
            assertThat(result.goods().get(0).customerProductCode()).isEqualTo("CPROD001");
            assertThat(result.goods().get(0).code()).isEqualTo("200");
        }
    }

    @Nested
    @DisplayName("updateGoods 성공")
    class UpdateGoodsSuccess {

        @Test
        @DisplayName("정상 응답 시 UpdateFulfillmentGoodsResult를 반환한다")
        void shouldReturnUpdateResultOnSuccessResponse() {
            // given
            stubGoodsProperties();
            stubPayloadGenerator();
            UpdateFulfillmentGoodsResponse payload = new UpdateFulfillmentGoodsResponse(
                    new FulfillmentResponseHeader("200", "success", 1),
                    List.of(new UpdateFulfillmentGoodsItemResponse("SLIP001", "ORD001", "OK", "200", null)),
                    null
            );
            stubPatchChain(ResponseEntity.ok(toJson(payload)));

            // when
            UpdateFulfillmentGoodsResult result = client.updateGoods(buildValidUpdateCommand());

            // then
            assertThat(result.dataCount()).isEqualTo(1);
            assertThat(result.goods()).hasSize(1);
            assertThat(result.goods().get(0).fulfillmentSlipNumber()).isEqualTo("SLIP001");
            assertThat(result.goods().get(0).code()).isEqualTo("200");
        }
    }

    @Nested
    @DisplayName("getGoods 성공")
    class GetGoodsSuccess {

        @Test
        @DisplayName("정상 응답 시 GetFulfillmentGoodsResult를 반환한다")
        void shouldReturnGoodsListOnSuccessResponse() {
            // given
            stubGoodsProperties();
            stubPayloadGenerator();
            FulfillmentGoodsListResponse payload = new FulfillmentGoodsListResponse(
                    new FulfillmentResponseHeader("200", "success", 1),
                    List.of(buildGoodsItemResponse()),
                    null
            );
            stubGetChain(ResponseEntity.ok(toJson(payload)));

            GetFulfillmentGoodsCommand command = GetFulfillmentGoodsCommand.of("CUST001", "valid-token");

            // when
            GetFulfillmentGoodsResult result = client.getGoods(command);

            // then
            assertThat(result.dataCount()).isEqualTo(1);
            assertThat(result.goods()).hasSize(1);
            assertThat(result.goods().get(0).productCode()).isEqualTo("GOD001");
        }
    }

    @Nested
    @DisplayName("getGoodsDetail 성공")
    class GetGoodsDetailSuccess {

        @Test
        @DisplayName("정상 응답 시 GetFulfillmentGoodsResult를 반환한다")
        void shouldReturnGoodsDetailOnSuccessResponse() {
            // given
            stubGoodsProperties();
            stubPayloadGenerator();
            FulfillmentGoodsListResponse payload = new FulfillmentGoodsListResponse(
                    new FulfillmentResponseHeader("200", "success", 1),
                    List.of(buildGoodsItemResponse()),
                    null
            );
            stubGetChain(ResponseEntity.ok(toJson(payload)));

            GetFulfillmentGoodsDetailCommand command = GetFulfillmentGoodsDetailCommand.of(
                    "CUST001", "valid-token", "테스트 상품"
            );

            // when
            GetFulfillmentGoodsResult result = client.getGoodsDetail(command);

            // then
            assertThat(result.dataCount()).isEqualTo(1);
            assertThat(result.goods()).hasSize(1);
            assertThat(result.goods().get(0).productName()).isEqualTo("테스트 상품");
        }
    }

    @Nested
    @DisplayName("getGoodsElements 성공")
    class GetGoodsElementsSuccess {

        @Test
        @DisplayName("정상 응답 시 GetFulfillmentGoodsElementsResult를 반환한다")
        void shouldReturnGoodsElementsOnSuccessResponse() {
            // given
            stubGoodsElementProperties();
            stubPayloadGenerator();
            FulfillmentGoodsElementsResponse payload = new FulfillmentGoodsElementsResponse(
                    new FulfillmentResponseHeader("200", "success", 1),
                    List.of(new FulfillmentGoodsElementResponse(
                            "GOD001", "CPROD001", "테스트 상품", "Y", List.of()
                    )),
                    null
            );
            stubGetChain(ResponseEntity.ok(toJson(payload)));

            GetFulfillmentGoodsElementsCommand command = GetFulfillmentGoodsElementsCommand.of(
                    "CUST001", "valid-token"
            );

            // when
            GetFulfillmentGoodsElementsResult result = client.getGoodsElements(command);

            // then
            assertThat(result.dataCount()).isEqualTo(1);
            assertThat(result.elements()).hasSize(1);
            assertThat(result.elements().get(0).productCode()).isEqualTo("GOD001");
        }
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

        @Test
        @DisplayName("5xx 응답 시 RegisterFulfillmentGoodsFailedException이 발생한다")
        void shouldThrowExceptionOn5xxResponse() {
            // given
            stubGoodsProperties();
            stubPayloadGenerator();
            stubPostChainThrows(new HttpServerErrorException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal Server Error",
                    "server boom".getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8
            ));

            RegisterFulfillmentGoodsCommand command = buildValidRegisterCommand();

            // when & then
            assertThatThrownBy(() -> client.registerGoods(command))
                    .isInstanceOf(RegisterFulfillmentGoodsFailedException.class);
        }
    }

    @Nested
    @DisplayName("updateGoods 실패")
    class UpdateGoodsFailure {

        @Test
        @DisplayName("4xx 응답 시 UpdateFulfillmentGoodsFailedException이 발생한다")
        void shouldThrowExceptionOn4xxResponse() {
            // given
            stubGoodsProperties();
            stubPayloadGenerator();
            stubPatchChainThrows(new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Bad Request",
                    "client error".getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8
            ));

            UpdateFulfillmentGoodsCommand command = buildValidUpdateCommand();

            // when & then
            assertThatThrownBy(() -> client.updateGoods(command))
                    .isInstanceOf(UpdateFulfillmentGoodsFailedException.class);
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
    @DisplayName("getGoodsDetail 실패")
    class GetGoodsDetailFailure {

        @Test
        @DisplayName("5xx 응답 시 GetFulfillmentGoodsDetailFailedException이 발생한다")
        void shouldThrowExceptionOn5xxResponse() {
            // given
            stubGoodsProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new HttpServerErrorException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal Server Error",
                    "server boom".getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8
            ));

            GetFulfillmentGoodsDetailCommand command = GetFulfillmentGoodsDetailCommand.of(
                    "CUST001", "valid-token", "테스트 상품"
            );

            // when & then
            assertThatThrownBy(() -> client.getGoodsDetail(command))
                    .isInstanceOf(GetFulfillmentGoodsDetailFailedException.class);
        }
    }

    @Nested
    @DisplayName("getGoodsElements 실패")
    class GetGoodsElementsFailure {

        @Test
        @DisplayName("4xx 응답 시 GetFulfillmentGoodsElementsFailedException이 발생한다")
        void shouldThrowExceptionOn4xxResponse() {
            // given
            stubGoodsElementProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Bad Request",
                    "client error".getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8
            ));

            GetFulfillmentGoodsElementsCommand command = GetFulfillmentGoodsElementsCommand.of(
                    "CUST001", "valid-token"
            );

            // when & then
            assertThatThrownBy(() -> client.getGoodsElements(command))
                    .isInstanceOf(GetFulfillmentGoodsElementsFailedException.class);
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

            GetFulfillmentGoodsCommand command = GetFulfillmentGoodsCommand.of("CUST001", "valid-token");

            // when & then
            assertThatThrownBy(() -> client.getGoods(command))
                    .isInstanceOf(FulfillmentConfigurationException.class);
        }
    }

    @Nested
    @DisplayName("Query validate")
    class QueryValidation {

        @Test
        @DisplayName("FulfillmentGoodsDetailQuery는 customerCode가 없으면 예외가 발생한다")
        void shouldThrowWhenDetailQueryMissesCustomerCode() {
            assertThatThrownBy(() -> FulfillmentGoodsDetailQuery.of(null, "valid-token", "상품명"))
                    .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentGoodsDetailQuery는 accessToken이 없으면 예외가 발생한다")
        void shouldThrowWhenDetailQueryMissesAccessToken() {
            assertThatThrownBy(() -> FulfillmentGoodsDetailQuery.of("CUST001", null, "상품명"))
                    .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentGoodsDetailQuery는 godNm이 없으면 예외가 발생한다")
        void shouldThrowWhenDetailQueryMissesGodNm() {
            assertThatThrownBy(() -> FulfillmentGoodsDetailQuery.of("CUST001", "valid-token", null))
                    .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentGoodsElementQuery는 customerCode가 없으면 예외가 발생한다")
        void shouldThrowWhenElementQueryMissesCustomerCode() {
            assertThatThrownBy(() -> FulfillmentGoodsElementQuery.of(null, "valid-token"))
                    .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentGoodsElementQuery는 accessToken이 없으면 예외가 발생한다")
        void shouldThrowWhenElementQueryMissesAccessToken() {
            assertThatThrownBy(() -> FulfillmentGoodsElementQuery.of("CUST001", null))
                    .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }
    }

    @Nested
    @DisplayName("INVALID_ACCESS 감지")
    class InvalidAccessDetection {

        @Test
        @DisplayName("응답 헤더가 INVALID_ACCESS이면 FulfillmentInvalidAccessException이 발생한다")
        void shouldThrowInvalidAccessExceptionOnInvalidAccessHeader() {
            // given
            stubGoodsProperties();
            stubPayloadGenerator();
            FulfillmentGoodsListResponse payload = new FulfillmentGoodsListResponse(
                    new FulfillmentResponseHeader("401", "INVALID_ACCESS", 0),
                    List.of(),
                    null
            );
            stubGetChain(ResponseEntity.ok(toJson(payload)));

            GetFulfillmentGoodsCommand command = GetFulfillmentGoodsCommand.of("CUST001", "stale-token");

            // when & then
            assertThatThrownBy(() -> client.getGoods(command))
                    .isInstanceOf(com.personal.marketnote.fulfillment.exception.FulfillmentInvalidAccessException.class);
        }

        @Test
        @DisplayName("errorInfo가 INVALID_ACCESS이면 FulfillmentInvalidAccessException이 발생한다")
        void shouldThrowInvalidAccessExceptionOnInvalidAccessErrorInfo() {
            // given
            stubGoodsElementProperties();
            stubPayloadGenerator();
            FulfillmentGoodsElementsResponse payload = new FulfillmentGoodsElementsResponse(
                    new FulfillmentResponseHeader("400", "bad", 0),
                    List.of(),
                    new FulfillmentErrorInfo("INVALID_ACCESS", "Access denied", null)
            );
            stubGetChain(ResponseEntity.ok(toJson(payload)));

            GetFulfillmentGoodsElementsCommand command = GetFulfillmentGoodsElementsCommand.of(
                    "CUST001", "stale-token"
            );

            // when & then
            assertThatThrownBy(() -> client.getGoodsElements(command))
                    .isInstanceOf(com.personal.marketnote.fulfillment.exception.FulfillmentInvalidAccessException.class);
        }
    }
}
