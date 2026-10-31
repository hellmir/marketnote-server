package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery.FulfillmentDeliveryDetailQuery;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery.FulfillmentDeliveryGoodDetailQuery;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery.FulfillmentDeliveryOutOrdGoodsByOrdNoQuery;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery.FulfillmentDeliveryOutOrdGoodsDetailQuery;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery.FulfillmentDeliveryQuery;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery.FulfillmentDeliveryStatusQuery;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import com.personal.marketnote.fulfillment.exception.CancelFulfillmentDeliveryFailedException;
import com.personal.marketnote.fulfillment.exception.CompleteFulfillmentDeliveryIcsFailedException;
import com.personal.marketnote.fulfillment.exception.FulfillmentConfigurationException;
import com.personal.marketnote.fulfillment.exception.FulfillmentInvalidAccessException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentDeliveriesFailedException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentDeliveryDetailFailedException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentDeliveryGoodDetailFailedException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentDeliveryOutOrdGoodsByOrdNoFailedException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentDeliveryOutOrdGoodsDetailFailedException;
import com.personal.marketnote.fulfillment.exception.GetFulfillmentDeliveryStatusesFailedException;
import com.personal.marketnote.fulfillment.exception.RegisterFulfillmentDeliveryFailedException;
import com.personal.marketnote.fulfillment.exception.UpdateFulfillmentDeliveryCarFailedException;
import com.personal.marketnote.fulfillment.exception.UpdateFulfillmentDeliveryFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.CancelFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.CancelFulfillmentDeliveryItemCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.CompleteFulfillmentDeliveryIcsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.CompleteFulfillmentDeliveryIcsItemCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveriesCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryGoodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryOutOrdGoodsByOrdNoCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryOutOrdGoodsDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryStatusesCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryCarCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryCarItemCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryIcsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryIcsItemCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryItemCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentDeliveryCarCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentDeliveryCarItemCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentDeliveryItemCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.CancelFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.CompleteFulfillmentDeliveryIcsResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveriesResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryDetailResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryGoodDetailResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryOutOrdGoodsDetailResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryStatusesResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;
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

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentDeliveryClient 테스트")
class FulfillmentDeliveryClientTest {

    private FulfillmentDeliveryClient client;

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

        client = new FulfillmentDeliveryClient(
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

    private void stubDeliveryMutationProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getDeliveryPath()).thenReturn("/delivery/{customerCode}");
    }

    private void stubDeliveryCarMutationProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getDeliveryCarPath()).thenReturn("/delivery/car/{customerCode}");
    }

    private void stubDeliveryIcsMutationProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getDeliveryIcsPath()).thenReturn("/delivery/ics/{customerCode}");
    }

    private void stubDeliveryIcsCompletedProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getDeliveryIcsCompletedPath()).thenReturn("/delivery/ics/completed/{customerCode}");
    }

    private void stubDeliveryCancelProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getDeliveryCancelPath()).thenReturn("/delivery/cancel/{customerCode}");
    }

    private void stubDeliveryListProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getDeliveryListPath()).thenReturn("/delivery/{customerCode}/{startDate}/{endDate}/{status}/{outDiv}");
    }

    private void stubDeliveryStatusProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getDeliveryStatusPath())
                .thenReturn("/delivery/parcel/{customerCode}/{startDate}/{endDate}/{outDiv}");
    }

    private void stubDeliveryDetailProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getDeliveryDetailPath()).thenReturn("/delivery/detail/{customerCode}/{slipNo}");
    }

    private void stubOutOrdGoodsDetailProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getDeliveryOutOrdGoodsDetailPath())
                .thenReturn("/delivery/outOrd/goodsDetail/{customerCode}");
    }

    private void stubOutOrdGoodsByOrdNoProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getDeliveryOutOrdGoodsByOrdNoPath())
                .thenReturn("/delivery/outOrd/ordNo/{customerCode}/{startDate}/{endDate}");
    }

    private void stubDeliveryGoodDetailProperties() {
        when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
        when(properties.getDeliveryGoodDetailPath())
                .thenReturn("/delivery/goodDetail/{customerCode}/{startDate}/{endDate}");
    }

    private RegisterFulfillmentDeliveryCommand buildValidRegisterCommand() {
        RegisterFulfillmentDeliveryGoodsCommand goodsCommand = RegisterFulfillmentDeliveryGoodsCommand.of(
                "PROD001", "20260414", 1
        );
        RegisterFulfillmentDeliveryItemCommand itemCommand = RegisterFulfillmentDeliveryItemCommand.builder()
                .orderNumber("ORD001")
                .orderDate("20260414")
                .recipientName("홍길동")
                .recipientPhoneNumber("01012345678")
                .recipientAddress("서울시 강남구")
                .releaseMethod("01")
                .products(List.of(goodsCommand))
                .build();
        return RegisterFulfillmentDeliveryCommand.of("CUST001", "valid-token", List.of(itemCommand));
    }

    private UpdateFulfillmentDeliveryCommand buildValidUpdateCommand() {
        RegisterFulfillmentDeliveryGoodsCommand goodsCommand = RegisterFulfillmentDeliveryGoodsCommand.of(
                "PROD001", "20260414", 1
        );
        UpdateFulfillmentDeliveryItemCommand itemCommand = UpdateFulfillmentDeliveryItemCommand.builder()
                .orderNumber("ORD001")
                .orderDate("20260414")
                .recipientName("홍길동")
                .recipientPhoneNumber("01012345678")
                .recipientAddress("서울시 강남구")
                .releaseMethod("01")
                .slipNumber("SLIP001")
                .products(List.of(goodsCommand))
                .build();
        return UpdateFulfillmentDeliveryCommand.of("CUST001", "valid-token", List.of(itemCommand));
    }

    private RegisterFulfillmentDeliveryCarCommand buildValidRegisterCarCommand() {
        RegisterFulfillmentDeliveryGoodsCommand goodsCommand = RegisterFulfillmentDeliveryGoodsCommand.of(
                "PROD001", "20260414", 1
        );
        RegisterFulfillmentDeliveryCarItemCommand itemCommand = RegisterFulfillmentDeliveryCarItemCommand.builder()
                .orderNumber("ORD001")
                .orderDate("20260414")
                .releaseMethod("01")
                .shopCode("SHOP001")
                .products(List.of(goodsCommand))
                .build();
        return RegisterFulfillmentDeliveryCarCommand.of("CUST001", "valid-token", List.of(itemCommand));
    }

    private UpdateFulfillmentDeliveryCarCommand buildValidUpdateCarCommand() {
        RegisterFulfillmentDeliveryGoodsCommand goodsCommand = RegisterFulfillmentDeliveryGoodsCommand.of(
                "PROD001", "20260414", 1
        );
        UpdateFulfillmentDeliveryCarItemCommand itemCommand = UpdateFulfillmentDeliveryCarItemCommand.builder()
                .orderNumber("ORD001")
                .orderDate("20260414")
                .releaseMethod("01")
                .shopCode("SHOP001")
                .slipNumber("SLIP001")
                .products(List.of(goodsCommand))
                .build();
        return UpdateFulfillmentDeliveryCarCommand.of("CUST001", "valid-token", List.of(itemCommand));
    }

    private RegisterFulfillmentDeliveryIcsCommand buildValidRegisterIcsCommand() {
        RegisterFulfillmentDeliveryGoodsCommand goodsCommand = RegisterFulfillmentDeliveryGoodsCommand.of(
                "PROD001", "20260414", 1
        );
        RegisterFulfillmentDeliveryIcsItemCommand itemCommand = RegisterFulfillmentDeliveryIcsItemCommand.builder()
                .orderNumber("ORD001")
                .orderDate("20260414")
                .recipientName("홍길동")
                .recipientPhoneNumber("01012345678")
                .recipientAddress("서울시 강남구")
                .invoiceNumber("INV001")
                .logisticsCenter("LC01")
                .platform("PLATFORM")
                .products(List.of(goodsCommand))
                .build();
        return RegisterFulfillmentDeliveryIcsCommand.of("CUST001", "valid-token", List.of(itemCommand));
    }

    private CompleteFulfillmentDeliveryIcsCommand buildValidCompleteIcsCommand() {
        CompleteFulfillmentDeliveryIcsItemCommand itemCommand = CompleteFulfillmentDeliveryIcsItemCommand.of(
                List.of("ORD001")
        );
        return CompleteFulfillmentDeliveryIcsCommand.of("CUST001", "valid-token", List.of(itemCommand));
    }

    private CancelFulfillmentDeliveryCommand buildValidCancelCommand() {
        CancelFulfillmentDeliveryItemCommand itemCommand = CancelFulfillmentDeliveryItemCommand.of(
                "SLIP001", "ORD001"
        );
        return CancelFulfillmentDeliveryCommand.of("CUST001", "valid-token", List.of(itemCommand));
    }

    @SuppressWarnings("unchecked")
    private void stubMethodChainThrows(Exception exception) {
        when(restClient.method(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(URI.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).body(any(Object.class));
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        doThrow(exception).when(responseSpec).toEntity(any(Class.class));
    }

    @SuppressWarnings("unchecked")
    private void stubMethodChainReturns(ResponseEntity<String> entity) {
        when(restClient.method(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(URI.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).body(any(Object.class));
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        doReturn(entity).when(responseSpec).toEntity(any(Class.class));
    }

    @SuppressWarnings("unchecked")
    private void stubGetChainThrows(Exception exception) {
        RestClient.RequestHeadersUriSpec rawSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headerSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec respSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(rawSpec);
        when(rawSpec.uri(any(URI.class))).thenReturn(headerSpec);
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
        when(rawSpec.uri(any(URI.class))).thenReturn(headerSpec);
        when(headerSpec.headers(any())).thenReturn(headerSpec);
        when(headerSpec.retrieve()).thenReturn(respSpec);
        when(respSpec.toEntity(any(Class.class))).thenReturn(entity);
    }

    private ResponseEntity<String> successJson(String body) {
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    private ResponseEntity<String> responseWithStatus(String body, HttpStatus status) {
        return new ResponseEntity<>(body, status);
    }

    @Nested
    @DisplayName("registerDelivery")
    class RegisterDelivery {

        @Test
        @DisplayName("정상 응답 시 RegisterFulfillmentDeliveryResult로 매핑된다")
        void shouldMapRegisterDeliverySuccessResponse() {
            // given
            stubDeliveryMutationProperties();
            stubPayloadGenerator();
            String body = """
                    {
                      "header": {"code": "200", "msg": "success", "dataCount": 1},
                      "data": [{"code": "200", "msg": "OK", "fmsSlipNo": "FMS001", "orderNo": "ORD001"}]
                    }
                    """;
            stubMethodChainReturns(successJson(body));

            // when
            RegisterFulfillmentDeliveryResult result = client.registerDelivery(buildValidRegisterCommand());

            // then
            assertThat(result).isNotNull();
            assertThat(result.dataCount()).isEqualTo(1);
            assertThat(result.deliveries()).hasSize(1);
            assertThat(result.deliveries().get(0).fulfillmentSlipNumber()).isEqualTo("FMS001");
            assertThat(result.deliveries().get(0).orderNumber()).isEqualTo("ORD001");
        }

        @Test
        @DisplayName("모든 재시도 실패 시 RegisterFulfillmentDeliveryFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubDeliveryMutationProperties();
            stubPayloadGenerator();
            stubMethodChainThrows(new RuntimeException("Connection refused"));

            RegisterFulfillmentDeliveryCommand command = buildValidRegisterCommand();

            // when & then
            assertThatThrownBy(() -> client.registerDelivery(command))
                    .isInstanceOf(RegisterFulfillmentDeliveryFailedException.class);
        }

        @Test
        @DisplayName("응답 body에 INVALID_ACCESS 마커가 포함되면 FulfillmentInvalidAccessException이 발생한다")
        void shouldThrowInvalidAccessExceptionWhenMarkerFound() {
            // given
            stubDeliveryMutationProperties();
            stubPayloadGenerator();
            String body = """
                    {
                      "header": {"code": "INVALID_ACCESS", "msg": "access token is invalid"},
                      "data": []
                    }
                    """;
            stubMethodChainReturns(successJson(body));

            RegisterFulfillmentDeliveryCommand command = buildValidRegisterCommand();

            // when & then
            assertThatThrownBy(() -> client.registerDelivery(command))
                    .isInstanceOf(FulfillmentInvalidAccessException.class);
        }
    }

    @Nested
    @DisplayName("updateDelivery")
    class UpdateDelivery {

        @Test
        @DisplayName("정상 응답 시 RegisterFulfillmentDeliveryResult로 매핑된다")
        void shouldMapUpdateDeliverySuccessResponse() {
            // given
            stubDeliveryMutationProperties();
            stubPayloadGenerator();
            String body = """
                    {
                      "header": {"code": "200", "msg": "success", "dataCount": 1},
                      "data": [{"code": "200", "msg": "OK", "fmsSlipNo": "FMS001", "orderNo": "ORD001"}]
                    }
                    """;
            stubMethodChainReturns(successJson(body));

            // when
            RegisterFulfillmentDeliveryResult result = client.updateDelivery(buildValidUpdateCommand());

            // then
            assertThat(result).isNotNull();
            assertThat(result.deliveries()).hasSize(1);
            assertThat(result.deliveries().get(0).orderNumber()).isEqualTo("ORD001");
        }

        @Test
        @DisplayName("모든 재시도 실패 시 UpdateFulfillmentDeliveryFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubDeliveryMutationProperties();
            stubPayloadGenerator();
            stubMethodChainThrows(new RuntimeException("Connection refused"));

            UpdateFulfillmentDeliveryCommand command = buildValidUpdateCommand();

            // when & then
            assertThatThrownBy(() -> client.updateDelivery(command))
                    .isInstanceOf(UpdateFulfillmentDeliveryFailedException.class);
        }
    }

    @Nested
    @DisplayName("registerDeliveryCar")
    class RegisterDeliveryCar {

        @Test
        @DisplayName("정상 응답 시 RegisterFulfillmentDeliveryResult로 매핑된다")
        void shouldMapRegisterDeliveryCarSuccessResponse() {
            // given
            stubDeliveryCarMutationProperties();
            stubPayloadGenerator();
            String body = """
                    {
                      "header": {"code": "200", "msg": "success", "dataCount": 1},
                      "data": [{"code": "200", "msg": "OK", "fmsSlipNo": "FMS001", "orderNo": "ORD001"}]
                    }
                    """;
            stubMethodChainReturns(successJson(body));

            // when
            RegisterFulfillmentDeliveryResult result = client.registerDeliveryCar(buildValidRegisterCarCommand());

            // then
            assertThat(result).isNotNull();
            assertThat(result.deliveries()).hasSize(1);
            assertThat(result.deliveries().get(0).fulfillmentSlipNumber()).isEqualTo("FMS001");
        }
    }

    @Nested
    @DisplayName("updateDeliveryCar")
    class UpdateDeliveryCar {

        @Test
        @DisplayName("정상 응답 시 RegisterFulfillmentDeliveryResult로 매핑된다")
        void shouldMapUpdateDeliveryCarSuccessResponse() {
            // given
            stubDeliveryCarMutationProperties();
            stubPayloadGenerator();
            String body = """
                    {
                      "header": {"code": "200", "msg": "success", "dataCount": 1},
                      "data": [{"code": "200", "msg": "OK", "fmsSlipNo": "FMS001", "orderNo": "ORD001"}]
                    }
                    """;
            stubMethodChainReturns(successJson(body));

            // when
            RegisterFulfillmentDeliveryResult result = client.updateDeliveryCar(buildValidUpdateCarCommand());

            // then
            assertThat(result).isNotNull();
            assertThat(result.deliveries()).hasSize(1);
        }

        @Test
        @DisplayName("모든 재시도 실패 시 UpdateFulfillmentDeliveryCarFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubDeliveryCarMutationProperties();
            stubPayloadGenerator();
            stubMethodChainThrows(new RuntimeException("Connection refused"));

            UpdateFulfillmentDeliveryCarCommand command = buildValidUpdateCarCommand();

            // when & then
            assertThatThrownBy(() -> client.updateDeliveryCar(command))
                    .isInstanceOf(UpdateFulfillmentDeliveryCarFailedException.class);
        }
    }

    @Nested
    @DisplayName("registerDeliveryIcs")
    class RegisterDeliveryIcs {

        @Test
        @DisplayName("정상 응답 시 RegisterFulfillmentDeliveryResult로 매핑된다")
        void shouldMapRegisterDeliveryIcsSuccessResponse() {
            // given
            stubDeliveryIcsMutationProperties();
            stubPayloadGenerator();
            String body = """
                    {
                      "header": {"code": "200", "msg": "success", "dataCount": 1},
                      "data": [{"code": "200", "msg": "OK", "fmsSlipNo": "FMS001", "orderNo": "ORD001"}]
                    }
                    """;
            stubMethodChainReturns(successJson(body));

            // when
            RegisterFulfillmentDeliveryResult result = client.registerDeliveryIcs(buildValidRegisterIcsCommand());

            // then
            assertThat(result).isNotNull();
            assertThat(result.deliveries()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("completeDeliveryIcs")
    class CompleteDeliveryIcs {

        @Test
        @DisplayName("정상 응답 시 CompleteFulfillmentDeliveryIcsResult로 매핑된다")
        void shouldMapCompleteDeliveryIcsSuccessResponse() {
            // given
            stubDeliveryIcsCompletedProperties();
            stubPayloadGenerator();
            String body = """
                    {
                      "header": {"code": "200", "msg": "success", "dataCount": 1},
                      "data": [{"code": "200", "msg": "completed", "ordNo": "ORD001"}]
                    }
                    """;
            stubMethodChainReturns(successJson(body));

            // when
            CompleteFulfillmentDeliveryIcsResult result = client.completeDeliveryIcs(buildValidCompleteIcsCommand());

            // then
            assertThat(result).isNotNull();
            assertThat(result.completions()).hasSize(1);
            assertThat(result.completions().get(0).orderNumber()).isEqualTo("ORD001");
        }

        @Test
        @DisplayName("모든 재시도 실패 시 CompleteFulfillmentDeliveryIcsFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubDeliveryIcsCompletedProperties();
            stubPayloadGenerator();
            stubMethodChainThrows(new RuntimeException("Connection refused"));

            CompleteFulfillmentDeliveryIcsCommand command = buildValidCompleteIcsCommand();

            // when & then
            assertThatThrownBy(() -> client.completeDeliveryIcs(command))
                    .isInstanceOf(CompleteFulfillmentDeliveryIcsFailedException.class);
        }
    }

    @Nested
    @DisplayName("cancelDelivery")
    class CancelDelivery {

        @Test
        @DisplayName("정상 응답 시 CancelFulfillmentDeliveryResult로 매핑된다")
        void shouldMapCancelDeliverySuccessResponse() {
            // given
            stubDeliveryCancelProperties();
            stubPayloadGenerator();
            String body = """
                    {
                      "header": {"code": "200", "msg": "success", "dataCount": 1},
                      "data": [{"code": "200", "msg": "cancelled", "fmsSlipNo": "FMS001", "orderNo": "ORD001"}]
                    }
                    """;
            stubMethodChainReturns(successJson(body));

            // when
            CancelFulfillmentDeliveryResult result = client.cancelDelivery(buildValidCancelCommand());

            // then
            assertThat(result).isNotNull();
            assertThat(result.deliveries()).hasSize(1);
            assertThat(result.deliveries().get(0).fulfillmentSlipNumber()).isEqualTo("FMS001");
        }

        @Test
        @DisplayName("모든 재시도 실패 시 CancelFulfillmentDeliveryFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubDeliveryCancelProperties();
            stubPayloadGenerator();
            stubMethodChainThrows(new RuntimeException("Connection refused"));

            CancelFulfillmentDeliveryCommand command = buildValidCancelCommand();

            // when & then
            assertThatThrownBy(() -> client.cancelDelivery(command))
                    .isInstanceOf(CancelFulfillmentDeliveryFailedException.class);
        }
    }

    @Nested
    @DisplayName("getDeliveries")
    class GetDeliveries {

        @Test
        @DisplayName("정상 응답 시 GetFulfillmentDeliveriesResult로 매핑된다")
        void shouldMapGetDeliveriesSuccessResponse() {
            // given
            stubDeliveryListProperties();
            stubPayloadGenerator();
            String body = """
                    {
                      "header": {"code": "200", "msg": "success", "dataCount": 1},
                      "data": [{"ordNo": "ORD001", "slipNo": "SLIP001", "whCd": "WH01"}]
                    }
                    """;
            stubGetChainReturns(successJson(body));

            GetFulfillmentDeliveriesCommand command = GetFulfillmentDeliveriesCommand.of(
                    "CUST001", "valid-token", "20260101", "20260131", "01", "01", "ORD001"
            );

            // when
            GetFulfillmentDeliveriesResult result = client.getDeliveries(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.dataCount()).isEqualTo(1);
            assertThat(result.deliveries()).hasSize(1);
            assertThat(result.deliveries().get(0).orderNumber()).isEqualTo("ORD001");
        }

        @Test
        @DisplayName("모든 재시도 실패 시 GetFulfillmentDeliveriesFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubDeliveryListProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new RuntimeException("Connection refused"));

            GetFulfillmentDeliveriesCommand command = GetFulfillmentDeliveriesCommand.of(
                    "CUST001", "valid-token", "20260101", "20260131", "01", "01", null
            );

            // when & then
            assertThatThrownBy(() -> client.getDeliveries(command))
                    .isInstanceOf(GetFulfillmentDeliveriesFailedException.class);
        }
    }

    @Nested
    @DisplayName("getDeliveryStatuses")
    class GetDeliveryStatuses {

        @Test
        @DisplayName("정상 응답 시 GetFulfillmentDeliveryStatusesResult로 매핑된다")
        void shouldMapGetDeliveryStatusesSuccessResponse() {
            // given
            stubDeliveryStatusProperties();
            stubPayloadGenerator();
            String body = """
                    {
                      "header": {"code": "200", "msg": "success", "dataCount": 1},
                      "data": [{"ordNo": "ORD001", "invoiceNo": "INV001", "godCd": "G001"}]
                    }
                    """;
            stubGetChainReturns(successJson(body));

            GetFulfillmentDeliveryStatusesCommand command = GetFulfillmentDeliveryStatusesCommand.of(
                    "CUST001", "valid-token", "20260101", "20260131", "01"
            );

            // when
            GetFulfillmentDeliveryStatusesResult result = client.getDeliveryStatuses(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.dataCount()).isEqualTo(1);
            assertThat(result.deliveryStatuses()).hasSize(1);
            assertThat(result.deliveryStatuses().get(0).orderNumber()).isEqualTo("ORD001");
        }

        @Test
        @DisplayName("모든 재시도 실패 시 GetFulfillmentDeliveryStatusesFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubDeliveryStatusProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new RuntimeException("Connection refused"));

            GetFulfillmentDeliveryStatusesCommand command = GetFulfillmentDeliveryStatusesCommand.of(
                    "CUST001", "valid-token", "20260101", "20260131", "01"
            );

            // when & then
            assertThatThrownBy(() -> client.getDeliveryStatuses(command))
                    .isInstanceOf(GetFulfillmentDeliveryStatusesFailedException.class);
        }
    }

    @Nested
    @DisplayName("getDeliveryDetail")
    class GetDeliveryDetail {

        @Test
        @DisplayName("정상 응답 시 GetFulfillmentDeliveryDetailResult로 매핑된다")
        void shouldMapGetDeliveryDetailSuccessResponse() {
            // given
            stubDeliveryDetailProperties();
            stubPayloadGenerator();
            String body = """
                    {
                      "header": {"code": "200", "msg": "success", "dataCount": 1},
                      "data": [{"ordNo": "ORD001", "slipNo": "SLIP001", "whCd": "WH01"}]
                    }
                    """;
            stubGetChainReturns(successJson(body));

            GetFulfillmentDeliveryDetailCommand command = GetFulfillmentDeliveryDetailCommand.of(
                    "CUST001", "valid-token", "SLIP001", "ORD001"
            );

            // when
            GetFulfillmentDeliveryDetailResult result = client.getDeliveryDetail(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.deliveries()).hasSize(1);
            assertThat(result.deliveries().get(0).orderNumber()).isEqualTo("ORD001");
        }

        @Test
        @DisplayName("모든 재시도 실패 시 GetFulfillmentDeliveryDetailFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubDeliveryDetailProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new RuntimeException("Connection refused"));

            GetFulfillmentDeliveryDetailCommand command = GetFulfillmentDeliveryDetailCommand.of(
                    "CUST001", "valid-token", "SLIP001"
            );

            // when & then
            assertThatThrownBy(() -> client.getDeliveryDetail(command))
                    .isInstanceOf(GetFulfillmentDeliveryDetailFailedException.class);
        }
    }

    @Nested
    @DisplayName("getOutOrdGoodsDetail")
    class GetOutOrdGoodsDetail {

        @Test
        @DisplayName("정상 응답 시 GetFulfillmentDeliveryOutOrdGoodsDetailResult로 매핑된다")
        void shouldMapGetOutOrdGoodsDetailSuccessResponse() {
            // given
            stubOutOrdGoodsDetailProperties();
            stubPayloadGenerator();
            String body = """
                    {
                      "header": {"code": "200", "msg": "success", "dataCount": 1},
                      "data": [{"invoiceNo": "INV001", "goodsDeliveredList": []}]
                    }
                    """;
            stubGetChainReturns(successJson(body));

            GetFulfillmentDeliveryOutOrdGoodsDetailCommand command = GetFulfillmentDeliveryOutOrdGoodsDetailCommand.of(
                    "CUST001", "valid-token", "SLIP001"
            );

            // when
            GetFulfillmentDeliveryOutOrdGoodsDetailResult result = client.getOutOrdGoodsDetail(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.goodsByInvoice()).hasSize(1);
            assertThat(result.goodsByInvoice().get(0).invoiceNumber()).isEqualTo("INV001");
        }

        @Test
        @DisplayName("header만 있고 data가 비어도 빈 결과로 정상 매핑된다")
        void shouldMapEmptyDataResponseWhenHeaderPresent() {
            // given
            stubOutOrdGoodsDetailProperties();
            stubPayloadGenerator();
            String body = """
                    {
                      "header": {"code": "200", "msg": "success", "dataCount": 0}
                    }
                    """;
            stubGetChainReturns(successJson(body));

            GetFulfillmentDeliveryOutOrdGoodsDetailCommand command = GetFulfillmentDeliveryOutOrdGoodsDetailCommand.of(
                    "CUST001", "valid-token", "SLIP001"
            );

            // when
            GetFulfillmentDeliveryOutOrdGoodsDetailResult result = client.getOutOrdGoodsDetail(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.goodsByInvoice()).isEmpty();
        }

        @Test
        @DisplayName("모든 재시도 실패 시 GetFulfillmentDeliveryOutOrdGoodsDetailFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubOutOrdGoodsDetailProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new RuntimeException("Connection refused"));

            GetFulfillmentDeliveryOutOrdGoodsDetailCommand command = GetFulfillmentDeliveryOutOrdGoodsDetailCommand.of(
                    "CUST001", "valid-token", "SLIP001"
            );

            // when & then
            assertThatThrownBy(() -> client.getOutOrdGoodsDetail(command))
                    .isInstanceOf(GetFulfillmentDeliveryOutOrdGoodsDetailFailedException.class);
        }
    }

    @Nested
    @DisplayName("getOutOrdGoodsByOrdNo")
    class GetOutOrdGoodsByOrdNo {

        @Test
        @DisplayName("정상 응답 시 GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult로 매핑된다")
        void shouldMapGetOutOrdGoodsByOrdNoSuccessResponse() {
            // given
            stubOutOrdGoodsByOrdNoProperties();
            stubPayloadGenerator();
            String body = """
                    {
                      "header": {"code": "200", "msg": "success", "dataCount": 1},
                      "data": [{"ordNo": "ORD001", "invoiceNo": "INV001", "goods": []}]
                    }
                    """;
            stubGetChainReturns(successJson(body));

            GetFulfillmentDeliveryOutOrdGoodsByOrdNoCommand command = GetFulfillmentDeliveryOutOrdGoodsByOrdNoCommand.of(
                    "CUST001", "valid-token", "20260101", "20260131", "ORD001"
            );

            // when
            GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult result = client.getOutOrdGoodsByOrdNo(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.goodsByOrdNo()).hasSize(1);
            assertThat(result.goodsByOrdNo().get(0).orderNumber()).isEqualTo("ORD001");
        }
    }

    @Nested
    @DisplayName("getDeliveryGoodDetail")
    class GetDeliveryGoodDetail {

        @Test
        @DisplayName("정상 응답 시 GetFulfillmentDeliveryGoodDetailResult로 매핑된다")
        void shouldMapGetDeliveryGoodDetailSuccessResponse() {
            // given
            stubDeliveryGoodDetailProperties();
            stubPayloadGenerator();
            String body = """
                    {
                      "header": {"code": "200", "msg": "success", "dataCount": 1},
                      "data": [{"orderNo": "ORD001", "slipNo": "SLIP001", "outDt": "20260101", "outQty": 1}]
                    }
                    """;
            stubGetChainReturns(successJson(body));

            GetFulfillmentDeliveryGoodDetailCommand command = GetFulfillmentDeliveryGoodDetailCommand.of(
                    "CUST001", "valid-token", "20260101", "20260131", "ORD001"
            );

            // when
            GetFulfillmentDeliveryGoodDetailResult result = client.getDeliveryGoodDetail(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.goodDetails()).hasSize(1);
            assertThat(result.goodDetails().get(0).orderNumber()).isEqualTo("ORD001");
        }

        @Test
        @DisplayName("모든 재시도 실패 시 GetFulfillmentDeliveryGoodDetailFailedException이 발생한다")
        void shouldThrowExceptionWhenAllRetriesFail() {
            // given
            stubDeliveryGoodDetailProperties();
            stubPayloadGenerator();
            stubGetChainThrows(new RuntimeException("Connection refused"));

            GetFulfillmentDeliveryGoodDetailCommand command = GetFulfillmentDeliveryGoodDetailCommand.of(
                    "CUST001", "valid-token", "20260101", "20260131", null
            );

            // when & then
            assertThatThrownBy(() -> client.getDeliveryGoodDetail(command))
                    .isInstanceOf(GetFulfillmentDeliveryGoodDetailFailedException.class);
        }
    }

    @Nested
    @DisplayName("비200 응답 재시도 동작")
    class NonSuccessStatusRetry {

        @Test
        @DisplayName("401 응답이 지속되면 GetFulfillmentDeliveriesFailedException이 발생한다")
        void shouldThrowGetFailedOnPersistentUnauthorized() {
            // given
            stubDeliveryListProperties();
            stubPayloadGenerator();
            String body = """
                    {"header": {"code": "401", "msg": "unauthorized"}}
                    """;
            stubGetChainReturns(responseWithStatus(body, HttpStatus.UNAUTHORIZED));

            GetFulfillmentDeliveriesCommand command = GetFulfillmentDeliveriesCommand.of(
                    "CUST001", "valid-token", "20260101", "20260131", "01", "01", null
            );

            // when & then
            assertThatThrownBy(() -> client.getDeliveries(command))
                    .isInstanceOf(GetFulfillmentDeliveriesFailedException.class);
        }

        @Test
        @DisplayName("400 응답이 지속되면 RegisterFulfillmentDeliveryFailedException이 발생한다")
        void shouldThrowRegisterFailedOnPersistentBadRequest() {
            // given
            stubDeliveryMutationProperties();
            stubPayloadGenerator();
            String body = """
                    {"header": {"code": "400", "msg": "bad request"}}
                    """;
            stubMethodChainReturns(responseWithStatus(body, HttpStatus.BAD_REQUEST));

            RegisterFulfillmentDeliveryCommand command = buildValidRegisterCommand();

            // when & then
            assertThatThrownBy(() -> client.registerDelivery(command))
                    .isInstanceOf(RegisterFulfillmentDeliveryFailedException.class);
        }

        @Test
        @DisplayName("500 응답이 지속되면 RegisterFulfillmentDeliveryFailedException이 발생한다")
        void shouldThrowRegisterFailedOnPersistentServerError() {
            // given
            stubDeliveryMutationProperties();
            stubPayloadGenerator();
            String body = """
                    {"header": {"code": "500", "msg": "internal server error"}}
                    """;
            stubMethodChainReturns(responseWithStatus(body, HttpStatus.INTERNAL_SERVER_ERROR));

            RegisterFulfillmentDeliveryCommand command = buildValidRegisterCommand();

            // when & then
            assertThatThrownBy(() -> client.registerDelivery(command))
                    .isInstanceOf(RegisterFulfillmentDeliveryFailedException.class);
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

            GetFulfillmentDeliveriesCommand command = GetFulfillmentDeliveriesCommand.of(
                    "CUST001", "valid-token", "20260101", "20260131", "01", "01", null
            );

            // when & then
            assertThatThrownBy(() -> client.getDeliveries(command))
                    .isInstanceOf(FulfillmentConfigurationException.class);
        }

        @Test
        @DisplayName("deliveryPath가 누락되면 FulfillmentConfigurationException이 발생한다")
        void shouldThrowWhenDeliveryPathMissing() {
            // given
            when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
            when(properties.getDeliveryPath()).thenReturn(null);

            RegisterFulfillmentDeliveryCommand command = buildValidRegisterCommand();

            // when & then
            assertThatThrownBy(() -> client.registerDelivery(command))
                    .isInstanceOf(FulfillmentConfigurationException.class);
        }

        @Test
        @DisplayName("deliveryPath에 {customerCode} 플레이스홀더가 없으면 FulfillmentConfigurationException이 발생한다")
        void shouldThrowWhenDeliveryPathMissingPlaceholder() {
            // given
            when(properties.getBaseUrl()).thenReturn("https://api.fassto.com");
            when(properties.getDeliveryPath()).thenReturn("/delivery");

            RegisterFulfillmentDeliveryCommand command = buildValidRegisterCommand();

            // when & then
            assertThatThrownBy(() -> client.registerDelivery(command))
                    .isInstanceOf(FulfillmentConfigurationException.class);
        }
    }

    @Nested
    @DisplayName("Query 파라미터 검증")
    class QueryValidation {

        @Test
        @DisplayName("FulfillmentDeliveryQuery customerCode 누락 시 예외 발생")
        void deliveryQueryCustomerCodeMissing() {
            assertThatThrownBy(() -> FulfillmentDeliveryQuery.of(
                    null, "token", "20260101", "20260131", "01", "01"
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentDeliveryQuery accessToken 공백 시 예외 발생")
        void deliveryQueryAccessTokenBlank() {
            assertThatThrownBy(() -> FulfillmentDeliveryQuery.of(
                    "CUST001", "  ", "20260101", "20260131", "01", "01"
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentDeliveryQuery startDate 누락 시 예외 발생")
        void deliveryQueryStartDateMissing() {
            assertThatThrownBy(() -> FulfillmentDeliveryQuery.of(
                    "CUST001", "token", null, "20260131", "01", "01"
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentDeliveryQuery endDate 누락 시 예외 발생")
        void deliveryQueryEndDateMissing() {
            assertThatThrownBy(() -> FulfillmentDeliveryQuery.of(
                    "CUST001", "token", "20260101", null, "01", "01"
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentDeliveryQuery status 누락 시 예외 발생")
        void deliveryQueryStatusMissing() {
            assertThatThrownBy(() -> FulfillmentDeliveryQuery.of(
                    "CUST001", "token", "20260101", "20260131", null, "01"
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentDeliveryQuery outDiv 누락 시 예외 발생")
        void deliveryQueryOutDivMissing() {
            assertThatThrownBy(() -> FulfillmentDeliveryQuery.of(
                    "CUST001", "token", "20260101", "20260131", "01", null
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentDeliveryStatusQuery customerCode 누락 시 예외 발생")
        void statusQueryCustomerCodeMissing() {
            assertThatThrownBy(() -> FulfillmentDeliveryStatusQuery.of(
                    null, "token", "20260101", "20260131", "01"
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentDeliveryStatusQuery accessToken 누락 시 예외 발생")
        void statusQueryAccessTokenMissing() {
            assertThatThrownBy(() -> FulfillmentDeliveryStatusQuery.of(
                    "CUST001", null, "20260101", "20260131", "01"
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentDeliveryStatusQuery outDiv 공백 시 예외 발생")
        void statusQueryOutDivBlank() {
            assertThatThrownBy(() -> FulfillmentDeliveryStatusQuery.of(
                    "CUST001", "token", "20260101", "20260131", " "
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentDeliveryDetailQuery customerCode 누락 시 예외 발생")
        void detailQueryCustomerCodeMissing() {
            assertThatThrownBy(() -> FulfillmentDeliveryDetailQuery.of(
                    null, "token", "SLIP001"
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentDeliveryDetailQuery slipNo 누락 시 예외 발생")
        void detailQuerySlipNoMissing() {
            assertThatThrownBy(() -> FulfillmentDeliveryDetailQuery.of(
                    "CUST001", "token", null
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentDeliveryDetailQuery accessToken 공백 시 예외 발생")
        void detailQueryAccessTokenBlank() {
            assertThatThrownBy(() -> FulfillmentDeliveryDetailQuery.of(
                    "CUST001", "", "SLIP001"
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentDeliveryOutOrdGoodsDetailQuery customerCode 누락 시 예외 발생")
        void outOrdGoodsDetailCustomerCodeMissing() {
            assertThatThrownBy(() -> FulfillmentDeliveryOutOrdGoodsDetailQuery.of(
                    null, "token", "SLIP001"
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentDeliveryOutOrdGoodsDetailQuery outOrdSlipNo 누락 시 예외 발생")
        void outOrdGoodsDetailSlipNoMissing() {
            assertThatThrownBy(() -> FulfillmentDeliveryOutOrdGoodsDetailQuery.of(
                    "CUST001", "token", null
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentDeliveryOutOrdGoodsDetailQuery accessToken 공백 시 예외 발생")
        void outOrdGoodsDetailAccessTokenBlank() {
            assertThatThrownBy(() -> FulfillmentDeliveryOutOrdGoodsDetailQuery.of(
                    "CUST001", " ", "SLIP001"
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentDeliveryOutOrdGoodsByOrdNoQuery customerCode 누락 시 예외 발생")
        void outOrdGoodsByOrdNoCustomerCodeMissing() {
            assertThatThrownBy(() -> FulfillmentDeliveryOutOrdGoodsByOrdNoQuery.of(
                    null, "token", "20260101", "20260131", null
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentDeliveryOutOrdGoodsByOrdNoQuery accessToken 누락 시 예외 발생")
        void outOrdGoodsByOrdNoAccessTokenMissing() {
            assertThatThrownBy(() -> FulfillmentDeliveryOutOrdGoodsByOrdNoQuery.of(
                    "CUST001", null, "20260101", "20260131", null
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentDeliveryOutOrdGoodsByOrdNoQuery startDate 공백 시 예외 발생")
        void outOrdGoodsByOrdNoStartDateBlank() {
            assertThatThrownBy(() -> FulfillmentDeliveryOutOrdGoodsByOrdNoQuery.of(
                    "CUST001", "token", " ", "20260131", null
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentDeliveryOutOrdGoodsByOrdNoQuery endDate 누락 시 예외 발생")
        void outOrdGoodsByOrdNoEndDateMissing() {
            assertThatThrownBy(() -> FulfillmentDeliveryOutOrdGoodsByOrdNoQuery.of(
                    "CUST001", "token", "20260101", null, null
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentDeliveryGoodDetailQuery customerCode 누락 시 예외 발생")
        void goodDetailCustomerCodeMissing() {
            assertThatThrownBy(() -> FulfillmentDeliveryGoodDetailQuery.of(
                    null, "token", "20260101", "20260131", null
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentDeliveryGoodDetailQuery accessToken 공백 시 예외 발생")
        void goodDetailAccessTokenBlank() {
            assertThatThrownBy(() -> FulfillmentDeliveryGoodDetailQuery.of(
                    "CUST001", "  ", "20260101", "20260131", null
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentDeliveryGoodDetailQuery startDate 누락 시 예외 발생")
        void goodDetailStartDateMissing() {
            assertThatThrownBy(() -> FulfillmentDeliveryGoodDetailQuery.of(
                    "CUST001", "token", null, "20260131", null
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("FulfillmentDeliveryGoodDetailQuery endDate 누락 시 예외 발생")
        void goodDetailEndDateMissing() {
            assertThatThrownBy(() -> FulfillmentDeliveryGoodDetailQuery.of(
                    "CUST001", "token", "20260101", null, null
            )).isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }
    }
}
