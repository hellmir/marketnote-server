package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentErrorInfo;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentResponseHeader;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentWarehousingAbnormalItemResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentWarehousingAbnormalListResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentWarehousingDetailItemResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentWarehousingDetailListResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentWarehousingInspecDetailItemResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentWarehousingInspecDetailListResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentWarehousingItemResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.FulfillmentWarehousingListResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.RegisterFulfillmentWarehousingItemResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.response.RegisterFulfillmentWarehousingResponse;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.warehousing.FulfillmentWarehousingAbnormalQuery;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.warehousing.FulfillmentWarehousingDetailQuery;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.warehousing.FulfillmentWarehousingInspecDetailQuery;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.warehousing.FulfillmentWarehousingQuery;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import com.personal.marketnote.fulfillment.exception.FulfillmentConfigurationException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentWarehousingAbnormalFailedException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentWarehousingDetailFailedException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentWarehousingFailedException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentWarehousingInspecDetailFailedException;
import com.personal.marketnote.fulfillment.exception.RegisterFulfillmentWarehousingFailedException;
import com.personal.marketnote.fulfillment.exception.UpdateFulfillmentWarehousingFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingAbnormalCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingInspecDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentWarehousingCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentWarehousingGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentWarehousingItemCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentWarehousingCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentWarehousingGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentWarehousingItemCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingAbnormalResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingDetailResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingInspecDetailResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentWarehousingResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentWarehousingResult;
import com.personal.marketnote.fulfillment.utility.VendorCommunicationFailureHandler;
import com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.fulfillment.utility.VendorCommunicationRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@DisplayName("FulfillmentWarehousingClient 테스트")
class FulfillmentWarehousingClientTest {

    private FulfillmentWarehousingClient client;

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

        client = new FulfillmentWarehousingClient(
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

    private void stubWarehousingMutationProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getWarehousingPath()).thenReturn("/warehousing/{customerCode}");
    }

    private void stubWarehousingListProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getWarehousingListPath()).thenReturn("/warehousing/{customerCode}/{startDate}/{endDate}");
    }

    private void stubWarehousingDetailProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getWarehousingDetailPath()).thenReturn("/warehousing/detail/{customerCode}/{slipNo}");
    }

    private void stubWarehousingInspecDetailProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getWarehousingInspecDetailPath())
                .thenReturn("/warehousing/inspec/{customerCode}/{slipNo}/{whCd}");
    }

    private void stubWarehousingAbnormalProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getWarehousingAbnormalPath())
                .thenReturn("/warehousing/abnormal/{customerCode}/{whCd}/{slipNo}");
    }

    private RegisterFulfillmentWarehousingCommand buildValidRegisterCommand() {
        RegisterFulfillmentWarehousingGoodsCommand goodsCommand = RegisterFulfillmentWarehousingGoodsCommand.of(
                "PROD001", null, 10
        );
        RegisterFulfillmentWarehousingItemCommand itemCommand = RegisterFulfillmentWarehousingItemCommand.builder()
                .orderDate("20260414")
                .orderNumber("ORD001")
                .warehousingMethod("01")
                .supplierCode("SUP001")
                .products(List.of(goodsCommand))
                .build();
        return RegisterFulfillmentWarehousingCommand.of("CUST001", "valid-token", List.of(itemCommand));
    }

    private UpdateFulfillmentWarehousingCommand buildValidUpdateCommand() {
        UpdateFulfillmentWarehousingGoodsCommand goodsCommand = UpdateFulfillmentWarehousingGoodsCommand.of(
                null, "PROD001", null, 10
        );
        UpdateFulfillmentWarehousingItemCommand itemCommand = UpdateFulfillmentWarehousingItemCommand.of(
                "20260414",
                "ORD001",
                "01",
                "SLIP001",
                null,
                null,
                null,
                "SUP001",
                null,
                null,
                null,
                List.of(goodsCommand)
        );
        return UpdateFulfillmentWarehousingCommand.of("CUST001", "valid-token", List.of(itemCommand));
    }

    @SuppressWarnings("unchecked")
    private void stubMethodChain(ResponseEntity<?> response) {
        when(restClient.method(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(java.net.URI.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(response);
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

    @Nested
    @DisplayName("registerWarehousing 성공")
    class RegisterWarehousingSuccess {

        @Test
        @DisplayName("정상 응답 시 RegisterFulfillmentWarehousingResult를 반환한다")
        void shouldReturnRegisterResultOnSuccessResponse() {
            // given
            stubWarehousingMutationProperties();
            stubPayloadGenerator();
            RegisterFulfillmentWarehousingResponse payload = new RegisterFulfillmentWarehousingResponse(
                    new FulfillmentResponseHeader("200", "success", 1),
                    List.of(new RegisterFulfillmentWarehousingItemResponse("OK", "200", "SLIP001", "ORD001")),
                    null
            );
            stubMethodChain(ResponseEntity.ok(toJson(payload)));

            // when
            RegisterFulfillmentWarehousingResult result = client.registerWarehousing(buildValidRegisterCommand());

            // then
            assertThat(result.dataCount()).isEqualTo(1);
            assertThat(result.warehousing()).hasSize(1);
            assertThat(result.warehousing().get(0).slipNumber()).isEqualTo("SLIP001");
            assertThat(result.warehousing().get(0).orderNumber()).isEqualTo("ORD001");
        }
    }

    @Nested
    @DisplayName("updateWarehousing 성공")
    class UpdateWarehousingSuccess {

        @Test
        @DisplayName("정상 응답 시 UpdateFulfillmentWarehousingResult를 반환한다")
        void shouldReturnUpdateResultOnSuccessResponse() {
            // given
            stubWarehousingMutationProperties();
            stubPayloadGenerator();
            RegisterFulfillmentWarehousingResponse payload = new RegisterFulfillmentWarehousingResponse(
                    new FulfillmentResponseHeader("200", "success", 1),
                    List.of(new RegisterFulfillmentWarehousingItemResponse("OK", "200", "SLIP001", "ORD001")),
                    null
            );
            stubMethodChain(ResponseEntity.ok(toJson(payload)));

            // when
            UpdateFulfillmentWarehousingResult result = client.updateWarehousing(buildValidUpdateCommand());

            // then
            assertThat(result.dataCount()).isEqualTo(1);
            assertThat(result.warehousing()).hasSize(1);
            assertThat(result.warehousing().get(0).slipNumber()).isEqualTo("SLIP001");
        }
    }

    @Nested
    @DisplayName("getWarehousing 성공")
    class GetWarehousingSuccess {

        @Test
        @DisplayName("정상 응답 시 GetFulfillmentWarehousingResult를 반환한다")
        void shouldReturnListResultOnSuccessResponse() {
            // given
            stubWarehousingListProperties();
            stubPayloadGenerator();
            FulfillmentWarehousingListResponse payload = new FulfillmentWarehousingListResponse(
                    new FulfillmentResponseHeader("200", "success", 1),
                    List.of(new FulfillmentWarehousingItemResponse(
                            "20260414", "WH01", "Main",
                            "ORD001", "SLIP001", "CUST001", "Cust",
                            "SUP001", "SUPCUST", 1, "Sup",
                            1, 1, "01", "Parcel",
                            "CJ", "INV001", "A", "Arrived",
                            "N", "remark", null
                    )),
                    null
            );
            stubGetChain(ResponseEntity.ok(toJson(payload)));

            GetFulfillmentWarehousingCommand command = GetFulfillmentWarehousingCommand.of(
                    "CUST001", "valid-token", "20260101", "20260131"
            );

            // when
            GetFulfillmentWarehousingResult result = client.getWarehousing(command);

            // then
            assertThat(result.dataCount()).isEqualTo(1);
            assertThat(result.warehousing()).hasSize(1);
            assertThat(result.warehousing().get(0).slipNumber()).isEqualTo("SLIP001");
        }
    }

    @Nested
    @DisplayName("getWarehousingDetail 성공")
    class GetWarehousingDetailSuccess {

        @Test
        @DisplayName("정상 응답 시 GetFulfillmentWarehousingDetailResult를 반환한다")
        void shouldReturnDetailResultOnSuccessResponse() {
            // given
            stubWarehousingDetailProperties();
            stubPayloadGenerator();
            FulfillmentWarehousingDetailListResponse payload = new FulfillmentWarehousingDetailListResponse(
                    new FulfillmentResponseHeader("200", "success", 1),
                    List.of(new FulfillmentWarehousingDetailItemResponse(
                            "20260414", "WH01", "Main", "SLIP001", "ORD001",
                            "CUST001", "Cust", "SUP001", "Sup", "SUPCUST",
                            1, 1, 1, 0, "01", "Parcel",
                            "CJ", "INV001", "A", "Arrived",
                            "N", "remark", List.of()
                    )),
                    null
            );
            stubGetChain(ResponseEntity.ok(toJson(payload)));

            GetFulfillmentWarehousingDetailCommand command = GetFulfillmentWarehousingDetailCommand.of(
                    "CUST001", "valid-token", "SLIP001", null
            );

            // when
            GetFulfillmentWarehousingDetailResult result = client.getWarehousingDetail(command);

            // then
            assertThat(result.dataCount()).isEqualTo(1);
            assertThat(result.details()).hasSize(1);
            assertThat(result.details().get(0).slipNumber()).isEqualTo("SLIP001");
        }
    }

    @Nested
    @DisplayName("getWarehousingInspecDetail 성공")
    class GetWarehousingInspecDetailSuccess {

        @Test
        @DisplayName("정상 응답 시 GetFulfillmentWarehousingInspecDetailResult를 반환한다")
        void shouldReturnInspecDetailResultOnSuccessResponse() {
            // given
            stubWarehousingInspecDetailProperties();
            stubPayloadGenerator();
            FulfillmentWarehousingInspecDetailListResponse payload = new FulfillmentWarehousingInspecDetailListResponse(
                    new FulfillmentResponseHeader("200", "success", 1),
                    List.of(new FulfillmentWarehousingInspecDetailItemResponse(
                            "20260414", "WH01", "Main", "SLIP001",
                            "CUST001", "Cust", "SUP001", "Sup",
                            "01", "Parcel", "GOD001", 1, 1,
                            "CJ", "INV001", "A", "Arrived", "remark",
                            null, null, null, null
                    )),
                    null
            );
            stubGetChain(ResponseEntity.ok(toJson(payload)));

            GetFulfillmentWarehousingInspecDetailCommand command = GetFulfillmentWarehousingInspecDetailCommand.of(
                    "CUST001", "valid-token", "SLIP001", "WH01"
            );

            // when
            GetFulfillmentWarehousingInspecDetailResult result = client.getWarehousingInspecDetail(command);

            // then
            assertThat(result.dataCount()).isEqualTo(1);
            assertThat(result.details()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getWarehousingAbnormal 성공")
    class GetWarehousingAbnormalSuccess {

        @Test
        @DisplayName("정상 응답 시 GetFulfillmentWarehousingAbnormalResult를 반환한다")
        void shouldReturnAbnormalResultOnSuccessResponse() {
            // given
            stubWarehousingAbnormalProperties();
            stubPayloadGenerator();
            FulfillmentWarehousingAbnormalListResponse payload = new FulfillmentWarehousingAbnormalListResponse(
                    new FulfillmentResponseHeader("200", "success", 1),
                    List.of(new FulfillmentWarehousingAbnormalItemResponse(
                            "SLIP001", "SERIAL001", "OK", "WH01",
                            "CUST001", "Cust", "GOD001", "desc", "remark",
                            "FS1", 1, "20260414", "Reg",
                            "20260414", "Upd", "FN1", null
                    )),
                    null
            );
            stubGetChain(ResponseEntity.ok(toJson(payload)));

            GetFulfillmentWarehousingAbnormalCommand command = GetFulfillmentWarehousingAbnormalCommand.of(
                    "CUST001", "valid-token", "WH01", "SLIP001"
            );

            // when
            GetFulfillmentWarehousingAbnormalResult result = client.getWarehousingAbnormal(command);

            // then
            assertThat(result.dataCount()).isEqualTo(1);
            assertThat(result.abnormals()).hasSize(1);
            assertThat(result.abnormals().get(0).slipNumber()).isEqualTo("SLIP001");
        }
    }

    @Nested
    @DisplayName("registerWarehousing 실패")
    class RegisterWarehousingFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 RegisterFulfillmentWarehousingFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubWarehousingMutationProperties();
            stubPayloadGenerator();
            stubMethodChainThrows(new RuntimeException("Connection refused"));

            RegisterFulfillmentWarehousingCommand command = buildValidRegisterCommand();

            // when & then
            assertThatThrownBy(() -> client.registerWarehousing(command))
                    .isInstanceOf(RegisterFulfillmentWarehousingFailedException.class);
        }

        @Test
        @DisplayName("5xx 응답 시 RegisterFulfillmentWarehousingFailedException이 발생한다")
        void shouldThrowExceptionOn5xxResponse() {
            // given
            stubWarehousingMutationProperties();
            stubPayloadGenerator();
            stubMethodChainThrows(new org.springframework.web.client.HttpServerErrorException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal Server Error",
                    "server boom".getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8
            ));

            RegisterFulfillmentWarehousingCommand command = buildValidRegisterCommand();

            // when & then
            assertThatThrownBy(() -> client.registerWarehousing(command))
                    .isInstanceOf(RegisterFulfillmentWarehousingFailedException.class);
        }
    }

    @Nested
    @DisplayName("updateWarehousing 실패")
    class UpdateWarehousingFailure {

        @Test
        @DisplayName("4xx 응답 시 UpdateFulfillmentWarehousingFailedException이 발생한다")
        void shouldThrowExceptionOn4xxResponse() {
            // given
            stubWarehousingMutationProperties();
            stubPayloadGenerator();
            stubMethodChainThrows(new org.springframework.web.client.HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Bad Request",
                    "client error".getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8
            ));

            UpdateFulfillmentWarehousingCommand command = buildValidUpdateCommand();

            // when & then
            assertThatThrownBy(() -> client.updateWarehousing(command))
                    .isInstanceOf(UpdateFulfillmentWarehousingFailedException.class);
        }
    }

    @Nested
    @DisplayName("getWarehousing 실패")
    class GetWarehousingFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 GetFulfillmentWarehousingFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubWarehousingListProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new RuntimeException("Connection refused"));

            GetFulfillmentWarehousingCommand command = GetFulfillmentWarehousingCommand.of(
                    "CUST001", "valid-token", "20260101", "20260131"
            );

            // when & then
            assertThatThrownBy(() -> client.getWarehousing(command))
                    .isInstanceOf(GetFulfillmentWarehousingFailedException.class);
        }
    }

    @Nested
    @DisplayName("getWarehousingDetail 실패")
    class GetWarehousingDetailFailure {

        @Test
        @DisplayName("4xx 응답 시 GetFulfillmentWarehousingDetailFailedException이 발생한다")
        void shouldThrowExceptionOn4xxResponse() {
            // given
            stubWarehousingDetailProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new org.springframework.web.client.HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Bad Request",
                    "client error".getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8
            ));

            GetFulfillmentWarehousingDetailCommand command = GetFulfillmentWarehousingDetailCommand.of(
                    "CUST001", "valid-token", "SLIP001", null
            );

            // when & then
            assertThatThrownBy(() -> client.getWarehousingDetail(command))
                    .isInstanceOf(GetFulfillmentWarehousingDetailFailedException.class);
        }
    }

    @Nested
    @DisplayName("getWarehousingInspecDetail 실패")
    class GetWarehousingInspecDetailFailure {

        @Test
        @DisplayName("5xx 응답 시 GetFulfillmentWarehousingInspecDetailFailedException이 발생한다")
        void shouldThrowExceptionOn5xxResponse() {
            // given
            stubWarehousingInspecDetailProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new org.springframework.web.client.HttpServerErrorException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal Server Error",
                    "server error".getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8
            ));

            GetFulfillmentWarehousingInspecDetailCommand command = GetFulfillmentWarehousingInspecDetailCommand.of(
                    "CUST001", "valid-token", "SLIP001", "WH01"
            );

            // when & then
            assertThatThrownBy(() -> client.getWarehousingInspecDetail(command))
                    .isInstanceOf(GetFulfillmentWarehousingInspecDetailFailedException.class);
        }
    }

    @Nested
    @DisplayName("getWarehousingAbnormal 실패")
    class GetWarehousingAbnormalFailure {

        @Test
        @DisplayName("모든 재시도 실패 시 GetFulfillmentWarehousingAbnormalFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubWarehousingAbnormalProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new RuntimeException("Connection refused"));

            GetFulfillmentWarehousingAbnormalCommand command = GetFulfillmentWarehousingAbnormalCommand.of(
                    "CUST001", "valid-token", "WH01", "SLIP001"
            );

            // when & then
            assertThatThrownBy(() -> client.getWarehousingAbnormal(command))
                    .isInstanceOf(GetFulfillmentWarehousingAbnormalFailedException.class);
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

            GetFulfillmentWarehousingCommand command = GetFulfillmentWarehousingCommand.of(
                    "CUST001", "valid-token", "20260101", "20260131"
            );

            // when & then
            assertThatThrownBy(() -> client.getWarehousing(command))
                    .isInstanceOf(FulfillmentConfigurationException.class);
        }
    }

    @Nested
    @DisplayName("Query validate")
    class QueryValidation {

        @Test
        @DisplayName("FulfillmentWarehousingDetailQuery는 slipNo가 없으면 예외가 발생한다")
        void shouldThrowWhenDetailQueryMissesSlipNo() {
            assertThatThrownBy(() -> FulfillmentWarehousingDetailQuery.of(
                    "CUST001", "valid-token", null, null
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentWarehousingDetailQuery는 customerCode가 없으면 예외가 발생한다")
        void shouldThrowWhenDetailQueryMissesCustomerCode() {
            assertThatThrownBy(() -> FulfillmentWarehousingDetailQuery.of(
                    null, "valid-token", "SLIP001", null
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentWarehousingDetailQuery는 accessToken이 없으면 예외가 발생한다")
        void shouldThrowWhenDetailQueryMissesAccessToken() {
            assertThatThrownBy(() -> FulfillmentWarehousingDetailQuery.of(
                    "CUST001", null, "SLIP001", null
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentWarehousingInspecDetailQuery는 whCd가 없으면 예외가 발생한다")
        void shouldThrowWhenInspecDetailQueryMissesWhCd() {
            assertThatThrownBy(() -> FulfillmentWarehousingInspecDetailQuery.of(
                    "CUST001", "valid-token", "SLIP001", null
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentWarehousingInspecDetailQuery는 slipNo가 없으면 예외가 발생한다")
        void shouldThrowWhenInspecDetailQueryMissesSlipNo() {
            assertThatThrownBy(() -> FulfillmentWarehousingInspecDetailQuery.of(
                    "CUST001", "valid-token", null, "WH01"
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentWarehousingAbnormalQuery는 whCd가 없으면 예외가 발생한다")
        void shouldThrowWhenAbnormalQueryMissesWhCd() {
            assertThatThrownBy(() -> FulfillmentWarehousingAbnormalQuery.of(
                    "CUST001", "valid-token", null, "SLIP001"
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentWarehousingAbnormalQuery는 slipNo가 없으면 예외가 발생한다")
        void shouldThrowWhenAbnormalQueryMissesSlipNo() {
            assertThatThrownBy(() -> FulfillmentWarehousingAbnormalQuery.of(
                    "CUST001", "valid-token", "WH01", null
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentWarehousingQuery는 startDate가 없으면 예외가 발생한다")
        void shouldThrowWhenListQueryMissesStartDate() {
            assertThatThrownBy(() -> FulfillmentWarehousingQuery.of(
                    "CUST001", "valid-token", null, "20260131"
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentWarehousingQuery는 endDate가 없으면 예외가 발생한다")
        void shouldThrowWhenListQueryMissesEndDate() {
            assertThatThrownBy(() -> FulfillmentWarehousingQuery.of(
                    "CUST001", "valid-token", "20260101", null
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }
    }

    @Nested
    @DisplayName("INVALID_ACCESS 감지")
    class InvalidAccessDetection {

        @Test
        @DisplayName("응답 헤더가 INVALID_ACCESS이면 FulfillmentInvalidAccessException이 발생한다")
        void shouldThrowInvalidAccessExceptionOnInvalidAccessHeader() {
            // given
            stubWarehousingListProperties();
            stubPayloadGenerator();
            FulfillmentWarehousingListResponse payload = new FulfillmentWarehousingListResponse(
                    new FulfillmentResponseHeader("401", "INVALID_ACCESS", 0),
                    List.of(),
                    null
            );
            stubGetChain(ResponseEntity.ok(toJson(payload)));

            GetFulfillmentWarehousingCommand command = GetFulfillmentWarehousingCommand.of(
                    "CUST001", "stale-token", "20260101", "20260131"
            );

            // when & then
            assertThatThrownBy(() -> client.getWarehousing(command))
                    .isInstanceOf(com.personal.marketnote.fulfillment.exception.FulfillmentInvalidAccessException.class);
        }

        @Test
        @DisplayName("errorInfo가 INVALID_ACCESS이면 FulfillmentInvalidAccessException이 발생한다")
        void shouldThrowInvalidAccessExceptionOnInvalidAccessErrorInfo() {
            // given
            stubWarehousingListProperties();
            stubPayloadGenerator();
            FulfillmentWarehousingListResponse payload = new FulfillmentWarehousingListResponse(
                    new FulfillmentResponseHeader("400", "bad", 0),
                    List.of(),
                    new FulfillmentErrorInfo("INVALID_ACCESS", "Access denied", null)
            );
            stubGetChain(ResponseEntity.ok(toJson(payload)));

            GetFulfillmentWarehousingCommand command = GetFulfillmentWarehousingCommand.of(
                    "CUST001", "stale-token", "20260101", "20260131"
            );

            // when & then
            assertThatThrownBy(() -> client.getWarehousing(command))
                    .isInstanceOf(com.personal.marketnote.fulfillment.exception.FulfillmentInvalidAccessException.class);
        }
    }
}
