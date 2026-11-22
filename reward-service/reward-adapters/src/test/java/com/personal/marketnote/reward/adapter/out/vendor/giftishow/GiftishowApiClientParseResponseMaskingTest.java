package com.personal.marketnote.reward.adapter.out.vendor.giftishow;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
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

@DisplayName("GiftishowApiClient parseResponse sensitive path 마스킹 테스트")
class GiftishowApiClientParseResponseMaskingTest {

    private static final String BASE_URL = "http://localhost:9999";
    private static final String COUPON_SEND_PATH = "/bizApi/send";
    private static final String COUPON_DETAIL_PATH = "/bizApi/coupons";
    private static final String COUPON_RESEND_PATH = "/bizApi/resend";
    private static final String COUPON_CANCEL_PATH = "/bizApi/cancel";
    private static final String COUPON_SEND_FAIL_CANCEL_PATH = "/bizApi/sendFail/cancel";
    private static final String PRODUCT_LIST_PATH = "/bizApi/goods";
    private static final String SENSITIVE_BODY_WITH_PIN = "invalid_json_with_pin_1234-5678-9012-3456";
    private static final String NON_SENSITIVE_INVALID_BODY = "invalid json";

    private MockRestServiceServer mockServer;
    private GiftishowApiClient giftishowApiClient;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger clientLogger;
    private boolean previousAdditive;
    private Level previousLevel;

    @BeforeEach
    void setUp() {
        GiftishowApiProperties properties = new GiftishowApiProperties();
        properties.setBaseUrl(BASE_URL);
        properties.setApiCode("test-api-code");
        properties.setAuthCode("test-auth-code");
        properties.setAuthToken("test-auth-token");
        properties.setDevYn("Y");
        properties.setCallbackNo("0000000000");
        properties.setGubun("I");

        GiftishowApiProperties.Api api = new GiftishowApiProperties.Api();
        api.setProductListPath(PRODUCT_LIST_PATH);
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
        previousAdditive = clientLogger.isAdditive();
        previousLevel = clientLogger.getLevel();
        clientLogger.setAdditive(false);
        listAppender = new ListAppender<>();
        listAppender.start();
        clientLogger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        clientLogger.detachAppender(listAppender);
        listAppender.stop();
        clientLogger.setAdditive(previousAdditive);
        clientLogger.setLevel(previousLevel);
    }

    @Test
    @DisplayName("sensitive path 응답 파싱 실패 시 로그에 예외 메시지가 포함되지 않는다")
    void shouldNotLogExceptionMessageWhenSensitivePathParseFails() {
        // given
        mockServer.expect(requestTo(BASE_URL + COUPON_SEND_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(SENSITIVE_BODY_WITH_PIN, MediaType.APPLICATION_JSON));

        // when
        assertThatThrownBy(() -> giftishowApiClient.sendCoupon(
                "TR_001", "G001", "01012345678", "user1", "msg", "title"
        )).isInstanceOf(GiftishowCommunicationException.class);

        // then
        ILoggingEvent errorEvent = findErrorEvent();
        String formattedMessage = errorEvent.getFormattedMessage();
        assertThat(formattedMessage).doesNotContain("1234-5678-9012-3456");
        assertThat(formattedMessage).doesNotContain("invalid_json_with_pin");
        assertThat(errorEvent.getThrowableProxy()).isNull();

        mockServer.verify();
    }

    @Test
    @DisplayName("sensitive path 응답 파싱 실패 시 로그에 예외 클래스명이 포함된다")
    void shouldLogExceptionClassNameWhenSensitivePathParseFails() {
        // given
        mockServer.expect(requestTo(BASE_URL + COUPON_SEND_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(SENSITIVE_BODY_WITH_PIN, MediaType.APPLICATION_JSON));

        // when
        assertThatThrownBy(() -> giftishowApiClient.sendCoupon(
                "TR_001", "G001", "01012345678", "user1", "msg", "title"
        )).isInstanceOf(GiftishowCommunicationException.class);

        // then
        ILoggingEvent errorEvent = findErrorEvent();
        String formattedMessage = errorEvent.getFormattedMessage();
        assertThat(formattedMessage).contains(COUPON_SEND_PATH);
        assertThat(formattedMessage).containsAnyOf(
                "JsonParseException",
                "JsonProcessingException",
                "MismatchedInputException"
        );

        mockServer.verify();
    }

    @Test
    @DisplayName("sensitive path 응답 파싱 실패 시 GiftishowCommunicationException은 cause를 제거하여 raw body 노출을 차단한다")
    void shouldThrowExceptionWithoutCauseWhenSensitivePathParseFails() {
        // given
        mockServer.expect(requestTo(BASE_URL + COUPON_SEND_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(SENSITIVE_BODY_WITH_PIN, MediaType.APPLICATION_JSON));

        // when & then
        assertThatThrownBy(() -> giftishowApiClient.sendCoupon(
                "TR_001", "G001", "01012345678", "user1", "msg", "title"
        ))
                .isInstanceOf(GiftishowCommunicationException.class)
                .hasMessageContaining("응답 파싱 실패")
                .hasMessageContaining(COUPON_SEND_PATH)
                .hasNoCause();

        mockServer.verify();
    }

    @Test
    @DisplayName("non-sensitive path 응답 파싱 실패 시 로그에 예외 stack trace가 포함된다")
    void shouldLogStackTraceWhenNonSensitivePathParseFails() {
        // given
        mockServer.expect(requestTo(BASE_URL + PRODUCT_LIST_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(NON_SENSITIVE_INVALID_BODY, MediaType.APPLICATION_JSON));

        // when
        assertThatThrownBy(() -> giftishowApiClient.getProductList(0, 20))
                .isInstanceOf(GiftishowCommunicationException.class);

        // then
        ILoggingEvent errorEvent = findErrorEvent();
        assertThat(errorEvent.getThrowableProxy()).isNotNull();
        assertThat(errorEvent.getThrowableProxy().getClassName())
                .containsAnyOf("JsonParseException", "JsonProcessingException", "MismatchedInputException");

        mockServer.verify();
    }

    @Test
    @DisplayName("non-sensitive path 응답 파싱 실패 시 GiftishowCommunicationException이 throw되며 cause는 원본 예외이다")
    void shouldThrowExceptionWithCauseWhenNonSensitivePathParseFails() {
        // given
        mockServer.expect(requestTo(BASE_URL + PRODUCT_LIST_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(NON_SENSITIVE_INVALID_BODY, MediaType.APPLICATION_JSON));

        // when & then
        assertThatThrownBy(() -> giftishowApiClient.getProductList(0, 20))
                .isInstanceOf(GiftishowCommunicationException.class)
                .hasMessageContaining("응답 파싱 실패")
                .hasMessageContaining(PRODUCT_LIST_PATH)
                .hasCauseInstanceOf(com.fasterxml.jackson.core.JsonProcessingException.class);

        mockServer.verify();
    }

    @Test
    @DisplayName("couponDetail sensitive path 응답 파싱 실패 시 cause를 제거하여 raw body 노출을 차단한다")
    void shouldMaskWhenCouponDetailParseFails() {
        // given
        mockServer.expect(requestTo(BASE_URL + COUPON_DETAIL_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(SENSITIVE_BODY_WITH_PIN, MediaType.APPLICATION_JSON));

        // when & then
        assertThatThrownBy(() -> giftishowApiClient.getCouponDetail("TR_001"))
                .isInstanceOf(GiftishowCommunicationException.class)
                .hasMessageContaining("응답 파싱 실패")
                .hasMessageContaining(COUPON_DETAIL_PATH)
                .hasNoCause();

        ILoggingEvent errorEvent = findErrorEvent();
        String formattedMessage = errorEvent.getFormattedMessage();
        assertThat(formattedMessage).doesNotContain("1234-5678-9012-3456");
        assertThat(formattedMessage).doesNotContain("invalid_json_with_pin");
        assertThat(errorEvent.getThrowableProxy()).isNull();

        mockServer.verify();
    }

    @Test
    @DisplayName("couponResend sensitive path 응답 파싱 실패 시 cause를 제거하여 raw body 노출을 차단한다")
    void shouldMaskWhenCouponResendParseFails() {
        // given
        mockServer.expect(requestTo(BASE_URL + COUPON_RESEND_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(SENSITIVE_BODY_WITH_PIN, MediaType.APPLICATION_JSON));

        // when & then
        assertThatThrownBy(() -> giftishowApiClient.resendCoupon("TR_001", "01012345678"))
                .isInstanceOf(GiftishowCommunicationException.class)
                .hasMessageContaining("응답 파싱 실패")
                .hasMessageContaining(COUPON_RESEND_PATH)
                .hasNoCause();

        ILoggingEvent errorEvent = findErrorEvent();
        String formattedMessage = errorEvent.getFormattedMessage();
        assertThat(formattedMessage).doesNotContain("1234-5678-9012-3456");
        assertThat(formattedMessage).doesNotContain("invalid_json_with_pin");
        assertThat(errorEvent.getThrowableProxy()).isNull();

        mockServer.verify();
    }

    @Test
    @DisplayName("couponCancel non-sensitive path 응답 파싱 실패 시 stack trace를 포함하고 cause를 보존한다")
    void shouldPreserveCauseWhenCouponCancelParseFails() {
        // given
        mockServer.expect(requestTo(BASE_URL + COUPON_CANCEL_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(NON_SENSITIVE_INVALID_BODY, MediaType.APPLICATION_JSON));

        // when & then
        assertThatThrownBy(() -> giftishowApiClient.cancelCoupon("TR_001", "user1"))
                .isInstanceOf(GiftishowCommunicationException.class)
                .hasMessageContaining("응답 파싱 실패")
                .hasMessageContaining(COUPON_CANCEL_PATH)
                .hasCauseInstanceOf(com.fasterxml.jackson.core.JsonProcessingException.class);

        ILoggingEvent errorEvent = findErrorEvent();
        assertThat(errorEvent.getThrowableProxy()).isNotNull();
        assertThat(errorEvent.getThrowableProxy().getClassName())
                .containsAnyOf("JsonParseException", "JsonProcessingException", "MismatchedInputException");

        mockServer.verify();
    }

    @Test
    @DisplayName("couponSendFailCancel non-sensitive path 응답 파싱 실패 시 stack trace를 포함하고 cause를 보존한다")
    void shouldPreserveCauseWhenCouponSendFailCancelParseFails() {
        // given
        mockServer.expect(requestTo(BASE_URL + COUPON_SEND_FAIL_CANCEL_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(NON_SENSITIVE_INVALID_BODY, MediaType.APPLICATION_JSON));

        // when & then
        assertThatThrownBy(() -> giftishowApiClient.cancelSendFailedCoupon("TR_001", "user1"))
                .isInstanceOf(GiftishowCommunicationException.class)
                .hasMessageContaining("응답 파싱 실패")
                .hasMessageContaining(COUPON_SEND_FAIL_CANCEL_PATH)
                .hasCauseInstanceOf(com.fasterxml.jackson.core.JsonProcessingException.class);

        ILoggingEvent errorEvent = findErrorEvent();
        assertThat(errorEvent.getThrowableProxy()).isNotNull();
        assertThat(errorEvent.getThrowableProxy().getClassName())
                .containsAnyOf("JsonParseException", "JsonProcessingException", "MismatchedInputException");

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
