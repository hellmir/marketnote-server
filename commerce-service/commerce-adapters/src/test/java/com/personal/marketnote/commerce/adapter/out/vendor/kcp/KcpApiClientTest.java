package com.personal.marketnote.commerce.adapter.out.vendor.kcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpBatchKeyDeletionRequest;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpBatchKeyDeletionResponse;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpBatchKeyIssuanceRequest;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpBatchKeyIssuanceResponse;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpBatchPaymentApprovalRequest;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpBatchPaymentApprovalResponse;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpPaymentApprovalRequest;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpPaymentApprovalResponse;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpPaymentCancelRequest;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpPaymentCancelResponse;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpTradeRegisterRequest;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.KcpTradeRegisterResponse;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception.KcpCommunicationException;
import com.personal.marketnote.commerce.configuration.KcpProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("KcpApiClient 단위 테스트")
class KcpApiClientTest {

    private static final String TRADE_REGISTER_URL = "http://kcp.test/trade/register";
    private static final String PAYMENT_APPROVAL_URL = "http://kcp.test/payment/approval";
    private static final String PAYMENT_CANCEL_URL = "http://kcp.test/payment/cancel";
    private static final String BATCH_KEY_ISSUANCE_URL = "http://kcp.test/batch/key";
    private static final String BATCH_PAYMENT_APPROVAL_URL = "http://kcp.test/batch/approval";

    private KcpApiClient client;
    private MockRestServiceServer mockServer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        KcpProperties kcpProperties = new KcpProperties();
        KcpProperties.Api api = new KcpProperties.Api();
        api.setTradeRegisterUrl(TRADE_REGISTER_URL);
        api.setPaymentApprovalUrl(PAYMENT_APPROVAL_URL);
        api.setPaymentCancelUrl(PAYMENT_CANCEL_URL);
        api.setBatchKeyIssuanceUrl(BATCH_KEY_ISSUANCE_URL);
        api.setBatchPaymentApprovalUrl(BATCH_PAYMENT_APPROVAL_URL);
        kcpProperties.setApi(api);

        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();

        RestClient.Builder builder = RestClient.builder().requestFactory(new org.springframework.http.client.ClientHttpRequestFactory() {
            @Override
            public org.springframework.http.client.ClientHttpRequest createRequest(java.net.URI uri, HttpMethod httpMethod) throws java.io.IOException {
                return restTemplate.getRequestFactory().createRequest(uri, httpMethod);
            }
        });

        objectMapper = new ObjectMapper();
        client = new KcpApiClient(kcpProperties, builder, objectMapper);
    }

    @Nested
    @DisplayName("registerTrade")
    class RegisterTrade {

        @Test
        @DisplayName("거래 등록 성공 응답을 파싱한다")
        void shouldParseSuccessResponse() {
            // given
            String responseJson = """
                    {"Code":"0000","Message":"성공","approvalKey":"APR_KEY","PayUrl":"http://pay","traceNo":"TR_001","paymentMethod":"CARD"}
                    """;
            mockServer.expect(requestTo(TRADE_REGISTER_URL))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

            KcpTradeRegisterRequest request = KcpTradeRegisterRequest.builder()
                    .siteCd("T0000")
                    .ordrIdxx("ORDER_001")
                    .goodMny("10000")
                    .payMethod("CARD")
                    .goodName("상품")
                    .retUrl("http://return")
                    .batchCardnoReturnYn("Y")
                    .build();

            // when
            KcpTradeRegisterResponse response = client.registerTrade(request);

            // then
            assertThat(response.resCd()).isEqualTo("0000");
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.approvalKey()).isEqualTo("APR_KEY");
        }

        @Test
        @DisplayName("응답 본문이 비어 있으면 KcpCommunicationException을 던진다")
        void shouldThrowOnEmptyBody() {
            // given
            mockServer.expect(requestTo(TRADE_REGISTER_URL))
                    .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

            KcpTradeRegisterRequest request = KcpTradeRegisterRequest.builder().siteCd("T0000").build();

            // when & then
            assertThatThrownBy(() -> client.registerTrade(request))
                    .isInstanceOf(KcpCommunicationException.class);
        }

        @Test
        @DisplayName("파싱 실패 시 KcpCommunicationException을 던진다")
        void shouldThrowOnParseFailure() {
            // given
            mockServer.expect(requestTo(TRADE_REGISTER_URL))
                    .andRespond(withSuccess("not-a-json", MediaType.APPLICATION_JSON));

            KcpTradeRegisterRequest request = KcpTradeRegisterRequest.builder().siteCd("T0000").build();

            // when & then
            assertThatThrownBy(() -> client.registerTrade(request))
                    .isInstanceOf(KcpCommunicationException.class);
        }
    }

    @Nested
    @DisplayName("approvePayment")
    class ApprovePayment {

        @Test
        @DisplayName("결제 승인 성공 응답을 파싱한다")
        void shouldParseSuccessResponse() {
            // given
            String responseJson = "{\"res_cd\":\"0000\",\"res_msg\":\"OK\"}";
            mockServer.expect(requestTo(PAYMENT_APPROVAL_URL))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

            KcpPaymentApprovalRequest request = KcpPaymentApprovalRequest.builder().ordrNo("ORDER_001").build();

            // when
            KcpPaymentApprovalResponse response = client.approvePayment(request);

            // then
            assertThat(response).isNotNull();
        }
    }

    @Nested
    @DisplayName("cancelPayment")
    class CancelPayment {

        @Test
        @DisplayName("결제 취소 성공 응답을 파싱한다")
        void shouldParseCancelSuccessResponse() {
            // given
            String responseJson = "{\"res_cd\":\"0000\",\"res_msg\":\"OK\"}";
            mockServer.expect(requestTo(PAYMENT_CANCEL_URL))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

            KcpPaymentCancelRequest request = KcpPaymentCancelRequest.builder().tno("TNO_001").build();

            // when
            KcpPaymentCancelResponse response = client.cancelPayment(request);

            // then
            assertThat(response).isNotNull();
        }
    }

    @Nested
    @DisplayName("issueBatchKey")
    class IssueBatchKey {

        @Test
        @DisplayName("배치키 발급 성공 응답을 파싱한다")
        void shouldParseBatchKeySuccess() {
            // given
            String responseJson = "{\"res_cd\":\"0000\",\"res_msg\":\"OK\"}";
            mockServer.expect(requestTo(BATCH_KEY_ISSUANCE_URL))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

            KcpBatchKeyIssuanceRequest request = KcpBatchKeyIssuanceRequest.builder().siteCd("T0000").build();

            // when
            KcpBatchKeyIssuanceResponse response = client.issueBatchKey(request);

            // then
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("응답 본문이 비어 있으면 KcpCommunicationException을 던진다")
        void shouldThrowOnEmptyBatchKeyBody() {
            // given
            mockServer.expect(requestTo(BATCH_KEY_ISSUANCE_URL))
                    .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

            KcpBatchKeyIssuanceRequest request = KcpBatchKeyIssuanceRequest.builder().siteCd("T0000").build();

            // when & then
            assertThatThrownBy(() -> client.issueBatchKey(request))
                    .isInstanceOf(KcpCommunicationException.class);
        }
    }

    @Nested
    @DisplayName("approveBatchPayment")
    class ApproveBatchPayment {

        @Test
        @DisplayName("배치 결제승인 성공 응답을 파싱한다")
        void shouldParseBatchApprovalSuccess() {
            // given
            String responseJson = "{\"res_cd\":\"0000\",\"res_msg\":\"OK\"}";
            mockServer.expect(requestTo(BATCH_PAYMENT_APPROVAL_URL))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

            KcpBatchPaymentApprovalRequest request = KcpBatchPaymentApprovalRequest.builder()
                    .siteCd("T0000")
                    .ordrIdxx("ORDER_001")
                    .build();

            // when
            KcpBatchPaymentApprovalResponse response = client.approveBatchPayment(request);

            // then
            assertThat(response).isNotNull();
        }
    }

    @Nested
    @DisplayName("deleteBatchKey")
    class DeleteBatchKey {

        @Test
        @DisplayName("배치키 삭제 성공 응답을 파싱한다")
        void shouldParseBatchKeyDeletionSuccess() {
            // given
            String responseJson = "{\"res_cd\":\"0000\",\"res_msg\":\"OK\"}";
            mockServer.expect(requestTo(BATCH_PAYMENT_APPROVAL_URL))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

            KcpBatchKeyDeletionRequest request = KcpBatchKeyDeletionRequest.builder().siteCd("T0000").build();

            // when
            KcpBatchKeyDeletionResponse response = client.deleteBatchKey(request);

            // then
            assertThat(response).isNotNull();
        }
    }

    @Nested
    @DisplayName("toJsonNode")
    class ToJsonNode {

        @Test
        @DisplayName("객체를 JsonNode로 변환한다")
        void shouldConvertToJsonNode() {
            // given
            java.util.Map<String, String> data = java.util.Map.of("key", "value");

            // when
            var node = client.toJsonNode(data);

            // then
            assertThat(node.get("key").asText()).isEqualTo("value");
        }
    }
}
