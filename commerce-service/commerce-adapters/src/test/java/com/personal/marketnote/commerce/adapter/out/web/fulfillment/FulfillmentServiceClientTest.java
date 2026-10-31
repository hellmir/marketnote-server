package com.personal.marketnote.commerce.adapter.out.web.fulfillment;

import com.personal.marketnote.commerce.port.out.fulfillment.RegisterFulfillmentReturnDeliveryCommand;
import com.personal.marketnote.commerce.port.out.fulfillment.RegisterFulfillmentReturnDeliveryResult;
import com.personal.marketnote.commerce.port.out.fulfillment.ReturnInspectionResult;
import com.personal.marketnote.common.exception.FulfillmentServiceRequestFailedException;
import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentServiceClient 테스트")
class FulfillmentServiceClientTest {

    private MockWebServer mockWebServer;
    private FulfillmentServiceClient fulfillmentServiceClient;

    @Mock
    private HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("").toString();
        String trimmedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(2));

        RestClient.Builder builder = RestClient.builder().requestFactory(requestFactory);
        fulfillmentServiceClient = new FulfillmentServiceClient(
                builder,
                trimmedBaseUrl,
                hmacServiceAuthHeaderBuilder
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    private RegisterFulfillmentReturnDeliveryCommand buildCommand() {
        return RegisterFulfillmentReturnDeliveryCommand.builder()
                .orderId(100L)
                .orderDate("2026-10-31")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1111-2222")
                .recipientAddress("서울시 강남구")
                .pickupRecipientName("홍길동")
                .pickupRecipientPhoneNumber("010-1111-2222")
                .pickupZipCode("06000")
                .pickupAddress("서울시 강남구")
                .pickupAddressDetail("101동 101호")
                .returnReason("UNSATISFIED")
                .returnDetailReason("사이즈 불일치")
                .returnShippingRequest("경비실 맡겨주세요")
                .products(List.of(
                        RegisterFulfillmentReturnDeliveryCommand.ProductItem.of("P-001", 2)
                ))
                .build();
    }

    @Nested
    @DisplayName("registerReturnDelivery")
    class RegisterReturnDelivery {

        @Test
        @DisplayName("201 정상 응답 시 RegisterFulfillmentReturnDeliveryResult로 매핑한다")
        void shouldMapResultWhen2xxResponse() throws Exception {
            // given
            doNothing().when(hmacServiceAuthHeaderBuilder)
                    .applyHeaders(org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("POST"),
                            org.mockito.ArgumentMatchers.eq("/api/v1/internal/fulfillment/deliveries/return"));

            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(201)
                    .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .setBody("""
                            {
                                "content": {
                                    "orderId": 100,
                                    "returnSlipNumber": "RS-20260420-0001",
                                    "registered": true,
                                    "message": "반품 접수 완료"
                                }
                            }
                            """));

            // when
            RegisterFulfillmentReturnDeliveryResult result =
                    fulfillmentServiceClient.registerReturnDelivery(buildCommand());

            // then
            assertThat(result.orderId()).isEqualTo(100L);
            assertThat(result.returnSlipNumber()).isEqualTo("RS-20260420-0001");
            assertThat(result.registered()).isTrue();
            assertThat(result.message()).isEqualTo("반품 접수 완료");

            RecordedRequest recorded = mockWebServer.takeRequest();
            assertThat(recorded.getMethod()).isEqualTo("POST");
            assertThat(recorded.getPath()).isEqualTo("/api/v1/internal/fulfillment/deliveries/return");
            assertThat(recorded.getHeader("Content-Type")).contains(MediaType.APPLICATION_JSON_VALUE);

            verify(hmacServiceAuthHeaderBuilder)
                    .applyHeaders(org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("POST"),
                            org.mockito.ArgumentMatchers.eq("/api/v1/internal/fulfillment/deliveries/return"));
        }

        @Test
        @DisplayName("4xx 응답 시 FulfillmentServiceRequestFailedException을 발생시킨다")
        void shouldThrowExceptionWhen4xxResponse() {
            // given
            doNothing().when(hmacServiceAuthHeaderBuilder)
                    .applyHeaders(org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("POST"),
                            org.mockito.ArgumentMatchers.eq("/api/v1/internal/fulfillment/deliveries/return"));

            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(400)
                    .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .setBody("""
                            {
                                "message": "잘못된 요청"
                            }
                            """));

            // when & then
            assertThatThrownBy(() -> fulfillmentServiceClient.registerReturnDelivery(buildCommand()))
                    .isInstanceOf(FulfillmentServiceRequestFailedException.class);
        }

        @Test
        @DisplayName("5xx 응답 시 FulfillmentServiceRequestFailedException을 발생시킨다")
        void shouldThrowExceptionWhen5xxResponse() {
            // given
            doNothing().when(hmacServiceAuthHeaderBuilder)
                    .applyHeaders(org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("POST"),
                            org.mockito.ArgumentMatchers.eq("/api/v1/internal/fulfillment/deliveries/return"));

            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(500)
                    .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .setBody("""
                            {
                                "message": "내부 서버 오류"
                            }
                            """));

            // when & then
            assertThatThrownBy(() -> fulfillmentServiceClient.registerReturnDelivery(buildCommand()))
                    .isInstanceOf(FulfillmentServiceRequestFailedException.class);
        }

        @Test
        @DisplayName("네트워크 예외 발생 시 FulfillmentServiceRequestFailedException으로 IOException 래핑한다")
        void shouldWrapIoExceptionWhenNetworkFails() {
            // given
            doNothing().when(hmacServiceAuthHeaderBuilder)
                    .applyHeaders(org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("POST"),
                            org.mockito.ArgumentMatchers.eq("/api/v1/internal/fulfillment/deliveries/return"));

            mockWebServer.enqueue(new MockResponse()
                    .setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

            // when & then
            assertThatThrownBy(() -> fulfillmentServiceClient.registerReturnDelivery(buildCommand()))
                    .isInstanceOf(FulfillmentServiceRequestFailedException.class);
        }
    }

    @Nested
    @DisplayName("getReturnGodDetail")
    class GetReturnGodDetail {

        @Test
        @DisplayName("200 정상 응답 JSON 파싱 후 ReturnInspectionResult로 매핑한다")
        void shouldMapResultWhen2xxResponse() throws Exception {
            // given
            doNothing().when(hmacServiceAuthHeaderBuilder)
                    .applyHeaders(org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("GET"),
                            org.mockito.ArgumentMatchers.eq("/api/v1/internal/fulfillment/deliveries/return-god-detail"));

            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .setBody("""
                            {
                                "content": {
                                    "dataCount": 1,
                                    "returnGodInfos": [
                                        {
                                            "orderNumber": "O-0001",
                                            "inboundOrderSlipNumber": "IB-0001",
                                            "products": [
                                                {
                                                    "customerProductCode": "CP-001",
                                                    "productName": "상품A",
                                                    "returnProductCheckStatus": "COMPLETED",
                                                    "returnProductCheckStatusName": "검수완료"
                                                }
                                            ]
                                        }
                                    ]
                                }
                            }
                            """));

            // when
            ReturnInspectionResult result = fulfillmentServiceClient.getReturnGodDetail("RS-20260420-0001");

            // then
            assertThat(result.dataCount()).isEqualTo(1);
            assertThat(result.returnGodInfos()).hasSize(1);
            ReturnInspectionResult.ReturnInspectionResultItem item = result.returnGodInfos().get(0);
            assertThat(item.orderNumber()).isEqualTo("O-0001");
            assertThat(item.inboundOrderSlipNumber()).isEqualTo("IB-0001");
            assertThat(item.products()).hasSize(1);
            ReturnInspectionResult.ReturnInspectionGoodsItem goods = item.products().get(0);
            assertThat(goods.customerProductCode()).isEqualTo("CP-001");
            assertThat(goods.productName()).isEqualTo("상품A");
            assertThat(goods.returnProductCheckStatus()).isEqualTo("COMPLETED");
            assertThat(goods.returnProductCheckStatusName()).isEqualTo("검수완료");

            RecordedRequest recorded = mockWebServer.takeRequest();
            assertThat(recorded.getMethod()).isEqualTo("GET");
            assertThat(recorded.getPath()).startsWith("/api/v1/internal/fulfillment/deliveries/return-god-detail");
            assertThat(recorded.getPath()).contains("return-slip-numbers=RS-20260420-0001");
        }

        @Test
        @DisplayName("응답 바디가 null(204)이면 FulfillmentServiceRequestFailedException을 발생시킨다")
        void shouldThrowWhenResponseBodyIsNull() {
            // given
            doNothing().when(hmacServiceAuthHeaderBuilder)
                    .applyHeaders(org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("GET"),
                            org.mockito.ArgumentMatchers.eq("/api/v1/internal/fulfillment/deliveries/return-god-detail"));

            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(204));

            // when & then
            assertThatThrownBy(() -> fulfillmentServiceClient.getReturnGodDetail("RS-20260420-0001"))
                    .isInstanceOf(FulfillmentServiceRequestFailedException.class);
        }

        @Test
        @DisplayName("네트워크 예외 발생 시 FulfillmentServiceRequestFailedException으로 IOException 래핑한다")
        void shouldWrapIoExceptionWhenNetworkFails() {
            // given
            doNothing().when(hmacServiceAuthHeaderBuilder)
                    .applyHeaders(org.mockito.ArgumentMatchers.any(),
                            org.mockito.ArgumentMatchers.eq("GET"),
                            org.mockito.ArgumentMatchers.eq("/api/v1/internal/fulfillment/deliveries/return-god-detail"));

            mockWebServer.enqueue(new MockResponse()
                    .setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

            // when & then
            assertThatThrownBy(() -> fulfillmentServiceClient.getReturnGodDetail("RS-20260420-0001"))
                    .isInstanceOf(FulfillmentServiceRequestFailedException.class);
        }
    }
}
