package com.personal.marketnote.reward.adapter.out.vendor.giftishow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.external.log.ExternalSystem;
import com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.*;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.GiftishowCommunicationException;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.InvalidGiftishowBrandCodeException;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.InvalidGiftishowGoodsCodeException;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.InvalidGiftishowPhoneNoException;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.InvalidGiftishowTrIdException;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.InvalidGiftishowUserIdException;
import com.personal.marketnote.reward.configuration.GiftishowApiProperties;
import com.personal.marketnote.reward.utility.VendorCommunicationRecorder;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
@DisplayName("GiftishowApiClient 테스트")
class GiftishowApiClientTest {

    private static final String BASE_URL = "http://localhost:9999";
    private static final String AUTH_CODE = "test-auth-code";
    private static final String AUTH_TOKEN = "test-auth-token";
    private static final String DEV_YN = "N";
    private static final String CALLBACK_NO = "0000000000";
    private static final String GUBUN = "I";

    private MockRestServiceServer mockServer;
    private GiftishowApiClient giftishowApiClient;
    private GiftishowApiProperties properties;
    private ExternalCallLogger externalCallLogger;
    private VendorCommunicationRecorder vendorCommunicationRecorder;

    @BeforeEach
    void setUp() {
        properties = new GiftishowApiProperties();
        properties.setBaseUrl(BASE_URL);
        properties.setAuthCode(AUTH_CODE);
        properties.setAuthToken(AUTH_TOKEN);
        properties.setCallbackNo(CALLBACK_NO);
        properties.setGubun(GUBUN);

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
        externalCallLogger = spy(new ExternalCallLogger(new SimpleMeterRegistry()));
        vendorCommunicationRecorder = mock(VendorCommunicationRecorder.class);
        VendorCommunicationPayloadGenerator payloadGenerator = new VendorCommunicationPayloadGenerator(objectMapper);
        GiftishowOperationTargetTypeResolver resolver = new GiftishowOperationTargetTypeResolver();
        giftishowApiClient = new GiftishowApiClient(
                properties, builder.build(), objectMapper, externalCallLogger,
                vendorCommunicationRecorder, payloadGenerator, resolver
        );
    }

    @Nested
    @DisplayName("getProductList (0101)")
    class GetProductListTest {

        @Test
        @DisplayName("상품 리스트 조회는 api_code=0101을 송신한다")
        void shouldSendApiCode0101WhenGetProductList() {
            // given
            MultiValueMap<String, String> expectedFormData = new LinkedMultiValueMap<>();
            expectedFormData.add("custom_auth_code", AUTH_CODE);
            expectedFormData.add("custom_auth_token", AUTH_TOKEN);
            expectedFormData.add("dev_yn", DEV_YN);
            expectedFormData.add("api_code", "0101");
            expectedFormData.add("start", "0");
            expectedFormData.add("size", "20");

            mockServer.expect(requestTo(BASE_URL + "/bizApi/goods"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().formData(expectedFormData))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "listNum": 0,
                                    "goodsList": []
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            giftishowApiClient.getProductList(0, 20);

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("상품 리스트 조회에 성공하면 응답을 반환한다")
        void shouldReturnProductListWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/goods"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "listNum": 1,
                                    "goodsList": [
                                        {
                                            "goodsCode": "G001",
                                            "goodsName": "테스트 상품",
                                            "goodsImgB": "http://img.test.com/goods.jpg",
                                            "brandCode": "B001",
                                            "brandName": "테스트 브랜드",
                                            "brandIconImg": "http://img.test.com/brand.jpg",
                                            "category1Seq": "1",
                                            "salePrice": 10000,
                                            "realPrice": 12000,
                                            "limitday": 30,
                                            "content": "테스트 상품 설명",
                                            "goodsStateCd": "SALE"
                                        }
                                    ]
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            GiftishowApiResponse<GiftishowProductListResponse> response =
                    giftishowApiClient.getProductList(0, 20);

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.code()).isEqualTo("0000");
            assertThat(response.result().listNum()).isEqualTo(1);
            assertThat(response.result().goodsList()).hasSize(1);

            GiftishowProductListResponse.GiftishowProductItem item =
                    response.result().goodsList().getFirst();
            assertThat(item.goodsCode()).isEqualTo("G001");
            assertThat(item.goodsName()).isEqualTo("테스트 상품");
            assertThat(item.brandCode()).isEqualTo("B001");
            assertThat(item.salePrice()).isEqualTo(10000L);
            assertThat(item.limitday()).isEqualTo(30);

            mockServer.verify();
        }

        @Test
        @DisplayName("응답 본문이 비어 있으면 GiftishowCommunicationException이 발생한다")
        void shouldThrowExceptionWhenResponseBodyIsEmpty() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/goods"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

            // when & then
            assertThatThrownBy(() -> giftishowApiClient.getProductList(0, 20))
                    .isInstanceOf(GiftishowCommunicationException.class)
                    .hasMessageContaining("응답 본문이 비어 있습니다");

            mockServer.verify();
        }

        @Test
        @DisplayName("응답 JSON이 잘못된 형식이면 GiftishowCommunicationException이 발생한다")
        void shouldThrowExceptionWhenResponseJsonIsInvalid() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/goods"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("invalid json", MediaType.APPLICATION_JSON));

            // when & then
            assertThatThrownBy(() -> giftishowApiClient.getProductList(0, 20))
                    .isInstanceOf(GiftishowCommunicationException.class)
                    .hasMessageContaining("응답 파싱 실패");

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("getProductDetail (0111)")
    class GetProductDetailTest {

        @Test
        @DisplayName("상품 상세 조회에 성공하면 POST + URL path variable로 요청한다")
        void shouldReturnProductDetailWhenRequestSucceeds() {
            // given
            MultiValueMap<String, String> expectedFormData = new LinkedMultiValueMap<>();
            expectedFormData.add("custom_auth_code", AUTH_CODE);
            expectedFormData.add("custom_auth_token", AUTH_TOKEN);
            expectedFormData.add("dev_yn", DEV_YN);
            expectedFormData.add("api_code", "0111");

            mockServer.expect(requestTo(BASE_URL + "/bizApi/goods/G001"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().formData(expectedFormData))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "goodsDetail": {
                                        "goodsCode": "G001",
                                        "goodsName": "테스트 상품",
                                        "goodsImgB": "http://img.test.com/goods.jpg",
                                        "brandCode": "B001",
                                        "brandName": "테스트 브랜드",
                                        "brandIconImg": "http://img.test.com/brand.jpg",
                                        "categorySeq1": "1",
                                        "salePrice": 10000,
                                        "realPrice": 12000,
                                        "limitday": 30,
                                        "content": "테스트 상품 설명",
                                        "goodsStateCd": "SALE"
                                    }
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            GiftishowApiResponse<GiftishowProductDetailResponse> response =
                    giftishowApiClient.getProductDetail("G001");

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.result().goodsDetail().goodsCode()).isEqualTo("G001");
            assertThat(response.result().goodsDetail().goodsName()).isEqualTo("테스트 상품");
            assertThat(response.result().goodsDetail().salePrice()).isEqualTo(10000L);

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("getBrandList (0102)")
    class GetBrandListTest {

        @Test
        @DisplayName("브랜드 리스트 조회는 api_code=0102를 송신한다")
        void shouldSendApiCode0102WhenGetBrandList() {
            // given
            MultiValueMap<String, String> expectedFormData = new LinkedMultiValueMap<>();
            expectedFormData.add("custom_auth_code", AUTH_CODE);
            expectedFormData.add("custom_auth_token", AUTH_TOKEN);
            expectedFormData.add("dev_yn", DEV_YN);
            expectedFormData.add("api_code", "0102");

            mockServer.expect(requestTo(BASE_URL + "/bizApi/brands"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().formData(expectedFormData))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "listNum": 0,
                                    "brandList": []
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            giftishowApiClient.getBrandList();

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("브랜드 조회에 성공하면 응답을 반환한다")
        void shouldReturnBrandListWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/brands"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "listNum": 1,
                                    "brandList": [
                                        {
                                            "brandCode": "B001",
                                            "brandName": "테스트 브랜드",
                                            "brandIconImg": "http://img.test.com/brand.jpg",
                                            "category1Seq": "1",
                                            "category1Name": "카페/음료"
                                        }
                                    ]
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            GiftishowApiResponse<GiftishowBrandListResponse> response =
                    giftishowApiClient.getBrandList();

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.result().brandList()).hasSize(1);

            GiftishowBrandListResponse.GiftishowBrandItem item =
                    response.result().brandList().getFirst();
            assertThat(item.brandCode()).isEqualTo("B001");
            assertThat(item.brandName()).isEqualTo("테스트 브랜드");
            assertThat(item.category1Name()).isEqualTo("카페/음료");

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("getBrandDetail (0112)")
    class GetBrandDetailTest {

        @Test
        @DisplayName("브랜드 상세 조회에 성공하면 POST + URL path variable로 요청한다")
        void shouldReturnBrandDetailWhenRequestSucceeds() {
            // given
            MultiValueMap<String, String> expectedFormData = new LinkedMultiValueMap<>();
            expectedFormData.add("custom_auth_code", AUTH_CODE);
            expectedFormData.add("custom_auth_token", AUTH_TOKEN);
            expectedFormData.add("dev_yn", DEV_YN);
            expectedFormData.add("api_code", "0112");

            mockServer.expect(requestTo(BASE_URL + "/bizApi/brands/B001"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().formData(expectedFormData))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "brandDetail": {
                                        "brandCode": "B001",
                                        "brandName": "테스트 브랜드",
                                        "brandIconImg": "http://img.test.com/brand.jpg",
                                        "category1Seq": "1",
                                        "category1Name": "카페/음료"
                                    }
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            GiftishowApiResponse<GiftishowBrandDetailResponse> response =
                    giftishowApiClient.getBrandDetail("B001");

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.result().brandDetail().brandCode()).isEqualTo("B001");
            assertThat(response.result().brandDetail().category1Name()).isEqualTo("카페/음료");

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("sendCoupon (0204)")
    class SendCouponTest {

        @Test
        @DisplayName("쿠폰 발송은 api_code=0204를 송신한다")
        void shouldSendApiCode0204WhenSendCoupon() {
            // given
            MultiValueMap<String, String> expectedFormData = new LinkedMultiValueMap<>();
            expectedFormData.add("custom_auth_code", AUTH_CODE);
            expectedFormData.add("custom_auth_token", AUTH_TOKEN);
            expectedFormData.add("dev_yn", DEV_YN);
            expectedFormData.add("api_code", "0204");
            expectedFormData.add("tr_id", "NTCASH_1_20260403120000");
            expectedFormData.add("goods_code", "G001");
            expectedFormData.add("phone_no", "01012345678");
            expectedFormData.add("user_id", "user1");
            expectedFormData.add("gubun", GUBUN);
            expectedFormData.add("callback_no", CALLBACK_NO);
            expectedFormData.add("mms_msg", "기프티콘 발송");
            expectedFormData.add("mms_title", "마켓노트");

            mockServer.expect(requestTo(BASE_URL + "/bizApi/send"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().formData(expectedFormData))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "code": "0000",
                                    "message": "success",
                                    "result": {
                                        "orderNo": "ORD001",
                                        "pinNo": "1234-5678-9012",
                                        "couponImgUrl": "http://img.test.com/coupon.jpg"
                                    }
                                }
                            }
""", MediaType.APPLICATION_JSON));

            // when
            giftishowApiClient.sendCoupon(
                    "NTCASH_1_20260403120000",
                    "G001",
                    "01012345678",
                    "user1",
                    "기프티콘 발송",
                    "마켓노트",
                    null
            );

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("쿠폰 발송에 성공하면 응답을 반환한다")
        void shouldReturnCouponSendResponseWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/send"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "code": "0000",
                                    "message": "success",
                                    "result": {
                                        "orderNo": "ORD001",
                                        "pinNo": "1234-5678-9012",
                                        "couponImgUrl": "http://img.test.com/coupon.jpg"
                                    }
                                }
                            }
""", MediaType.APPLICATION_JSON));

            // when
            GiftishowApiResponse<GiftishowCouponSendResponse> response =
                    giftishowApiClient.sendCoupon(
                            "NTCASH_1_20260403120000",
                            "G001",
                            "01012345678",
                            "user1",
                            "기프티콘 발송",
                            "마켓노트",
                            null
                    );

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.result()).isNotNull();
            assertThat(response.result().result()).isNotNull();
            assertThat(response.result().result().orderNo()).isEqualTo("ORD001");
            assertThat(response.result().result().pinNo()).isEqualTo("1234-5678-9012");
            assertThat(response.result().result().couponImgUrl()).isEqualTo("http://img.test.com/coupon.jpg");

            mockServer.verify();
        }

        @Test
        @DisplayName("쿠폰 발송 시 properties.templateId가 설정되어 있으면 template_id 파라미터를 송신한다")
        void shouldSendTemplateIdWhenPropertiesTemplateIdIsSet() {
            // given
            properties.setTemplateId("TPL_001");

            MultiValueMap<String, String> expectedFormData = new LinkedMultiValueMap<>();
            expectedFormData.add("custom_auth_code", AUTH_CODE);
            expectedFormData.add("custom_auth_token", AUTH_TOKEN);
            expectedFormData.add("dev_yn", DEV_YN);
            expectedFormData.add("api_code", "0204");
            expectedFormData.add("tr_id", "NTCASH_1_20260403120000");
            expectedFormData.add("goods_code", "G001");
            expectedFormData.add("phone_no", "01012345678");
            expectedFormData.add("user_id", "user1");
            expectedFormData.add("gubun", GUBUN);
            expectedFormData.add("callback_no", CALLBACK_NO);
            expectedFormData.add("mms_msg", "기프티콘 발송");
            expectedFormData.add("mms_title", "마켓노트");
            expectedFormData.add("template_id", "TPL_001");

            mockServer.expect(requestTo(BASE_URL + "/bizApi/send"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().formData(expectedFormData))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "code": "0000",
                                    "message": "success",
                                    "result": {
                                        "orderNo": "ORD001",
                                        "pinNo": "1234-5678-9012",
                                        "couponImgUrl": "http://img.test.com/coupon.jpg"
                                    }
                                }
                            }
""", MediaType.APPLICATION_JSON));

            // when
            giftishowApiClient.sendCoupon(
                    "NTCASH_1_20260403120000", "G001", "01012345678",
                    "user1", "기프티콘 발송", "마켓노트", null
            );

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("쿠폰 발송 시 properties.bannerId가 설정되어 있으면 banner_id 파라미터를 송신한다")
        void shouldSendBannerIdWhenPropertiesBannerIdIsSet() {
            // given
            properties.setBannerId("BNR_001");

            MultiValueMap<String, String> expectedFormData = new LinkedMultiValueMap<>();
            expectedFormData.add("custom_auth_code", AUTH_CODE);
            expectedFormData.add("custom_auth_token", AUTH_TOKEN);
            expectedFormData.add("dev_yn", DEV_YN);
            expectedFormData.add("api_code", "0204");
            expectedFormData.add("tr_id", "NTCASH_1_20260403120000");
            expectedFormData.add("goods_code", "G001");
            expectedFormData.add("phone_no", "01012345678");
            expectedFormData.add("user_id", "user1");
            expectedFormData.add("gubun", GUBUN);
            expectedFormData.add("callback_no", CALLBACK_NO);
            expectedFormData.add("mms_msg", "기프티콘 발송");
            expectedFormData.add("mms_title", "마켓노트");
            expectedFormData.add("banner_id", "BNR_001");

            mockServer.expect(requestTo(BASE_URL + "/bizApi/send"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().formData(expectedFormData))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "code": "0000",
                                    "message": "success",
                                    "result": {
                                        "orderNo": "ORD001",
                                        "pinNo": "1234-5678-9012",
                                        "couponImgUrl": "http://img.test.com/coupon.jpg"
                                    }
                                }
                            }
""", MediaType.APPLICATION_JSON));

            // when
            giftishowApiClient.sendCoupon(
                    "NTCASH_1_20260403120000", "G001", "01012345678",
                    "user1", "기프티콘 발송", "마켓노트", null
            );

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("쿠폰 발송 시 properties.templateId/bannerId가 미설정이면 해당 파라미터를 송신하지 않고 호출에 실패하지 않는다")
        void shouldNotSendTemplateIdAndBannerIdWhenPropertiesAreNotSet() {
            // given — properties.templateId/bannerId 둘 다 미설정 (기본 setUp 그대로)
            mockServer.expect(requestTo(BASE_URL + "/bizApi/send"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().string(not(matchesRegex("(^|.*&)template_id=.*"))))
                    .andExpect(content().string(not(matchesRegex("(^|.*&)banner_id=.*"))))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "code": "0000",
                                    "message": "success",
                                    "result": {
                                        "orderNo": "ORD001",
                                        "pinNo": "1234-5678-9012",
                                        "couponImgUrl": "http://img.test.com/coupon.jpg"
                                    }
                                }
                            }
""", MediaType.APPLICATION_JSON));

            // when
            GiftishowApiResponse<GiftishowCouponSendResponse> response =
                    giftishowApiClient.sendCoupon(
                            "NTCASH_1_20260403120000", "G001", "01012345678",
                            "user1", "기프티콘 발송", "마켓노트", null
                    );

            // then
            assertThat(response.isSuccess()).isTrue();
            mockServer.verify();
        }

        @Test
        @DisplayName("쿠폰 발송 시 orderNo가 주어지면 order_no 파라미터를 송신한다")
        void shouldSendOrderNoWhenOrderNoIsProvided() {
            // given
            MultiValueMap<String, String> expectedFormData = new LinkedMultiValueMap<>();
            expectedFormData.add("custom_auth_code", AUTH_CODE);
            expectedFormData.add("custom_auth_token", AUTH_TOKEN);
            expectedFormData.add("dev_yn", DEV_YN);
            expectedFormData.add("api_code", "0204");
            expectedFormData.add("tr_id", "NTCASH_1_20260403120000");
            expectedFormData.add("goods_code", "G001");
            expectedFormData.add("phone_no", "01012345678");
            expectedFormData.add("user_id", "user1");
            expectedFormData.add("gubun", GUBUN);
            expectedFormData.add("callback_no", CALLBACK_NO);
            expectedFormData.add("mms_msg", "기프티콘 발송");
            expectedFormData.add("mms_title", "마켓노트");
            expectedFormData.add("order_no", "ORD123456");

            mockServer.expect(requestTo(BASE_URL + "/bizApi/send"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().formData(expectedFormData))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "code": "0000",
                                    "message": "success",
                                    "result": {
                                        "orderNo": "ORD123456",
                                        "pinNo": "1234-5678-9012",
                                        "couponImgUrl": "http://img.test.com/coupon.jpg"
                                    }
                                }
                            }
""", MediaType.APPLICATION_JSON));

            // when
            giftishowApiClient.sendCoupon(
                    "NTCASH_1_20260403120000", "G001", "01012345678",
                    "user1", "기프티콘 발송", "마켓노트", "ORD123456"
            );

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("쿠폰 발송 시 orderNo가 null/blank이면 order_no 파라미터를 송신하지 않는다")
        void shouldNotSendOrderNoWhenOrderNoIsNullOrBlank() {
            // given — orderNo=blank (null도 동일 분기, FormatValidator.hasValue가 둘 다 false)
            mockServer.expect(requestTo(BASE_URL + "/bizApi/send"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().string(not(matchesRegex("(^|.*&)order_no=.*"))))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "code": "0000",
                                    "message": "success",
                                    "result": {
                                        "orderNo": "ORD001",
                                        "pinNo": "1234-5678-9012",
                                        "couponImgUrl": "http://img.test.com/coupon.jpg"
                                    }
                                }
                            }
""", MediaType.APPLICATION_JSON));

            // when
            giftishowApiClient.sendCoupon(
                    "NTCASH_1_20260403120000", "G001", "01012345678",
                    "user1", "기프티콘 발송", "마켓노트", "  "
            );

            // then
            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("getCouponDetail (0201)")
    class GetCouponDetailTest {

        @Test
        @DisplayName("쿠폰 상세 조회는 api_code=0201을 송신한다")
        void shouldSendApiCode0201WhenGetCouponDetail() {
            // given
            MultiValueMap<String, String> expectedFormData = new LinkedMultiValueMap<>();
            expectedFormData.add("custom_auth_code", AUTH_CODE);
            expectedFormData.add("custom_auth_token", AUTH_TOKEN);
            expectedFormData.add("dev_yn", DEV_YN);
            expectedFormData.add("api_code", "0201");
            expectedFormData.add("tr_id", "NTCASH_1_20260403120000");

            mockServer.expect(requestTo(BASE_URL + "/bizApi/coupons"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().formData(expectedFormData))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": [
                                    {
                                        "couponInfoList": [
                                            {
                                                "trId": "NTCASH_1_20260403120000",
                                                "orderNo": "ORD001",
                                                "pinStatusCd": "01",
                                                "pinStatusNm": "발행",
                                                "validPrdEndDt": "20260503",
                                                "goodsCd": "G001",
                                                "goodsNm": "테스트 상품"
                                            }
                                        ]
                                    }
                                ]
                            }
""", MediaType.APPLICATION_JSON));

            // when
            giftishowApiClient.getCouponDetail("NTCASH_1_20260403120000");

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("쿠폰 상세 조회에 성공하면 응답을 반환한다")
        void shouldReturnCouponDetailWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/coupons"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": [
                                    {
                                        "couponInfoList": [
                                            {
                                                "trId": "NTCASH_1_20260403120000",
                                                "orderNo": "ORD001",
                                                "pinStatusCd": "01",
                                                "pinStatusNm": "발행",
                                                "validPrdEndDt": "20260503",
                                                "goodsCd": "G001",
                                                "goodsNm": "테스트 상품"
                                            }
                                        ]
                                    }
                                ]
                            }
""", MediaType.APPLICATION_JSON));

            // when
            GiftishowApiResponse<java.util.List<GiftishowCouponDetailResponse>> response =
                    giftishowApiClient.getCouponDetail("NTCASH_1_20260403120000");

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.result()).hasSize(1);
            GiftishowCouponDetailResponse first = response.result().getFirst();
            assertThat(first.couponInfoList()).hasSize(1);
            GiftishowCouponDetailResponse.CouponInfo info = first.couponInfoList().getFirst();
            assertThat(info.pinStatusCd()).isEqualTo("01");
            assertThat(info.pinStatusNm()).isEqualTo("발행");
            assertThat(info.validPrdEndDt()).isEqualTo("20260503");

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("cancelCoupon (0202)")
    class CancelCouponTest {

        @Test
        @DisplayName("쿠폰 취소는 api_code=0202를 송신한다")
        void shouldSendApiCode0202WhenCancelCoupon() {
            // given
            MultiValueMap<String, String> expectedFormData = new LinkedMultiValueMap<>();
            expectedFormData.add("custom_auth_code", AUTH_CODE);
            expectedFormData.add("custom_auth_token", AUTH_TOKEN);
            expectedFormData.add("dev_yn", DEV_YN);
            expectedFormData.add("api_code", "0202");
            expectedFormData.add("tr_id", "NTCASH_1_20260403120000");
            expectedFormData.add("user_id", "user1");

            mockServer.expect(requestTo(BASE_URL + "/bizApi/cancel"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().formData(expectedFormData))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "trId": "NTCASH_1_20260403120000",
                                    "orderNo": "ORD001"
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            giftishowApiClient.cancelCoupon("NTCASH_1_20260403120000", "user1");

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("쿠폰 취소에 성공하면 응답을 반환한다")
        void shouldReturnCancelResponseWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/cancel"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "trId": "NTCASH_1_20260403120000",
                                    "orderNo": "ORD001"
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            GiftishowApiResponse<GiftishowCouponCancelResponse> response =
                    giftishowApiClient.cancelCoupon("NTCASH_1_20260403120000", "user1");

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.result()).isNotNull();

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("resendCoupon (0203)")
    class ResendCouponTest {

        @Test
        @DisplayName("쿠폰 재전송은 api_code=0203을 송신한다")
        void shouldSendApiCode0203WhenResendCoupon() {
            // given
            MultiValueMap<String, String> expectedFormData = new LinkedMultiValueMap<>();
            expectedFormData.add("custom_auth_code", AUTH_CODE);
            expectedFormData.add("custom_auth_token", AUTH_TOKEN);
            expectedFormData.add("dev_yn", DEV_YN);
            expectedFormData.add("api_code", "0203");
            expectedFormData.add("tr_id", "NTCASH_1_20260403120000");
            expectedFormData.add("phone_no", "01012345678");

            mockServer.expect(requestTo(BASE_URL + "/bizApi/resend"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().formData(expectedFormData))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "trId": "NTCASH_1_20260403120000",
                                    "orderNo": "ORD001"
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            giftishowApiClient.resendCoupon("NTCASH_1_20260403120000", "01012345678");

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("쿠폰 재전송에 성공하면 응답을 반환한다")
        void shouldReturnResendResponseWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/resend"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "trId": "NTCASH_1_20260403120000",
                                    "orderNo": "ORD001"
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            GiftishowApiResponse<GiftishowCouponResendResponse> response =
                    giftishowApiClient.resendCoupon("NTCASH_1_20260403120000", "01012345678");

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.result()).isNotNull();

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("getBizMoneyBalance (0301)")
    class GetBizMoneyBalanceTest {

        @Test
        @DisplayName("비즈머니 잔액 조회는 api_code=0301을 송신한다")
        void shouldSendApiCode0301WhenGetBizMoneyBalance() {
            // given
            MultiValueMap<String, String> expectedFormData = new LinkedMultiValueMap<>();
            expectedFormData.add("custom_auth_code", AUTH_CODE);
            expectedFormData.add("custom_auth_token", AUTH_TOKEN);
            expectedFormData.add("dev_yn", DEV_YN);
            expectedFormData.add("api_code", "0301");
            expectedFormData.add("user_id", "user1");

            mockServer.expect(requestTo(BASE_URL + "/bizApi/bizmoney"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().formData(expectedFormData))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "balance": 500000
                            }
""", MediaType.APPLICATION_JSON));

            // when
            giftishowApiClient.getBizMoneyBalance("user1");

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("비즈머니 잔액 조회에 성공하면 응답을 반환한다")
        void shouldReturnBizMoneyBalanceWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/bizmoney"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "balance": 500000
                            }
""", MediaType.APPLICATION_JSON));

            // when
            GiftishowApiResponse<GiftishowBizMoneyResponse> response =
                    giftishowApiClient.getBizMoneyBalance("user1");

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.result()).isNotNull();
            assertThat(response.result().balance()).isEqualTo(500000L);

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("cancelSendFailedCoupon (0205)")
    class CancelSendFailedCouponTest {

        @Test
        @DisplayName("발송실패 취소는 api_code=0205를 송신한다")
        void shouldSendApiCode0205WhenCancelSendFailedCoupon() {
            // given
            MultiValueMap<String, String> expectedFormData = new LinkedMultiValueMap<>();
            expectedFormData.add("custom_auth_code", AUTH_CODE);
            expectedFormData.add("custom_auth_token", AUTH_TOKEN);
            expectedFormData.add("dev_yn", DEV_YN);
            expectedFormData.add("api_code", "0205");
            expectedFormData.add("tr_id", "NTCASH_1_20260403120000");
            expectedFormData.add("user_id", "user1");

            mockServer.expect(requestTo(BASE_URL + "/bizApi/sendFail/cancel"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().formData(expectedFormData))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "trId": "NTCASH_1_20260403120000",
                                    "orderNo": "ORD001"
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            giftishowApiClient.cancelSendFailedCoupon("NTCASH_1_20260403120000", "user1");

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("발송실패 취소에 성공하면 응답을 반환한다")
        void shouldReturnCancelResponseWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/sendFail/cancel"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "trId": "NTCASH_1_20260403120000",
                                    "orderNo": "ORD001"
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            GiftishowApiResponse<GiftishowCouponCancelResponse> response =
                    giftishowApiClient.cancelSendFailedCoupon("NTCASH_1_20260403120000", "user1");

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.result()).isNotNull();

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("public 메서드 입력 파라미터 가드")
    class InputGuardTest {

        @Test
        @DisplayName("getProductDetail에 null goodsCode를 전달하면 InvalidGiftishowGoodsCodeException이 발생한다")
        void shouldThrowWhenGetProductDetailGoodsCodeIsNull() {
            assertThatThrownBy(() -> giftishowApiClient.getProductDetail(null))
                    .isInstanceOf(InvalidGiftishowGoodsCodeException.class);
        }

        @Test
        @DisplayName("getProductDetail에 blank goodsCode를 전달하면 InvalidGiftishowGoodsCodeException이 발생한다")
        void shouldThrowWhenGetProductDetailGoodsCodeIsBlank() {
            assertThatThrownBy(() -> giftishowApiClient.getProductDetail("  "))
                    .isInstanceOf(InvalidGiftishowGoodsCodeException.class);
        }

        @Test
        @DisplayName("getBrandDetail에 null brandCode를 전달하면 InvalidGiftishowBrandCodeException이 발생한다")
        void shouldThrowWhenGetBrandDetailBrandCodeIsNull() {
            assertThatThrownBy(() -> giftishowApiClient.getBrandDetail(null))
                    .isInstanceOf(InvalidGiftishowBrandCodeException.class);
        }

        @Test
        @DisplayName("getBrandDetail에 blank brandCode를 전달하면 InvalidGiftishowBrandCodeException이 발생한다")
        void shouldThrowWhenGetBrandDetailBrandCodeIsBlank() {
            assertThatThrownBy(() -> giftishowApiClient.getBrandDetail("  "))
                    .isInstanceOf(InvalidGiftishowBrandCodeException.class);
        }

        @Test
        @DisplayName("sendCoupon에 null trId를 전달하면 InvalidGiftishowTrIdException이 발생한다")
        void shouldThrowWhenSendCouponTrIdIsNull() {
            assertThatThrownBy(() -> giftishowApiClient.sendCoupon(
                    null, "G001", "01012345678", "user1", "msg", "title", null
            )).isInstanceOf(InvalidGiftishowTrIdException.class);
        }

        @Test
        @DisplayName("sendCoupon에 null goodsCode를 전달하면 InvalidGiftishowGoodsCodeException이 발생한다")
        void shouldThrowWhenSendCouponGoodsCodeIsNull() {
            assertThatThrownBy(() -> giftishowApiClient.sendCoupon(
                    "TR001", null, "01012345678", "user1", "msg", "title", null
            )).isInstanceOf(InvalidGiftishowGoodsCodeException.class);
        }

        @Test
        @DisplayName("sendCoupon에 null phoneNo를 전달하면 InvalidGiftishowPhoneNoException이 발생한다")
        void shouldThrowWhenSendCouponPhoneNoIsNull() {
            assertThatThrownBy(() -> giftishowApiClient.sendCoupon(
                    "TR001", "G001", null, "user1", "msg", "title", null
            )).isInstanceOf(InvalidGiftishowPhoneNoException.class);
        }

        @Test
        @DisplayName("sendCoupon에 null userId를 전달하면 InvalidGiftishowUserIdException이 발생한다")
        void shouldThrowWhenSendCouponUserIdIsNull() {
            assertThatThrownBy(() -> giftishowApiClient.sendCoupon(
                    "TR001", "G001", "01012345678", null, "msg", "title", null
            )).isInstanceOf(InvalidGiftishowUserIdException.class);
        }

        @Test
        @DisplayName("getCouponDetail에 null trId를 전달하면 InvalidGiftishowTrIdException이 발생한다")
        void shouldThrowWhenGetCouponDetailTrIdIsNull() {
            assertThatThrownBy(() -> giftishowApiClient.getCouponDetail(null))
                    .isInstanceOf(InvalidGiftishowTrIdException.class);
        }

        @Test
        @DisplayName("cancelCoupon에 null trId를 전달하면 InvalidGiftishowTrIdException이 발생한다")
        void shouldThrowWhenCancelCouponTrIdIsNull() {
            assertThatThrownBy(() -> giftishowApiClient.cancelCoupon(null, "user1"))
                    .isInstanceOf(InvalidGiftishowTrIdException.class);
        }

        @Test
        @DisplayName("cancelCoupon에 null userId를 전달하면 InvalidGiftishowUserIdException이 발생한다")
        void shouldThrowWhenCancelCouponUserIdIsNull() {
            assertThatThrownBy(() -> giftishowApiClient.cancelCoupon("TR001", null))
                    .isInstanceOf(InvalidGiftishowUserIdException.class);
        }

        @Test
        @DisplayName("resendCoupon에 null trId를 전달하면 InvalidGiftishowTrIdException이 발생한다")
        void shouldThrowWhenResendCouponTrIdIsNull() {
            assertThatThrownBy(() -> giftishowApiClient.resendCoupon(null, "01012345678"))
                    .isInstanceOf(InvalidGiftishowTrIdException.class);
        }

        @Test
        @DisplayName("resendCoupon에 null phoneNo를 전달하면 InvalidGiftishowPhoneNoException이 발생한다")
        void shouldThrowWhenResendCouponPhoneNoIsNull() {
            assertThatThrownBy(() -> giftishowApiClient.resendCoupon("TR001", null))
                    .isInstanceOf(InvalidGiftishowPhoneNoException.class);
        }

        @Test
        @DisplayName("cancelSendFailedCoupon에 null trId를 전달하면 InvalidGiftishowTrIdException이 발생한다")
        void shouldThrowWhenCancelSendFailedCouponTrIdIsNull() {
            assertThatThrownBy(() -> giftishowApiClient.cancelSendFailedCoupon(null, "user1"))
                    .isInstanceOf(InvalidGiftishowTrIdException.class);
        }

        @Test
        @DisplayName("cancelSendFailedCoupon에 null userId를 전달하면 InvalidGiftishowUserIdException이 발생한다")
        void shouldThrowWhenCancelSendFailedCouponUserIdIsNull() {
            assertThatThrownBy(() -> giftishowApiClient.cancelSendFailedCoupon("TR001", null))
                    .isInstanceOf(InvalidGiftishowUserIdException.class);
        }

        @Test
        @DisplayName("getBizMoneyBalance에 null userId를 전달하면 InvalidGiftishowUserIdException이 발생한다")
        void shouldThrowWhenGetBizMoneyBalanceUserIdIsNull() {
            assertThatThrownBy(() -> giftishowApiClient.getBizMoneyBalance(null))
                    .isInstanceOf(InvalidGiftishowUserIdException.class);
        }
    }

    @Nested
    @DisplayName("ExternalCallLogger sensitive 분기")
    class ExternalCallLoggerSensitiveDispatchTest {

        @Test
        @DisplayName("sendCoupon (sensitive path /bizApi/send) 호출 시 recordSensitive로 위임된다")
        void shouldDispatchToRecordSensitiveWhenSendCoupon() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/send"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "code": "0000",
                                    "message": "success",
                                    "result": {
                                        "orderNo": "ORD001",
                                        "pinNo": "1234-5678-9012",
                                        "couponImgUrl": "http://img.test.com/coupon.jpg"
                                    }
                                }
                            }
""", MediaType.APPLICATION_JSON));

            // when
            giftishowApiClient.sendCoupon(
                    "NTCASH_1_20260403120000", "G001", "01012345678",
                    "user1", "msg", "title", null
            );

            // then
            verify(externalCallLogger).recordSensitive(
                    eq(ExternalSystem.GIFTISHOW), eq("coupon.send"), any(), any()
            );
            verify(externalCallLogger, never()).record(
                    eq(ExternalSystem.GIFTISHOW), eq("coupon.send"), any(), any()
            );
            mockServer.verify();
        }

        @Test
        @DisplayName("getCouponDetail (sensitive path /bizApi/coupons) 호출 시 recordSensitive로 위임된다")
        void shouldDispatchToRecordSensitiveWhenGetCouponDetail() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/coupons"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": [
                                    {
                                        "couponInfoList": [
                                            {
                                                "trId": "NTCASH_1_20260403120000",
                                                "orderNo": "ORD001",
                                                "pinStatusCd": "01",
                                                "pinStatusNm": "발행",
                                                "validPrdEndDt": "20260503",
                                                "goodsCd": "G001",
                                                "goodsNm": "테스트 상품"
                                            }
                                        ]
                                    }
                                ]
                            }
""", MediaType.APPLICATION_JSON));

            // when
            giftishowApiClient.getCouponDetail("NTCASH_1_20260403120000");

            // then
            verify(externalCallLogger).recordSensitive(
                    eq(ExternalSystem.GIFTISHOW), eq("coupon.detail"), any(), any()
            );
            verify(externalCallLogger, never()).record(
                    eq(ExternalSystem.GIFTISHOW), eq("coupon.detail"), any(), any()
            );
            mockServer.verify();
        }

        @Test
        @DisplayName("resendCoupon (sensitive path /bizApi/resend) 호출 시 recordSensitive로 위임된다")
        void shouldDispatchToRecordSensitiveWhenResendCoupon() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/resend"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "trId": "NTCASH_1_20260403120000",
                                    "orderNo": "ORD001"
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            giftishowApiClient.resendCoupon("NTCASH_1_20260403120000", "01012345678");

            // then
            verify(externalCallLogger).recordSensitive(
                    eq(ExternalSystem.GIFTISHOW), eq("coupon.resend"), any(), any()
            );
            verify(externalCallLogger, never()).record(
                    eq(ExternalSystem.GIFTISHOW), eq("coupon.resend"), any(), any()
            );
            mockServer.verify();
        }

        @Test
        @DisplayName("getProductList (non-sensitive path /bizApi/goods) 호출 시 record로 위임된다")
        void shouldDispatchToRecordWhenGetProductList() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/goods"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "listNum": 0,
                                    "goodsList": []
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            giftishowApiClient.getProductList(0, 20);

            // then
            verify(externalCallLogger).record(
                    eq(ExternalSystem.GIFTISHOW), eq("product.list"), any(), any()
            );
            verify(externalCallLogger, never()).recordSensitive(
                    eq(ExternalSystem.GIFTISHOW), eq("product.list"), any(), any()
            );
            mockServer.verify();
        }

        @Test
        @DisplayName("cancelCoupon (non-sensitive path /bizApi/cancel) 호출 시 record로 위임된다")
        void shouldDispatchToRecordWhenCancelCoupon() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/cancel"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "trId": "NTCASH_1_20260403120000",
                                    "orderNo": "ORD001"
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            giftishowApiClient.cancelCoupon("NTCASH_1_20260403120000", "user1");

            // then
            verify(externalCallLogger).record(
                    eq(ExternalSystem.GIFTISHOW), eq("coupon.cancel"), any(), any()
            );
            verify(externalCallLogger, never()).recordSensitive(
                    eq(ExternalSystem.GIFTISHOW), eq("coupon.cancel"), any(), any()
            );
            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("GiftishowApiResponse 공통 검증")
    class ApiResponseTest {

        @Test
        @DisplayName("응답 코드가 0000이 아니면 isSuccess는 false를 반환한다")
        void shouldReturnFalseWhenCodeIsNotSuccess() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/bizmoney"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("""
                            {
                                "code": "9999",
                                "message": "인증 실패",
                                "balance": 0
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            GiftishowApiResponse<GiftishowBizMoneyResponse> response =
                    giftishowApiClient.getBizMoneyBalance("user1");

            // then
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.code()).isEqualTo("9999");
            assertThat(response.message()).isEqualTo("인증 실패");

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("기프티쇼 비즈 API 연동 규격 회귀 가드")
    class ApiSpecComplianceRegressionTest {

        @Test
        @DisplayName("모든 호출은 custom_auth_code/custom_auth_token 파라미터를 송신한다")
        void shouldSendCustomAuthParamsOnAllCalls() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/bizmoney"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().string(containsString("custom_auth_code=" + AUTH_CODE)))
                    .andExpect(content().string(containsString("custom_auth_token=" + AUTH_TOKEN)))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "balance": 0
                            }
""", MediaType.APPLICATION_JSON));

            // when
            giftishowApiClient.getBizMoneyBalance("user1");

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("기존 auth_code/auth_token 파라미터는 송신되지 않는다 (규격 위반 회귀 가드)")
        void shouldNotSendLegacyAuthParams() {
            // given — 본문 어디에도 (^|&)auth_code= / (^|&)auth_token= 가 등장하지 않아야 함
            //         (custom_auth_code/custom_auth_token 은 토큰 boundary 가 다르므로 통과)
            mockServer.expect(requestTo(BASE_URL + "/bizApi/bizmoney"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().string(not(matchesRegex("(^|.*&)auth_code=.*"))))
                    .andExpect(content().string(not(matchesRegex("(^|.*&)auth_token=.*"))))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "balance": 0
                            }
""", MediaType.APPLICATION_JSON));

            // when
            giftishowApiClient.getBizMoneyBalance("user1");

            // then
            mockServer.verify();
        }

        @Test
        @DisplayName("dev_yn은 항상 N으로 송신된다 (운영 환경 상수)")
        void shouldAlwaysSendDevYnAsN() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/bizmoney"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(content().string(containsString("dev_yn=N")))
                    .andExpect(content().string(not(containsString("dev_yn=Y"))))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "balance": 0
                            }
""", MediaType.APPLICATION_JSON));

            // when
            giftishowApiClient.getBizMoneyBalance("user1");

            // then
            mockServer.verify();
        }
    }
}
