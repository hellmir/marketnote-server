package com.personal.marketnote.commerce.adapter.out.vendor.kcp;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.dto.*;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception.KcpCommunicationException;
import com.personal.marketnote.commerce.configuration.KcpProperties;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.external.log.ExternalSystem;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("KcpApiClient ExternalCallLogger 통합 단위 테스트")
class KcpApiClientExternalCallLoggerTest {

    private static final String TRADE_REGISTER_URL = "http://kcp.test/trade/register";
    private static final String PAYMENT_APPROVAL_URL = "http://kcp.test/payment/approval";
    private static final String PAYMENT_CANCEL_URL = "http://kcp.test/payment/cancel";
    private static final String BATCH_KEY_ISSUANCE_URL = "http://kcp.test/batch/key";
    private static final String BATCH_PAYMENT_APPROVAL_URL = "http://kcp.test/batch/approval";

    private static final String SITE_CD = "T0000";
    private static final String ORDER_KEY = "ORD-1";
    private static final String TNO = "TNO-1";

    private KcpApiClient client;
    private MockRestServiceServer mockServer;
    private MeterRegistry meterRegistry;
    private Logger kcpLogger;
    private ListAppender<ILoggingEvent> appender;
    private boolean previousAdditive;
    private Level previousLevel;

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

        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        RestClient.Builder builder = RestClient.builder()
            .requestFactory(new org.springframework.http.client.ClientHttpRequestFactory() {
                @Override
                public org.springframework.http.client.ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
                    return restTemplate.getRequestFactory().createRequest(uri, httpMethod);
                }
            });

        ObjectMapper objectMapper = new ObjectMapper();
        meterRegistry = new SimpleMeterRegistry();
        ExternalCallLogger externalCallLogger = new ExternalCallLogger(meterRegistry);

        client = new KcpApiClient(kcpProperties, builder, objectMapper, externalCallLogger);

        kcpLogger = (Logger) LoggerFactory.getLogger(ExternalSystem.KCP.loggerName());
        previousAdditive = kcpLogger.isAdditive();
        previousLevel = kcpLogger.getLevel();
        kcpLogger.setAdditive(false);
        kcpLogger.setLevel(Level.DEBUG);
        appender = new ListAppender<>();
        appender.start();
        kcpLogger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        kcpLogger.detachAppender(appender);
        appender.stop();
        kcpLogger.setAdditive(previousAdditive);
        kcpLogger.setLevel(previousLevel);
    }

    @Nested
    @DisplayName("operation별 success Timer/DEBUG 로그")
    class SuccessOperations {

        @Test
        @DisplayName("거래등록 성공 시 success Timer와 ordrIdxx 태그가 로그에 포함된다")
        void shouldRecordSuccessForTradeRegister() {
            String responseJson = """
                {"Code":"0000","Message":"OK","approvalKey":"K","PayUrl":"u","traceNo":"T","paymentMethod":"CARD"}
                """;
            mockServer.expect(requestTo(TRADE_REGISTER_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

            client.registerTrade(KcpTradeRegisterRequest.builder()
                .siteCd(SITE_CD)
                .ordrIdxx(ORDER_KEY)
                .goodMny("10000")
                .payMethod("CARD")
                .goodName("상품")
                .retUrl("http://r")
                .build()
            );

            assertSuccessTimerExists("trade.register");
            assertTagInDebugLog("ordrIdxx", ORDER_KEY);
            assertOperationInDebugLog("trade.register");
        }

        @Test
        @DisplayName("결제승인 성공 시 success Timer와 ordrNo 태그가 로그에 포함된다")
        void shouldRecordSuccessForPaymentApproval() {
            String responseJson = "{\"res_cd\":\"0000\",\"res_msg\":\"OK\"}";
            mockServer.expect(requestTo(PAYMENT_APPROVAL_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

            client.approvePayment(KcpPaymentApprovalRequest.builder()
                .siteCd(SITE_CD)
                .ordrNo(ORDER_KEY)
                .build()
            );

            assertSuccessTimerExists("payment.approve");
            assertTagInDebugLog("ordrNo", ORDER_KEY);
        }

        @Test
        @DisplayName("결제취소 성공 시 success Timer와 tno 태그가 로그에 포함된다")
        void shouldRecordSuccessForPaymentCancel() {
            String responseJson = "{\"res_cd\":\"0000\",\"res_msg\":\"OK\"}";
            mockServer.expect(requestTo(PAYMENT_CANCEL_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

            client.cancelPayment(KcpPaymentCancelRequest.builder()
                .siteCd(SITE_CD)
                .tno(TNO)
                .build()
            );

            assertSuccessTimerExists("payment.cancel");
            assertTagInDebugLog("tno", TNO);
        }

        @Test
        @DisplayName("배치키 발급 성공 시 success Timer가 기록된다")
        void shouldRecordSuccessForBatchKeyIssue() {
            String responseJson = "{\"res_cd\":\"0000\",\"res_msg\":\"OK\"}";
            mockServer.expect(requestTo(BATCH_KEY_ISSUANCE_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

            client.issueBatchKey(KcpBatchKeyIssuanceRequest.builder()
                .siteCd(SITE_CD)
                .build()
            );

            assertSuccessTimerExists("batch.key.issue");
        }

        @Test
        @DisplayName("배치 결제승인 성공 시 success Timer와 ordrIdxx 태그가 로그에 포함된다")
        void shouldRecordSuccessForBatchPaymentApprove() {
            String responseJson = "{\"res_cd\":\"0000\",\"res_msg\":\"OK\"}";
            mockServer.expect(requestTo(BATCH_PAYMENT_APPROVAL_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

            client.approveBatchPayment(KcpBatchPaymentApprovalRequest.builder()
                .siteCd(SITE_CD)
                .ordrIdxx(ORDER_KEY)
                .build()
            );

            assertSuccessTimerExists("batch.payment.approve");
            assertTagInDebugLog("ordrIdxx", ORDER_KEY);
        }

        @Test
        @DisplayName("배치키 삭제 성공 시 success Timer가 기록된다")
        void shouldRecordSuccessForBatchKeyDelete() {
            String responseJson = "{\"res_cd\":\"0000\",\"res_msg\":\"OK\"}";
            mockServer.expect(requestTo(BATCH_PAYMENT_APPROVAL_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

            client.deleteBatchKey(KcpBatchKeyDeletionRequest.builder()
                .siteCd(SITE_CD)
                .build()
            );

            assertSuccessTimerExists("batch.key.delete");
        }
    }

    @Nested
    @DisplayName("예외 발생 시 failure Timer/ERROR 로그")
    class FailureOperations {

        @Test
        @DisplayName("결제승인 5xx 응답 시 failure Timer가 기록되고 동일 예외를 rethrow한다")
        void shouldRecordFailureWhenPaymentApprovalFails() {
            mockServer.expect(requestTo(PAYMENT_APPROVAL_URL))
                .andRespond(withServerError());

            assertThatThrownBy(() -> client.approvePayment(
                KcpPaymentApprovalRequest.builder().ordrNo(ORDER_KEY).build()
            )).isInstanceOf(RuntimeException.class);

            Timer timer = meterRegistry.find("external.call")
                .tags("system", ExternalSystem.KCP.metricTag(),
                    "operation", "payment.approve",
                    "outcome", "failure"
                )
                .timer();
            assertThat(timer).isNotNull();
            assertThat(timer.count()).isEqualTo(1L);

            boolean hasError = appender.list.stream()
                .anyMatch(event -> event.getLevel() == Level.ERROR
                    && event.getFormattedMessage().contains("payment.approve"));
            assertThat(hasError).isTrue();
        }

        @Test
        @DisplayName("응답 본문이 비어 KcpCommunicationException 발생 시 failure Timer가 기록된다")
        void shouldRecordFailureOnEmptyResponse() {
            mockServer.expect(requestTo(TRADE_REGISTER_URL))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

            assertThatThrownBy(() -> client.registerTrade(
                KcpTradeRegisterRequest.builder().siteCd(SITE_CD).ordrIdxx(ORDER_KEY).build()
            )).isInstanceOf(KcpCommunicationException.class);

            Timer timer = meterRegistry.find("external.call")
                .tags("system", ExternalSystem.KCP.metricTag(),
                    "operation", "trade.register",
                    "outcome", "failure"
                )
                .timer();
            assertThat(timer).isNotNull();
            assertThat(timer.count()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("PCI-DSS 회귀 검증 (민감 정보 미포함)")
    class SensitiveDataAbsenceTests {

        @Test
        @DisplayName("결제승인 태그/로그에 enc_data/cert_info 등 민감 페이로드가 포함되지 않는다")
        void shouldNotIncludeSensitivePayloadInPaymentApprovalLog() {
            String responseJson = "{\"res_cd\":\"0000\",\"res_msg\":\"OK\"}";
            mockServer.expect(requestTo(PAYMENT_APPROVAL_URL))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

            client.approvePayment(KcpPaymentApprovalRequest.builder()
                .siteCd(SITE_CD)
                .ordrNo(ORDER_KEY)
                .encData("VERY-SECRET-ENC-DATA")
                .encInfo("VERY-SECRET-ENC-INFO")
                .kcpCertInfo("VERY-SECRET-CERT")
                .build()
            );

            assertNoLogContains("VERY-SECRET-ENC-DATA");
            assertNoLogContains("VERY-SECRET-ENC-INFO");
            assertNoLogContains("VERY-SECRET-CERT");
        }

        @Test
        @DisplayName("결제취소 태그/로그에 cert_info/sign_data 등 민감 페이로드가 포함되지 않는다")
        void shouldNotIncludeSensitivePayloadInPaymentCancelLog() {
            String responseJson = "{\"res_cd\":\"0000\",\"res_msg\":\"OK\"}";
            mockServer.expect(requestTo(PAYMENT_CANCEL_URL))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

            client.cancelPayment(KcpPaymentCancelRequest.builder()
                .siteCd(SITE_CD)
                .tno(TNO)
                .kcpCertInfo("CERT-CONFIDENTIAL")
                .kcpSignData("SIGN-CONFIDENTIAL")
                .build()
            );

            assertNoLogContains("CERT-CONFIDENTIAL");
            assertNoLogContains("SIGN-CONFIDENTIAL");
        }
    }

    private void assertSuccessTimerExists(String operation) {
        Timer timer = meterRegistry.find("external.call")
            .tags("system", ExternalSystem.KCP.metricTag(),
                "operation", operation,
                "outcome", "success"
            )
            .timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
    }

    private void assertOperationInDebugLog(String operation) {
        boolean found = appender.list.stream()
            .anyMatch(event -> event.getFormattedMessage().contains(operation));
        assertThat(found).as("expected operation '%s' in debug log", operation).isTrue();
    }

    private void assertTagInDebugLog(String tagKey, String tagValue) {
        boolean foundKey = appender.list.stream()
            .anyMatch(event -> event.getFormattedMessage().contains(tagKey));
        boolean foundValue = appender.list.stream()
            .anyMatch(event -> event.getFormattedMessage().contains(tagValue));
        assertThat(foundKey).as("expected tag key '%s' in debug log", tagKey).isTrue();
        assertThat(foundValue).as("expected tag value '%s' in debug log", tagValue).isTrue();
    }

    private void assertNoLogContains(String forbidden) {
        boolean leaked = appender.list.stream()
            .anyMatch(event -> event.getFormattedMessage().contains(forbidden));
        assertThat(leaked).as("sensitive value '%s' must not appear in log", forbidden).isFalse();
    }
}
