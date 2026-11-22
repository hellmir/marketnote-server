package com.personal.marketnote.reward.adapter.out.vendor.giftishow;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.GiftishowCommunicationException;
import com.personal.marketnote.reward.configuration.GiftishowApiProperties;
import com.personal.marketnote.reward.utility.VendorCommunicationRecorder;
import org.mockito.Mockito;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("GiftishowApiClient sensitive path 분류 메타데이터 기반 동작 테스트")
class GiftishowApiClientSensitivePathClassificationTest {

    private static final String BASE_URL = "http://localhost:9999";
    private static final String COUPON_SEND_PATH = "/bizApi/send";
    private static final String COUPON_DETAIL_PATH = "/bizApi/coupons";
    private static final String COUPON_RESEND_PATH = "/bizApi/resend";
    private static final String COUPON_CANCEL_PATH = "/bizApi/cancel";
    private static final String COUPON_SEND_FAIL_CANCEL_PATH = "/bizApi/sendFail/cancel";
    private static final String SENSITIVE_BODY_WITH_PIN = "invalid_json_with_pin_1234-5678-9012-3456";
    private static final String NON_SENSITIVE_INVALID_BODY = "invalid json";

    private MockRestServiceServer mockServer;
    private GiftishowApiClient giftishowApiClient;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger clientLogger;

    @BeforeEach
    void setUp() {
        GiftishowApiProperties properties = new GiftishowApiProperties();
        properties.setBaseUrl(BASE_URL);
        properties.setAuthCode("test-auth-code");
        properties.setAuthToken("test-auth-token");
        properties.setCallbackNo("0000000000");
        properties.setGubun("I");

        GiftishowApiProperties.Api api = new GiftishowApiProperties.Api();
        api.setProductListPath("/bizApi/goods");
        api.setProductDetailPath("/bizApi/goods");
        api.setBrandListPath("/bizApi/brands");
        api.setBrandDetailPath("/bizApi/brands");
        api.setCouponSendPath(COUPON_SEND_PATH);
        api.setCouponDetailPath(COUPON_DETAIL_PATH);
        api.setCouponCancelPath(COUPON_CANCEL_PATH);
        api.setCouponResendPath(COUPON_RESEND_PATH);
        api.setBizMoneyBalancePath("/bizApi/bizmoney");
        api.setCouponSendFailCancelPath(COUPON_SEND_FAIL_CANCEL_PATH);
        api.setSensitivePaths(Set.of(COUPON_SEND_PATH, COUPON_DETAIL_PATH, COUPON_RESEND_PATH));
        properties.setApi(api);

        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();

        ObjectMapper objectMapper = new ObjectMapper();
        ExternalCallLogger externalCallLogger = new ExternalCallLogger(new SimpleMeterRegistry());
        VendorCommunicationRecorder vendorCommunicationRecorder = Mockito.mock(VendorCommunicationRecorder.class);
        VendorCommunicationPayloadGenerator payloadGenerator = new VendorCommunicationPayloadGenerator(objectMapper);
        GiftishowOperationTargetTypeResolver resolver = new GiftishowOperationTargetTypeResolver();
        giftishowApiClient = new GiftishowApiClient(
                properties, builder.build(), objectMapper, externalCallLogger,
                vendorCommunicationRecorder, payloadGenerator, resolver
        );

        clientLogger = (Logger) LoggerFactory.getLogger(GiftishowApiClient.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        clientLogger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        clientLogger.detachAppender(listAppender);
        listAppender.stop();
    }

    @Test
    @DisplayName("sensitivePaths에 등록된 couponSend 경로는 cause를 제거하여 raw body 노출을 차단한다")
    void shouldMaskWhenCouponSendPathIsRegisteredAsSensitive() {
        // given
        mockServer.expect(requestTo(BASE_URL + COUPON_SEND_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(SENSITIVE_BODY_WITH_PIN, MediaType.APPLICATION_JSON));

        // when & then
        assertThatThrownBy(() -> giftishowApiClient.sendCoupon(
                "TR_001", "G001", "01012345678", "user1", "msg", "title"
        ))
                .isInstanceOf(GiftishowCommunicationException.class)
                .hasMessageContaining(COUPON_SEND_PATH)
                .hasNoCause();

        ILoggingEvent errorEvent = findErrorEvent();
        assertThat(errorEvent.getFormattedMessage()).doesNotContain("1234-5678-9012-3456");
        assertThat(errorEvent.getThrowableProxy()).isNull();

        mockServer.verify();
    }

    @Test
    @DisplayName("sensitivePaths에 등록된 couponDetail 경로는 cause를 제거하여 raw body 노출을 차단한다")
    void shouldMaskWhenCouponDetailPathIsRegisteredAsSensitive() {
        // given
        mockServer.expect(requestTo(BASE_URL + COUPON_DETAIL_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(SENSITIVE_BODY_WITH_PIN, MediaType.APPLICATION_JSON));

        // when & then
        assertThatThrownBy(() -> giftishowApiClient.getCouponDetail("TR_001"))
                .isInstanceOf(GiftishowCommunicationException.class)
                .hasMessageContaining(COUPON_DETAIL_PATH)
                .hasNoCause();

        ILoggingEvent errorEvent = findErrorEvent();
        assertThat(errorEvent.getFormattedMessage()).doesNotContain("1234-5678-9012-3456");
        assertThat(errorEvent.getThrowableProxy()).isNull();

        mockServer.verify();
    }

    @Test
    @DisplayName("sensitivePaths에 등록된 couponResend 경로는 cause를 제거하여 raw body 노출을 차단한다")
    void shouldMaskWhenCouponResendPathIsRegisteredAsSensitive() {
        // given
        mockServer.expect(requestTo(BASE_URL + COUPON_RESEND_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(SENSITIVE_BODY_WITH_PIN, MediaType.APPLICATION_JSON));

        // when & then
        assertThatThrownBy(() -> giftishowApiClient.resendCoupon("TR_001", "01012345678"))
                .isInstanceOf(GiftishowCommunicationException.class)
                .hasMessageContaining(COUPON_RESEND_PATH)
                .hasNoCause();

        ILoggingEvent errorEvent = findErrorEvent();
        assertThat(errorEvent.getFormattedMessage()).doesNotContain("1234-5678-9012-3456");
        assertThat(errorEvent.getThrowableProxy()).isNull();

        mockServer.verify();
    }

    @Test
    @DisplayName("sensitivePaths에 미등록된 couponCancel 경로는 cause를 보존하여 stack trace를 남긴다")
    void shouldPreserveCauseWhenCouponCancelPathIsNotRegisteredAsSensitive() {
        // given
        mockServer.expect(requestTo(BASE_URL + COUPON_CANCEL_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(NON_SENSITIVE_INVALID_BODY, MediaType.APPLICATION_JSON));

        // when & then
        assertThatThrownBy(() -> giftishowApiClient.cancelCoupon("TR_001", "user1"))
                .isInstanceOf(GiftishowCommunicationException.class)
                .hasMessageContaining(COUPON_CANCEL_PATH)
                .hasCauseInstanceOf(JsonProcessingException.class);

        ILoggingEvent errorEvent = findErrorEvent();
        assertThat(errorEvent.getThrowableProxy()).isNotNull();

        mockServer.verify();
    }

    @Test
    @DisplayName("sensitivePaths에 미등록된 couponSendFailCancel 경로는 cause를 보존하여 stack trace를 남긴다")
    void shouldPreserveCauseWhenCouponSendFailCancelPathIsNotRegisteredAsSensitive() {
        // given
        mockServer.expect(requestTo(BASE_URL + COUPON_SEND_FAIL_CANCEL_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(NON_SENSITIVE_INVALID_BODY, MediaType.APPLICATION_JSON));

        // when & then
        assertThatThrownBy(() -> giftishowApiClient.cancelSendFailedCoupon("TR_001", "user1"))
                .isInstanceOf(GiftishowCommunicationException.class)
                .hasMessageContaining(COUPON_SEND_FAIL_CANCEL_PATH)
                .hasCauseInstanceOf(JsonProcessingException.class);

        ILoggingEvent errorEvent = findErrorEvent();
        assertThat(errorEvent.getThrowableProxy()).isNotNull();

        mockServer.verify();
    }

    private ILoggingEvent findErrorEvent() {
        List<ILoggingEvent> events = listAppender.list;
        return events.stream()
                .filter(event -> event.getLevel() == Level.ERROR)
                .findFirst()
                .orElseThrow(() -> new AssertionError("ERROR 레벨 로그 이벤트를 찾을 수 없습니다."));
    }
}
