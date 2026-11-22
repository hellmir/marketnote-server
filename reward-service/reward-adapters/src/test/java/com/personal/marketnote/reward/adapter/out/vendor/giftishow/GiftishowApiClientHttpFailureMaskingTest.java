package com.personal.marketnote.reward.adapter.out.vendor.giftishow;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.external.log.ExternalSystem;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@DisplayName("GiftishowApiClient HTTP 응답 실패 sensitive path 마스킹 테스트")
class GiftishowApiClientHttpFailureMaskingTest {

    private static final String BASE_URL = "http://localhost:9999";
    private static final String COUPON_SEND_PATH = "/bizApi/send";
    private static final String COUPON_DETAIL_PATH = "/bizApi/coupons";
    private static final String COUPON_RESEND_PATH = "/bizApi/resend";
    private static final String COUPON_CANCEL_PATH = "/bizApi/cancel";
    private static final String COUPON_SEND_FAIL_CANCEL_PATH = "/bizApi/sendFail/cancel";
    private static final String PRODUCT_LIST_PATH = "/bizApi/goods";
    private static final String SENSITIVE_BODY_WITH_PIN =
            "{\"resultData\":{\"pin_no\":\"1234-5678-9012-3456\",\"trId\":\"TR_001\"}}";
    private static final String NON_SENSITIVE_ERROR_BODY = "{\"resultCode\":\"E0001\",\"resultMessage\":\"server error\"}";

    private MockRestServiceServer mockServer;
    private GiftishowApiClient giftishowApiClient;
    private ListAppender<ILoggingEvent> clientAppender;
    private ListAppender<ILoggingEvent> externalAppender;
    private Logger clientLogger;
    private Logger externalLogger;
    private boolean clientPreviousAdditive;
    private Level clientPreviousLevel;
    private boolean externalPreviousAdditive;
    private Level externalPreviousLevel;

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
        clientPreviousAdditive = clientLogger.isAdditive();
        clientPreviousLevel = clientLogger.getLevel();
        clientLogger.setAdditive(false);
        clientAppender = new ListAppender<>();
        clientAppender.start();
        clientLogger.addAppender(clientAppender);

        externalLogger = (Logger) LoggerFactory.getLogger(ExternalSystem.GIFTISHOW.loggerName());
        externalPreviousAdditive = externalLogger.isAdditive();
        externalPreviousLevel = externalLogger.getLevel();
        externalLogger.setAdditive(false);
        externalAppender = new ListAppender<>();
        externalAppender.start();
        externalLogger.addAppender(externalAppender);
    }

    @AfterEach
    void tearDown() {
        clientLogger.detachAppender(clientAppender);
        clientAppender.stop();
        clientLogger.setAdditive(clientPreviousAdditive);
        clientLogger.setLevel(clientPreviousLevel);

        externalLogger.detachAppender(externalAppender);
        externalAppender.stop();
        externalLogger.setAdditive(externalPreviousAdditive);
        externalLogger.setLevel(externalPreviousLevel);
    }

    @Test
    @DisplayName("sensitive path 4xx 응답 시 raw body가 예외 메시지/로그에 노출되지 않고 cause를 제거한다")
    void shouldMaskRawBodyWhenSensitivePathReturns4xx() {
        // given
        mockServer.expect(requestTo(BASE_URL + COUPON_SEND_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(SENSITIVE_BODY_WITH_PIN));

        // when & then
        assertThatThrownBy(() -> giftishowApiClient.sendCoupon(
                "TR_001", "G001", "01012345678", "user1", "msg", "title"
        ))
                .isInstanceOf(GiftishowCommunicationException.class)
                .hasMessageContaining("HTTP 응답 실패")
                .hasMessageContaining(COUPON_SEND_PATH)
                .hasMessageContaining("status=401")
                .hasMessageNotContaining("1234-5678-9012-3456")
                .hasNoCause();

        // client logger (sensitive 분기): raw body 없어야 하고 throwable 자체가 첨부되지 않아야 함
        assertThat(clientAppender.list)
                .filteredOn(event -> event.getLevel() == Level.ERROR)
                .allSatisfy(event -> {
                    assertThat(event.getFormattedMessage()).doesNotContain("1234-5678-9012-3456");
                    assertThat(event.getThrowableProxy()).isNull();
                });

        // external logger (ExternalCallLogger.recordFailure): GiftishowCommunicationException 자체는 첨부되지만,
        // 메시지/스택/cause 어디에도 raw body가 포함되어선 안 된다.
        assertThat(externalAppender.list)
                .filteredOn(event -> event.getLevel() == Level.ERROR)
                .allSatisfy(event -> {
                    assertThat(event.getFormattedMessage()).doesNotContain("1234-5678-9012-3456");
                    assertNoRawBodyInThrowableChain(event, "1234-5678-9012-3456");
                });

        mockServer.verify();
    }

    @Test
    @DisplayName("sensitive path 5xx 응답 시에도 raw body가 노출되지 않고 cause를 제거한다")
    void shouldMaskRawBodyWhenSensitivePathReturns5xx() {
        // given
        mockServer.expect(requestTo(BASE_URL + COUPON_DETAIL_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(SENSITIVE_BODY_WITH_PIN));

        // when & then
        assertThatThrownBy(() -> giftishowApiClient.getCouponDetail("TR_001"))
                .isInstanceOf(GiftishowCommunicationException.class)
                .hasMessageContaining("HTTP 응답 실패")
                .hasMessageContaining(COUPON_DETAIL_PATH)
                .hasMessageContaining("status=500")
                .hasMessageNotContaining("1234-5678-9012-3456")
                .hasNoCause();

        assertThat(clientAppender.list)
                .filteredOn(event -> event.getLevel() == Level.ERROR)
                .allSatisfy(event -> {
                    assertThat(event.getFormattedMessage()).doesNotContain("1234-5678-9012-3456");
                    assertThat(event.getThrowableProxy()).isNull();
                });

        assertThat(externalAppender.list)
                .filteredOn(event -> event.getLevel() == Level.ERROR)
                .allSatisfy(event -> {
                    assertThat(event.getFormattedMessage()).doesNotContain("1234-5678-9012-3456");
                    assertNoRawBodyInThrowableChain(event, "1234-5678-9012-3456");
                });

        mockServer.verify();
    }

    @Test
    @DisplayName("sensitive path 4xx 응답 시 client 로그에 path/status/exception 클래스명만 기록된다")
    void shouldLogPathAndStatusWhenSensitivePathHttpFailure() {
        // given
        mockServer.expect(requestTo(BASE_URL + COUPON_RESEND_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(SENSITIVE_BODY_WITH_PIN));

        // when
        assertThatThrownBy(() -> giftishowApiClient.resendCoupon("TR_001", "01012345678"))
                .isInstanceOf(GiftishowCommunicationException.class);

        // then
        ILoggingEvent errorEvent = findClientErrorEvent();
        String formattedMessage = errorEvent.getFormattedMessage();
        assertThat(formattedMessage).contains(COUPON_RESEND_PATH);
        assertThat(formattedMessage).contains("status=400");
        assertThat(formattedMessage).containsAnyOf(
                "HttpClientErrorException",
                "RestClientResponseException",
                "BadRequest"
        );

        mockServer.verify();
    }

    @Test
    @DisplayName("non-sensitive path 5xx 응답 시 cause는 보존되고 stack trace가 client 로그에 포함된다")
    void shouldPreserveCauseWhenNonSensitivePathHttpFailure() {
        // given
        mockServer.expect(requestTo(BASE_URL + PRODUCT_LIST_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(NON_SENSITIVE_ERROR_BODY));

        // when & then
        assertThatThrownBy(() -> giftishowApiClient.getProductList(0, 20))
                .isInstanceOf(GiftishowCommunicationException.class)
                .hasMessageContaining("HTTP 응답 실패")
                .hasMessageContaining(PRODUCT_LIST_PATH)
                .hasMessageContaining("status=500")
                .hasCauseInstanceOf(org.springframework.web.client.RestClientResponseException.class);

        ILoggingEvent errorEvent = findClientErrorEvent();
        assertThat(errorEvent.getThrowableProxy()).isNotNull();
        assertThat(errorEvent.getThrowableProxy().getClassName())
                .containsAnyOf(
                        "RestClientResponseException",
                        "HttpServerErrorException",
                        "InternalServerError"
                );

        mockServer.verify();
    }

    @Test
    @DisplayName("non-sensitive path /bizApi/cancel 5xx 응답 시 cause는 보존되고 stack trace가 client 로그에 포함된다")
    void shouldPreserveCauseWhenCouponCancelHttpFailure() {
        // given
        mockServer.expect(requestTo(BASE_URL + COUPON_CANCEL_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(NON_SENSITIVE_ERROR_BODY));

        // when & then
        assertThatThrownBy(() -> giftishowApiClient.cancelCoupon("TR_001", "user1"))
                .isInstanceOf(GiftishowCommunicationException.class)
                .hasMessageContaining("HTTP 응답 실패")
                .hasMessageContaining(COUPON_CANCEL_PATH)
                .hasMessageContaining("status=500")
                .hasCauseInstanceOf(org.springframework.web.client.RestClientResponseException.class);

        ILoggingEvent errorEvent = findClientErrorEvent();
        assertThat(errorEvent.getThrowableProxy()).isNotNull();
        assertThat(errorEvent.getThrowableProxy().getClassName())
                .containsAnyOf(
                        "RestClientResponseException",
                        "HttpServerErrorException",
                        "InternalServerError"
                );

        mockServer.verify();
    }

    @Test
    @DisplayName("non-sensitive path /bizApi/sendFail/cancel 5xx 응답 시 cause는 보존되고 stack trace가 client 로그에 포함된다")
    void shouldPreserveCauseWhenCouponSendFailCancelHttpFailure() {
        // given
        mockServer.expect(requestTo(BASE_URL + COUPON_SEND_FAIL_CANCEL_PATH))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(NON_SENSITIVE_ERROR_BODY));

        // when & then
        assertThatThrownBy(() -> giftishowApiClient.cancelSendFailedCoupon("TR_001", "user1"))
                .isInstanceOf(GiftishowCommunicationException.class)
                .hasMessageContaining("HTTP 응답 실패")
                .hasMessageContaining(COUPON_SEND_FAIL_CANCEL_PATH)
                .hasMessageContaining("status=500")
                .hasCauseInstanceOf(org.springframework.web.client.RestClientResponseException.class);

        ILoggingEvent errorEvent = findClientErrorEvent();
        assertThat(errorEvent.getThrowableProxy()).isNotNull();
        assertThat(errorEvent.getThrowableProxy().getClassName())
                .containsAnyOf(
                        "RestClientResponseException",
                        "HttpServerErrorException",
                        "InternalServerError"
                );

        mockServer.verify();
    }

    private ILoggingEvent findClientErrorEvent() {
        List<ILoggingEvent> events = clientAppender.list;
        return events.stream()
                .filter(event -> event.getLevel() == Level.ERROR)
                .findFirst()
                .orElseThrow(() -> new AssertionError("ERROR 레벨 client 로그 이벤트를 찾을 수 없습니다."));
    }

    private void assertNoRawBodyInThrowableChain(ILoggingEvent event, String forbidden) {
        IThrowableProxy proxy = event.getThrowableProxy();
        while (proxy != null) {
            String message = proxy.getMessage();
            if (message != null) {
                assertThat(message).doesNotContain(forbidden);
            }
            StackTraceElementProxy[] stack = proxy.getStackTraceElementProxyArray();
            if (stack != null) {
                for (StackTraceElementProxy element : stack) {
                    assertThat(element.getSTEAsString()).doesNotContain(forbidden);
                }
            }
            proxy = proxy.getCause();
        }
    }
}
