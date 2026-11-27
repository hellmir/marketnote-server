package com.personal.marketnote.reward.adapter.out.vendor.giftishow;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.external.log.ExternalSystem;
import com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.GiftishowCommunicationException;
import com.personal.marketnote.reward.configuration.GiftishowApiProperties;
import com.personal.marketnote.reward.utility.VendorCommunicationRecorder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("GiftishowApiClient ExternalCallLogger 통합 단위 테스트")
class GiftishowApiClientExternalCallLoggerTest {

    private static final String BASE_URL = "http://localhost:9999";
    private static final String PRODUCT_LIST_PATH = "/bizApi/goods";
    private static final String PRODUCT_DETAIL_PATH = "/bizApi/goods";
    private static final String BRAND_LIST_PATH = "/bizApi/brands";
    private static final String BRAND_DETAIL_PATH = "/bizApi/brands";
    private static final String COUPON_SEND_PATH = "/bizApi/send";
    private static final String COUPON_DETAIL_PATH = "/bizApi/coupons";
    private static final String COUPON_CANCEL_PATH = "/bizApi/cancel";
    private static final String COUPON_RESEND_PATH = "/bizApi/resend";
    private static final String BIZMONEY_BALANCE_PATH = "/bizApi/bizmoney";
    private static final String COUPON_SEND_FAIL_CANCEL_PATH = "/bizApi/sendFail/cancel";

    private static final String TR_ID = "TR_001";
    private static final String GOODS_CODE = "G001";
    private static final String BRAND_CODE = "B001";
    private static final String PHONE_NO = "01012345678";
    private static final String USER_ID = "user-1";

    private static final String SUCCESS_BODY = "{\"resultCode\":\"0000\",\"resultMessage\":\"OK\"}";

    private GiftishowApiClient client;
    private MockRestServiceServer mockServer;
    private MeterRegistry meterRegistry;
    private Logger giftishowLogger;
    private ListAppender<ILoggingEvent> appender;
    private boolean previousAdditive;
    private Level previousLevel;

    @BeforeEach
    void setUp() {
        GiftishowApiProperties properties = new GiftishowApiProperties();
        properties.setBaseUrl(BASE_URL);
        properties.setAuthCode("test-auth-code");
        properties.setAuthToken("test-auth-token");
        properties.setCallbackNo("0000000000");
        properties.setGubun("I");

        GiftishowApiProperties.Api api = new GiftishowApiProperties.Api();
        api.setProductListPath(PRODUCT_LIST_PATH);
        api.setProductDetailPath(PRODUCT_DETAIL_PATH);
        api.setBrandListPath(BRAND_LIST_PATH);
        api.setBrandDetailPath(BRAND_DETAIL_PATH);
        api.setCouponSendPath(COUPON_SEND_PATH);
        api.setCouponDetailPath(COUPON_DETAIL_PATH);
        api.setCouponCancelPath(COUPON_CANCEL_PATH);
        api.setCouponResendPath(COUPON_RESEND_PATH);
        api.setBizMoneyBalancePath(BIZMONEY_BALANCE_PATH);
        api.setCouponSendFailCancelPath(COUPON_SEND_FAIL_CANCEL_PATH);
        properties.setApi(api);

        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();

        ObjectMapper objectMapper = new ObjectMapper();
        meterRegistry = new SimpleMeterRegistry();
        ExternalCallLogger externalCallLogger = new ExternalCallLogger(meterRegistry);

        VendorCommunicationRecorder vendorCommunicationRecorder = Mockito.mock(VendorCommunicationRecorder.class);
        VendorCommunicationPayloadGenerator payloadGenerator = new VendorCommunicationPayloadGenerator(objectMapper);
        GiftishowOperationTargetTypeResolver resolver = new GiftishowOperationTargetTypeResolver();

        client = new GiftishowApiClient(
            properties, builder.build(), objectMapper, externalCallLogger,
            vendorCommunicationRecorder, payloadGenerator, resolver
        );

        giftishowLogger = (Logger) LoggerFactory.getLogger(ExternalSystem.GIFTISHOW.loggerName());
        previousAdditive = giftishowLogger.isAdditive();
        previousLevel = giftishowLogger.getLevel();
        giftishowLogger.setAdditive(false);
        giftishowLogger.setLevel(Level.DEBUG);
        appender = new ListAppender<>();
        appender.start();
        giftishowLogger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        giftishowLogger.detachAppender(appender);
        appender.stop();
        giftishowLogger.setAdditive(previousAdditive);
        giftishowLogger.setLevel(previousLevel);
    }

    @Nested
    @DisplayName("operation별 success Timer/DEBUG 로그")
    class SuccessOperations {

        @Test
        @DisplayName("상품 리스트 조회 성공 시 product.list success Timer가 기록된다")
        void shouldRecordSuccessForProductList() {
            mockServer.expect(requestTo(BASE_URL + PRODUCT_LIST_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(SUCCESS_BODY, MediaType.APPLICATION_JSON));

            client.getProductList(0, 20);

            assertSuccessTimerExists("product.list");
            assertOperationInDebugLog("product.list");
        }

        @Test
        @DisplayName("상품 상세 조회 성공 시 product.detail success Timer와 goods_code 태그가 로그에 포함된다")
        void shouldRecordSuccessForProductDetail() {
            mockServer.expect(requestTo(BASE_URL + PRODUCT_DETAIL_PATH + "/" + GOODS_CODE))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andRespond(withSuccess(SUCCESS_BODY, MediaType.APPLICATION_JSON));

            client.getProductDetail(GOODS_CODE);

            assertSuccessTimerExists("product.detail");
            assertTagInDebugLog("goods_code", GOODS_CODE);
        }

        @Test
        @DisplayName("브랜드 리스트 조회 성공 시 brand.list success Timer가 기록된다")
        void shouldRecordSuccessForBrandList() {
            mockServer.expect(requestTo(BASE_URL + BRAND_LIST_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(SUCCESS_BODY, MediaType.APPLICATION_JSON));

            client.getBrandList();

            assertSuccessTimerExists("brand.list");
        }

        @Test
        @DisplayName("브랜드 상세 조회 성공 시 brand.detail success Timer와 brand_code 태그가 로그에 포함된다")
        void shouldRecordSuccessForBrandDetail() {
            mockServer.expect(requestTo(BASE_URL + BRAND_DETAIL_PATH + "/" + BRAND_CODE))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andRespond(withSuccess(SUCCESS_BODY, MediaType.APPLICATION_JSON));

            client.getBrandDetail(BRAND_CODE);

            assertSuccessTimerExists("brand.detail");
            assertTagInDebugLog("brand_code", BRAND_CODE);
        }

        @Test
        @DisplayName("쿠폰 발송 성공 시 coupon.send success Timer와 tr_id/goods_code/user_id 태그가 로그에 포함된다")
        void shouldRecordSuccessForCouponSend() {
            mockServer.expect(requestTo(BASE_URL + COUPON_SEND_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(SUCCESS_BODY, MediaType.APPLICATION_JSON));

            client.sendCoupon(TR_ID, GOODS_CODE, PHONE_NO, USER_ID, "msg", "title", null);

            assertSuccessTimerExists("coupon.send");
            assertTagInDebugLog("tr_id", TR_ID);
            assertTagInDebugLog("goods_code", GOODS_CODE);
            assertTagInDebugLog("user_id", USER_ID);
        }

        @Test
        @DisplayName("쿠폰 상세 조회 성공 시 coupon.detail success Timer와 tr_id 태그가 로그에 포함된다")
        void shouldRecordSuccessForCouponDetail() {
            mockServer.expect(requestTo(BASE_URL + COUPON_DETAIL_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(SUCCESS_BODY, MediaType.APPLICATION_JSON));

            client.getCouponDetail(TR_ID);

            assertSuccessTimerExists("coupon.detail");
            assertTagInDebugLog("tr_id", TR_ID);
        }

        @Test
        @DisplayName("쿠폰 취소 성공 시 coupon.cancel success Timer와 tr_id/user_id 태그가 로그에 포함된다")
        void shouldRecordSuccessForCouponCancel() {
            mockServer.expect(requestTo(BASE_URL + COUPON_CANCEL_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(SUCCESS_BODY, MediaType.APPLICATION_JSON));

            client.cancelCoupon(TR_ID, USER_ID);

            assertSuccessTimerExists("coupon.cancel");
            assertTagInDebugLog("tr_id", TR_ID);
            assertTagInDebugLog("user_id", USER_ID);
        }

        @Test
        @DisplayName("쿠폰 재전송 성공 시 coupon.resend success Timer와 tr_id 태그가 로그에 포함된다")
        void shouldRecordSuccessForCouponResend() {
            mockServer.expect(requestTo(BASE_URL + COUPON_RESEND_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(SUCCESS_BODY, MediaType.APPLICATION_JSON));

            client.resendCoupon(TR_ID, PHONE_NO);

            assertSuccessTimerExists("coupon.resend");
            assertTagInDebugLog("tr_id", TR_ID);
        }

        @Test
        @DisplayName("비즈머니 잔액 조회 성공 시 bizmoney.balance success Timer와 user_id 태그가 로그에 포함된다")
        void shouldRecordSuccessForBizMoneyBalance() {
            mockServer.expect(requestTo(BASE_URL + BIZMONEY_BALANCE_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(SUCCESS_BODY, MediaType.APPLICATION_JSON));

            client.getBizMoneyBalance(USER_ID);

            assertSuccessTimerExists("bizmoney.balance");
            assertTagInDebugLog("user_id", USER_ID);
        }

        @Test
        @DisplayName("발송실패 취소 성공 시 coupon.sendFailCancel success Timer와 tr_id/user_id 태그가 로그에 포함된다")
        void shouldRecordSuccessForCouponSendFailCancel() {
            mockServer.expect(requestTo(BASE_URL + COUPON_SEND_FAIL_CANCEL_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(SUCCESS_BODY, MediaType.APPLICATION_JSON));

            client.cancelSendFailedCoupon(TR_ID, USER_ID);

            assertSuccessTimerExists("coupon.sendFailCancel");
            assertTagInDebugLog("tr_id", TR_ID);
            assertTagInDebugLog("user_id", USER_ID);
        }
    }

    @Nested
    @DisplayName("예외 발생 시 failure Timer/ERROR 로그")
    class FailureOperations {

        @Test
        @DisplayName("상품 리스트 5xx 응답 시 product.list failure Timer가 기록되고 동일 예외를 rethrow한다")
        void shouldRecordFailureWhenProductListFails() {
            mockServer.expect(requestTo(BASE_URL + PRODUCT_LIST_PATH))
                .andRespond(withServerError());

            assertThatThrownBy(() -> client.getProductList(0, 20))
                .isInstanceOf(RuntimeException.class);

            assertFailureTimerExists("product.list");
            assertErrorOperationInLog("product.list");
        }

        @Test
        @DisplayName("쿠폰 발송 응답 본문이 비어 GiftishowCommunicationException 발생 시 coupon.send failure Timer가 기록된다")
        void shouldRecordFailureOnEmptyCouponSendResponse() {
            mockServer.expect(requestTo(BASE_URL + COUPON_SEND_PATH))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

            assertThatThrownBy(() -> client.sendCoupon(
                TR_ID, GOODS_CODE, PHONE_NO, USER_ID, "msg", "title", null
            )).isInstanceOf(GiftishowCommunicationException.class);

            assertFailureTimerExists("coupon.send");
        }
    }

    @Nested
    @DisplayName("PCI-DSS 회귀 검증 (민감 정보 미포함)")
    class SensitiveDataAbsenceTests {

        @Test
        @DisplayName("쿠폰 발송 태그/로그에 mms_msg/mms_title 페이로드가 포함되지 않는다")
        void shouldNotIncludeMmsPayloadInCouponSendLog() {
            String mmsMsg = "VERY-SECRET-MMS-MSG";
            String mmsTitle = "VERY-SECRET-MMS-TITLE";
            mockServer.expect(requestTo(BASE_URL + COUPON_SEND_PATH))
                .andRespond(withSuccess(SUCCESS_BODY, MediaType.APPLICATION_JSON));

            client.sendCoupon(TR_ID, GOODS_CODE, PHONE_NO, USER_ID, mmsMsg, mmsTitle, null);

            assertNoLogContains(mmsMsg);
            assertNoLogContains(mmsTitle);
        }

        @Test
        @DisplayName("인증 파라미터(custom_auth_code/custom_auth_token)가 외부 호출 로그에 포함되지 않는다")
        void shouldNotIncludeAuthParamsInLog() {
            mockServer.expect(requestTo(BASE_URL + COUPON_DETAIL_PATH))
                .andRespond(withSuccess(SUCCESS_BODY, MediaType.APPLICATION_JSON));

            client.getCouponDetail(TR_ID);

            assertNoLogContains("test-auth-code");
            assertNoLogContains("test-auth-token");
        }
    }

    private void assertSuccessTimerExists(String operation) {
        Timer timer = meterRegistry.find("external.call")
            .tags("system", ExternalSystem.GIFTISHOW.metricTag(),
                "operation", operation,
                "outcome", "success"
            )
            .timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
    }

    private void assertFailureTimerExists(String operation) {
        Timer timer = meterRegistry.find("external.call")
            .tags("system", ExternalSystem.GIFTISHOW.metricTag(),
                "operation", operation,
                "outcome", "failure"
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

    private void assertErrorOperationInLog(String operation) {
        boolean found = appender.list.stream()
            .anyMatch(event -> event.getLevel() == Level.ERROR
                && event.getFormattedMessage().contains(operation));
        assertThat(found).as("expected ERROR log for operation '%s'", operation).isTrue();
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
