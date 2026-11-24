package com.personal.marketnote.reward.adapter.out.vendor.giftishow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.GiftishowCommunicationException;
import com.personal.marketnote.reward.configuration.GiftishowApiProperties;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationSenderType;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationTargetType;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationType;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorName;
import com.personal.marketnote.reward.utility.VendorCommunicationRecorder;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Giftishow ApiClient의 vendor_communication_history 영속 검증 테스트.
 *
 * <p>핵심:
 * <ul>
 *   <li>10개 메서드 호출 시 REQUEST/RESPONSE 2건 record</li>
 *   <li>sensitive path(쿠폰 발송/조회/재전송) 응답은 비식별화 토큰만 영속 — 핀번호/폰번호 raw 0건</li>
 *   <li>실패 시에도 응답 record 발생 (silent fail 금지)</li>
 *   <li>operation → targetType 매핑 정합 (COUPON / BIZMONEY)</li>
 * </ul>
 */
@DisplayName("GiftishowApiClient vendor_communication_history 영속 테스트")
class GiftishowApiClientVendorCommunicationRecordTest {

    private static final String BASE_URL = "http://localhost:9999";
    private static final String AUTH_CODE = "test-auth-code";
    private static final String AUTH_TOKEN = "test-auth-token";

    private static final String PIN_NO = "1234-5678-9012";
    private static final String PHONE_NO = "01098765432";
    private static final String TR_ID = "NTCASH_1_20260403120000";
    private static final String GOODS_CODE = "G001";
    private static final String BRAND_CODE = "B001";
    private static final String USER_ID = "user-1";

    private MockRestServiceServer mockServer;
    private GiftishowApiClient giftishowApiClient;
    private VendorCommunicationRecorder vendorCommunicationRecorder;

    @BeforeEach
    void setUp() {
        GiftishowApiProperties properties = new GiftishowApiProperties();
        properties.setBaseUrl(BASE_URL);
        properties.setAuthCode(AUTH_CODE);
        properties.setAuthToken(AUTH_TOKEN);
        properties.setCallbackNo("0000000000");
        properties.setGubun("I");

        GiftishowApiProperties.Api api = new GiftishowApiProperties.Api();
        api.setProductListPath("/bizApi/goods");
        api.setProductDetailPath("/bizApi/goods");
        api.setBrandListPath("/bizApi/brands");
        api.setBrandDetailPath("/bizApi/brands");
        api.setCouponSendPath("/bizApi/send");
        api.setCouponDetailPath("/bizApi/coupons");
        api.setCouponCancelPath("/bizApi/cancel");
        api.setCouponResendPath("/bizApi/resend");
        api.setBizMoneyBalancePath("/bizApi/bizmoney");
        api.setCouponSendFailCancelPath("/bizApi/sendFail/cancel");
        api.setSensitivePaths(Set.of("/bizApi/send", "/bizApi/coupons", "/bizApi/resend"));
        properties.setApi(api);

        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();

        ObjectMapper objectMapper = new ObjectMapper();
        ExternalCallLogger externalCallLogger = new ExternalCallLogger(new SimpleMeterRegistry());
        vendorCommunicationRecorder = mock(VendorCommunicationRecorder.class);
        VendorCommunicationPayloadGenerator payloadGenerator = new VendorCommunicationPayloadGenerator(objectMapper);
        GiftishowOperationTargetTypeResolver resolver = new GiftishowOperationTargetTypeResolver();

        giftishowApiClient = new GiftishowApiClient(
                properties, builder.build(), objectMapper, externalCallLogger,
                vendorCommunicationRecorder, payloadGenerator, resolver
        );
    }

    @Nested
    @DisplayName("Non-sensitive path — REQUEST/RESPONSE 2건 record")
    class NonSensitivePathRecordTest {

        @Test
        @DisplayName("getProductList 정상 호출 시 REQUEST/RESPONSE 2건이 COUPON targetType으로 record된다")
        void shouldRecordRequestAndResponseForProductList() {
            mockServer.expect(requestTo(BASE_URL + "/bizApi/goods"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(
                            "{\"code\":\"0000\",\"message\":\"success\",\"result\":{\"list_total_cnt\":0,\"goodsList\":[]}}",
                            MediaType.APPLICATION_JSON
                    ));

            giftishowApiClient.getProductList(0, 20);

            // REQUEST (8-arg overload — exception 포함)
            verify(vendorCommunicationRecorder).record(
                    eq(RewardVendorCommunicationTargetType.COUPON),
                    eq(RewardVendorCommunicationType.REQUEST),
                    eq(RewardVendorCommunicationSenderType.SERVER),
                    any(),
                    eq(RewardVendorName.GIFTISHOW),
                    any(String.class),
                    any(JsonNode.class)
            );
            // RESPONSE (8-arg overload — exception 포함)
            verify(vendorCommunicationRecorder).record(
                    eq(RewardVendorCommunicationTargetType.COUPON),
                    eq(RewardVendorCommunicationType.RESPONSE),
                    eq(RewardVendorCommunicationSenderType.VENDOR),
                    any(),
                    eq(RewardVendorName.GIFTISHOW),
                    any(String.class),
                    any(JsonNode.class),
                    any()
            );
        }

        @Test
        @DisplayName("getProductDetail 정상 호출 시 targetId=goodsCode로 record된다")
        void shouldRecordWithGoodsCodeAsTargetId() {
            mockServer.expect(requestTo(BASE_URL + "/bizApi/goods/" + GOODS_CODE))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(
                            "{\"code\":\"0000\",\"message\":\"success\",\"result\":{\"goodsCode\":\"G001\",\"goodsName\":\"테스트\",\"goodsImgB\":\"http://x\",\"brandCode\":\"B001\",\"brandName\":\"브\",\"brandIconImg\":\"http://x\",\"category1Seq\":\"1\",\"salePrice\":1000,\"realPrice\":2000,\"limitDay\":30,\"content\":\"c\",\"goodsStateCd\":\"SALE\"}}",
                            MediaType.APPLICATION_JSON
                    ));

            giftishowApiClient.getProductDetail(GOODS_CODE);

            ArgumentCaptor<String> targetIdCaptor = ArgumentCaptor.forClass(String.class);
            verify(vendorCommunicationRecorder, atLeastOnce()).record(
                    any(RewardVendorCommunicationTargetType.class),
                    any(RewardVendorCommunicationType.class),
                    any(RewardVendorCommunicationSenderType.class),
                    targetIdCaptor.capture(),
                    any(RewardVendorName.class),
                    any(String.class),
                    any(JsonNode.class)
            );
            assertThat(targetIdCaptor.getAllValues()).contains(GOODS_CODE);
        }

        @Test
        @DisplayName("getBrandDetail 정상 호출 시 targetId=brandCode로 record된다")
        void shouldRecordWithBrandCodeAsTargetId() {
            mockServer.expect(requestTo(BASE_URL + "/bizApi/brands/" + BRAND_CODE))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(
                            "{\"code\":\"0000\",\"message\":\"success\",\"result\":{\"brand_code\":\"B001\",\"brand_name\":\"브\",\"brand_icon_img\":\"http://x\",\"category1Seq\":\"1\",\"category1Name\":\"카\"}}",
                            MediaType.APPLICATION_JSON
                    ));

            giftishowApiClient.getBrandDetail(BRAND_CODE);

            ArgumentCaptor<String> targetIdCaptor = ArgumentCaptor.forClass(String.class);
            verify(vendorCommunicationRecorder, atLeastOnce()).record(
                    any(RewardVendorCommunicationTargetType.class),
                    any(RewardVendorCommunicationType.class),
                    any(RewardVendorCommunicationSenderType.class),
                    targetIdCaptor.capture(),
                    any(RewardVendorName.class),
                    any(String.class),
                    any(JsonNode.class)
            );
            assertThat(targetIdCaptor.getAllValues()).contains(BRAND_CODE);
        }

        @Test
        @DisplayName("getBrandList 정상 호출 시 REQUEST/RESPONSE record된다")
        void shouldRecordForBrandList() {
            mockServer.expect(requestTo(BASE_URL + "/bizApi/brands"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(
                            "{\"code\":\"0000\",\"message\":\"success\",\"result\":{\"list_total_cnt\":0,\"brandList\":[]}}",
                            MediaType.APPLICATION_JSON
                    ));

            giftishowApiClient.getBrandList();

            verify(vendorCommunicationRecorder, atLeastOnce()).record(
                    eq(RewardVendorCommunicationTargetType.COUPON),
                    eq(RewardVendorCommunicationType.REQUEST),
                    eq(RewardVendorCommunicationSenderType.SERVER),
                    any(),
                    eq(RewardVendorName.GIFTISHOW),
                    any(String.class),
                    any(JsonNode.class)
            );
        }

        @Test
        @DisplayName("cancelCoupon 정상 호출 시 COUPON targetType + targetId=trId로 record된다")
        void shouldRecordForCancelCouponAsCoupon() {
            mockServer.expect(requestTo(BASE_URL + "/bizApi/cancel"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(
                            "{\"code\":\"0000\",\"message\":\"success\",\"result\":{\"tr_id\":\"" + TR_ID + "\",\"order_no\":\"ORD001\"}}",
                            MediaType.APPLICATION_JSON
                    ));

            giftishowApiClient.cancelCoupon(TR_ID, USER_ID);

            ArgumentCaptor<String> targetIdCaptor = ArgumentCaptor.forClass(String.class);
            verify(vendorCommunicationRecorder, atLeastOnce()).record(
                    eq(RewardVendorCommunicationTargetType.COUPON),
                    any(RewardVendorCommunicationType.class),
                    any(RewardVendorCommunicationSenderType.class),
                    targetIdCaptor.capture(),
                    any(RewardVendorName.class),
                    any(String.class),
                    any(JsonNode.class)
            );
            assertThat(targetIdCaptor.getAllValues()).contains(TR_ID);
        }

        @Test
        @DisplayName("getBizMoneyBalance 정상 호출 시 BIZMONEY targetType으로 record된다")
        void shouldRecordForBizmoneyBalanceAsBizmoney() {
            mockServer.expect(requestTo(BASE_URL + "/bizApi/bizmoney"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(
                            "{\"code\":\"0000\",\"message\":\"success\",\"result\":{\"user_id\":\"" + USER_ID + "\",\"biz_money\":1000}}",
                            MediaType.APPLICATION_JSON
                    ));

            giftishowApiClient.getBizMoneyBalance(USER_ID);

            verify(vendorCommunicationRecorder, atLeastOnce()).record(
                    eq(RewardVendorCommunicationTargetType.BIZMONEY),
                    eq(RewardVendorCommunicationType.REQUEST),
                    eq(RewardVendorCommunicationSenderType.SERVER),
                    any(),
                    eq(RewardVendorName.GIFTISHOW),
                    any(String.class),
                    any(JsonNode.class)
            );
            verify(vendorCommunicationRecorder, atLeastOnce()).record(
                    eq(RewardVendorCommunicationTargetType.BIZMONEY),
                    eq(RewardVendorCommunicationType.RESPONSE),
                    eq(RewardVendorCommunicationSenderType.VENDOR),
                    any(),
                    eq(RewardVendorName.GIFTISHOW),
                    any(String.class),
                    any(JsonNode.class),
                    any()
            );
        }

        @Test
        @DisplayName("cancelSendFailedCoupon 정상 호출 시 COUPON targetType으로 record된다")
        void shouldRecordForCancelSendFailedCoupon() {
            mockServer.expect(requestTo(BASE_URL + "/bizApi/sendFail/cancel"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(
                            "{\"code\":\"0000\",\"message\":\"success\",\"result\":{\"tr_id\":\"" + TR_ID + "\",\"order_no\":\"ORD001\"}}",
                            MediaType.APPLICATION_JSON
                    ));

            giftishowApiClient.cancelSendFailedCoupon(TR_ID, USER_ID);

            verify(vendorCommunicationRecorder, atLeastOnce()).record(
                    eq(RewardVendorCommunicationTargetType.COUPON),
                    eq(RewardVendorCommunicationType.REQUEST),
                    eq(RewardVendorCommunicationSenderType.SERVER),
                    any(),
                    eq(RewardVendorName.GIFTISHOW),
                    any(String.class),
                    any(JsonNode.class)
            );
        }
    }

    @Nested
    @DisplayName("Sensitive path — 비식별화 토큰만 영속 (핀번호/폰번호 raw 0건)")
    class SensitivePathMaskingTest {

        @Test
        @DisplayName("sendCoupon 정상 호출 시 응답 payload에 핀번호 raw가 포함되지 않는다")
        void shouldNotPersistRawPinNumberOnSendCouponSuccess() {
            mockServer.expect(requestTo(BASE_URL + "/bizApi/send"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(
                            "{\"code\":\"0000\",\"message\":\"success\",\"result\":{\"tr_id\":\"" + TR_ID + "\",\"order_no\":\"ORD001\",\"pin_no\":\"" + PIN_NO + "\",\"coupon_img_url\":\"http://x\",\"valid_prd_end_dt\":\"20260503\"}}",
                            MediaType.APPLICATION_JSON
                    ));

            giftishowApiClient.sendCoupon(TR_ID, GOODS_CODE, PHONE_NO, USER_ID, "msg", "title", null);

            ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
            // RESPONSE (8-arg) 만 캡처 (exception 인자 포함된 오버로드)
            verify(vendorCommunicationRecorder, atLeastOnce()).record(
                    any(RewardVendorCommunicationTargetType.class),
                    eq(RewardVendorCommunicationType.RESPONSE),
                    any(RewardVendorCommunicationSenderType.class),
                    any(),
                    any(RewardVendorName.class),
                    payloadCaptor.capture(),
                    any(JsonNode.class),
                    any()
            );

            for (String payload : payloadCaptor.getAllValues()) {
                assertThat(payload)
                        .as("응답 payload에 핀번호 raw가 포함되면 안 됨")
                        .doesNotContain(PIN_NO);
            }
        }

        @Test
        @DisplayName("sendCoupon 호출 시 REQUEST payload에 폰번호 raw가 포함되지 않는다")
        void shouldNotPersistRawPhoneNumberOnSendCouponRequest() {
            mockServer.expect(requestTo(BASE_URL + "/bizApi/send"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(
                            "{\"code\":\"0000\",\"message\":\"success\",\"result\":{\"tr_id\":\"" + TR_ID + "\",\"order_no\":\"ORD001\",\"pin_no\":\"" + PIN_NO + "\",\"coupon_img_url\":\"http://x\",\"valid_prd_end_dt\":\"20260503\"}}",
                            MediaType.APPLICATION_JSON
                    ));

            giftishowApiClient.sendCoupon(TR_ID, GOODS_CODE, PHONE_NO, USER_ID, "msg", "title", null);

            ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
            verify(vendorCommunicationRecorder, atLeastOnce()).record(
                    any(RewardVendorCommunicationTargetType.class),
                    eq(RewardVendorCommunicationType.REQUEST),
                    any(RewardVendorCommunicationSenderType.class),
                    any(),
                    any(RewardVendorName.class),
                    payloadCaptor.capture(),
                    any(JsonNode.class)
            );

            for (String payload : payloadCaptor.getAllValues()) {
                assertThat(payload)
                        .as("REQUEST payload에 폰번호 raw가 포함되면 안 됨")
                        .doesNotContain(PHONE_NO);
            }
        }

        @Test
        @DisplayName("sendCoupon 정상 응답 시 비식별화 토큰(masked=true)만 응답 payload에 포함된다")
        void shouldPersistMaskedTokenOnly() {
            mockServer.expect(requestTo(BASE_URL + "/bizApi/send"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(
                            "{\"code\":\"0000\",\"message\":\"success\",\"result\":{\"tr_id\":\"" + TR_ID + "\",\"order_no\":\"ORD001\",\"pin_no\":\"" + PIN_NO + "\",\"coupon_img_url\":\"http://x\",\"valid_prd_end_dt\":\"20260503\"}}",
                            MediaType.APPLICATION_JSON
                    ));

            giftishowApiClient.sendCoupon(TR_ID, GOODS_CODE, PHONE_NO, USER_ID, "msg", "title", null);

            ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
            verify(vendorCommunicationRecorder, atLeastOnce()).record(
                    any(RewardVendorCommunicationTargetType.class),
                    eq(RewardVendorCommunicationType.RESPONSE),
                    any(RewardVendorCommunicationSenderType.class),
                    any(),
                    any(RewardVendorName.class),
                    payloadCaptor.capture(),
                    any(JsonNode.class),
                    any()
            );

            assertThat(payloadCaptor.getValue())
                    .contains("\"masked\":true")
                    .contains("\"operation\":\"coupon.send\"");
        }

        @Test
        @DisplayName("getCouponDetail 정상 응답 시 응답 payload에 핀번호/식별 정보가 포함되지 않는다")
        void shouldNotPersistRawSensitiveDataOnGetCouponDetail() {
            mockServer.expect(requestTo(BASE_URL + "/bizApi/coupons"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(
                            "{\"code\":\"0000\",\"message\":\"success\",\"result\":[{\"couponInfoList\":[{\"trId\":\"" + TR_ID + "\",\"orderNo\":\"ORD001\",\"pinStatusCd\":\"01\",\"pinStatusNm\":\"발행\",\"validPrdEndDt\":\"20260503\",\"goodsCd\":\"G001\",\"goodsNm\":\"테스트\"}]}]}",
                            MediaType.APPLICATION_JSON
                    ));

            giftishowApiClient.getCouponDetail(TR_ID);

            ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
            verify(vendorCommunicationRecorder, atLeastOnce()).record(
                    any(RewardVendorCommunicationTargetType.class),
                    eq(RewardVendorCommunicationType.RESPONSE),
                    any(RewardVendorCommunicationSenderType.class),
                    any(),
                    any(RewardVendorName.class),
                    payloadCaptor.capture(),
                    any(JsonNode.class),
                    any()
            );

            // 식별 정보(테스트 상품명) 포함 안 됨 확인
            assertThat(payloadCaptor.getValue())
                    .as("sensitive path 응답은 비식별화 토큰만")
                    .contains("\"masked\":true")
                    .doesNotContain("테스트");
        }

        @Test
        @DisplayName("resendCoupon 정상 응답 시 응답 payload에 폰번호/식별 정보가 포함되지 않는다")
        void shouldNotPersistRawSensitiveDataOnResendCoupon() {
            mockServer.expect(requestTo(BASE_URL + "/bizApi/resend"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(
                            "{\"code\":\"0000\",\"message\":\"success\",\"result\":{\"tr_id\":\"" + TR_ID + "\",\"order_no\":\"ORD001\"}}",
                            MediaType.APPLICATION_JSON
                    ));

            giftishowApiClient.resendCoupon(TR_ID, PHONE_NO);

            ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
            verify(vendorCommunicationRecorder, atLeastOnce()).record(
                    any(RewardVendorCommunicationTargetType.class),
                    eq(RewardVendorCommunicationType.RESPONSE),
                    any(RewardVendorCommunicationSenderType.class),
                    any(),
                    any(RewardVendorName.class),
                    payloadCaptor.capture(),
                    any(JsonNode.class),
                    any()
            );

            assertThat(payloadCaptor.getValue())
                    .contains("\"masked\":true")
                    .doesNotContain(PHONE_NO);
        }
    }

    @Nested
    @DisplayName("실패 시에도 응답 record (silent fail 금지)")
    class FailureRecordTest {

        @Test
        @DisplayName("HTTP 5xx 실패 시 REQUEST + 실패 RESPONSE 모두 record된다")
        void shouldRecordRequestAndFailureResponseOnHttp5xx() {
            mockServer.expect(requestTo(BASE_URL + "/bizApi/goods"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

            assertThatThrownBy(() -> giftishowApiClient.getProductList(0, 20))
                    .isInstanceOf(GiftishowCommunicationException.class);

            // REQUEST (7-arg)
            verify(vendorCommunicationRecorder).record(
                    eq(RewardVendorCommunicationTargetType.COUPON),
                    eq(RewardVendorCommunicationType.REQUEST),
                    eq(RewardVendorCommunicationSenderType.SERVER),
                    any(),
                    eq(RewardVendorName.GIFTISHOW),
                    any(String.class),
                    any(JsonNode.class)
            );
            // RESPONSE (8-arg, exception)
            verify(vendorCommunicationRecorder).record(
                    eq(RewardVendorCommunicationTargetType.COUPON),
                    eq(RewardVendorCommunicationType.RESPONSE),
                    eq(RewardVendorCommunicationSenderType.VENDOR),
                    any(),
                    eq(RewardVendorName.GIFTISHOW),
                    any(String.class),
                    any(JsonNode.class),
                    any()
            );
        }

        @Test
        @DisplayName("sendCoupon HTTP 실패 시에도 응답 payload에 핀번호 raw가 포함되지 않는다")
        void shouldNotLeakSensitiveDataOnSendCouponFailure() {
            mockServer.expect(requestTo(BASE_URL + "/bizApi/send"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("{\"pin_no\":\"" + PIN_NO + "\"}")
                            .contentType(MediaType.APPLICATION_JSON));

            assertThatThrownBy(() -> giftishowApiClient.sendCoupon(
                    TR_ID, GOODS_CODE, PHONE_NO, USER_ID, "msg", "title", null
            )).isInstanceOf(GiftishowCommunicationException.class);

            ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
            verify(vendorCommunicationRecorder, atLeastOnce()).record(
                    any(RewardVendorCommunicationTargetType.class),
                    any(RewardVendorCommunicationType.class),
                    any(RewardVendorCommunicationSenderType.class),
                    any(),
                    any(RewardVendorName.class),
                    payloadCaptor.capture(),
                    any(JsonNode.class),
                    any()
            );

            for (String payload : payloadCaptor.getAllValues()) {
                assertThat(payload)
                        .as("실패 시에도 핀번호 raw 영속 0건")
                        .doesNotContain(PIN_NO);
            }
            for (String payload : payloadCaptor.getAllValues()) {
                assertThat(payload)
                        .as("실패 시에도 폰번호 raw 영속 0건")
                        .doesNotContain(PHONE_NO);
            }
        }
    }

    @Nested
    @DisplayName("record 실패는 외부 호출 흐름에 영향 없음 (try-catch 격리)")
    class RecorderFailureIsolationTest {

        @Test
        @DisplayName("vendorCommunicationRecorder.record가 예외를 던져도 외부 호출 결과는 정상 반환된다")
        void shouldNotPropagateRecorderFailure() {
            mockServer.expect(requestTo(BASE_URL + "/bizApi/goods"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(
                            "{\"code\":\"0000\",\"message\":\"success\",\"result\":{\"list_total_cnt\":0,\"goodsList\":[]}}",
                            MediaType.APPLICATION_JSON
                    ));

            org.mockito.Mockito.doThrow(new RuntimeException("recorder DB down"))
                    .when(vendorCommunicationRecorder)
                    .record(
                            any(RewardVendorCommunicationTargetType.class),
                            any(RewardVendorCommunicationType.class),
                            any(RewardVendorCommunicationSenderType.class),
                            any(),
                            any(RewardVendorName.class),
                            any(String.class),
                            any(JsonNode.class)
                    );

            // 외부 호출은 정상 반환되어야 함
            assertThat(giftishowApiClient.getProductList(0, 20).isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("vendorName은 항상 GIFTISHOW")
    class VendorNameTest {

        @Test
        @DisplayName("getProductList 호출 시 vendorName=GIFTISHOW로 REQUEST/RESPONSE 각각 record된다")
        void shouldRecordVendorNameAsGiftishow() {
            mockServer.expect(requestTo(BASE_URL + "/bizApi/goods"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(
                            "{\"code\":\"0000\",\"message\":\"success\",\"result\":{\"list_total_cnt\":0,\"goodsList\":[]}}",
                            MediaType.APPLICATION_JSON
                    ));

            giftishowApiClient.getProductList(0, 20);

            // REQUEST (7-arg)
            verify(vendorCommunicationRecorder, times(1)).record(
                    any(RewardVendorCommunicationTargetType.class),
                    eq(RewardVendorCommunicationType.REQUEST),
                    any(RewardVendorCommunicationSenderType.class),
                    any(),
                    eq(RewardVendorName.GIFTISHOW),
                    any(String.class),
                    any(JsonNode.class)
            );
            // RESPONSE (8-arg)
            verify(vendorCommunicationRecorder, times(1)).record(
                    any(RewardVendorCommunicationTargetType.class),
                    eq(RewardVendorCommunicationType.RESPONSE),
                    any(RewardVendorCommunicationSenderType.class),
                    any(),
                    eq(RewardVendorName.GIFTISHOW),
                    any(String.class),
                    any(JsonNode.class),
                    any()
            );
            verify(vendorCommunicationRecorder, never()).record(
                    any(RewardVendorCommunicationTargetType.class),
                    any(RewardVendorCommunicationType.class),
                    any(RewardVendorCommunicationSenderType.class),
                    any(),
                    eq(RewardVendorName.ADPOPCORN),
                    any(String.class),
                    any(JsonNode.class),
                    any()
            );
        }
    }
}
