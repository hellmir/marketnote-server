package com.personal.marketnote.reward.adapter.out.vendor.giftishow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.*;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.GiftishowCommunicationException;
import com.personal.marketnote.reward.configuration.GiftishowApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
@DisplayName("GiftishowApiClient 테스트")
class GiftishowApiClientTest {

    private static final String BASE_URL = "http://localhost:9999";
    private static final String API_CODE = "test-api-code";
    private static final String AUTH_CODE = "test-auth-code";
    private static final String AUTH_TOKEN = "test-auth-token";
    private static final String DEV_YN = "Y";
    private static final String CALLBACK_NO = "0000000000";
    private static final String GUBUN = "I";

    private MockRestServiceServer mockServer;
    private GiftishowApiClient giftishowApiClient;
    private GiftishowApiProperties properties;

    @BeforeEach
    void setUp() {
        properties = new GiftishowApiProperties();
        properties.setBaseUrl(BASE_URL);
        properties.setApiCode(API_CODE);
        properties.setAuthCode(AUTH_CODE);
        properties.setAuthToken(AUTH_TOKEN);
        properties.setDevYn(DEV_YN);
        properties.setCallbackNo(CALLBACK_NO);
        properties.setGubun(GUBUN);

        GiftishowApiProperties.Api api = new GiftishowApiProperties.Api();
        api.setProductListPath("/bizApi/goods");
        api.setProductDetailPath("/bizApi/goods");
        api.setBrandListPath("/bizApi/brand");
        api.setBrandDetailPath("/bizApi/brand");
        api.setCouponSendPath("/bizApi/coupon");
        api.setCouponDetailPath("/bizApi/coupon/detail");
        api.setCouponCancelPath("/bizApi/coupon/cancel");
        api.setCouponResendPath("/bizApi/coupon/resend");
        api.setBizMoneyBalancePath("/bizApi/bizMoney");
        api.setCouponSendFailCancelPath("/bizApi/coupon/sendFailCancel");
        properties.setApi(api);

        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();

        ObjectMapper objectMapper = new ObjectMapper();
        giftishowApiClient = new GiftishowApiClient(properties, builder.build(), objectMapper);
    }

    @Nested
    @DisplayName("getProductList (0101)")
    class GetProductListTest {

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
                                    "list_total_cnt": 1,
                                    "goodsList": [
                                        {
                                            "goods_code": "G001",
                                            "goods_name": "테스트 상품",
                                            "goods_img_B": "http://img.test.com/goods.jpg",
                                            "brandCode": "B001",
                                            "brandName": "테스트 브랜드",
                                            "brandIconImg": "http://img.test.com/brand.jpg",
                                            "category1Seq": "1",
                                            "sale_price": 10000,
                                            "real_price": 12000,
                                            "limitDay": 30,
                                            "content": "테스트 상품 설명",
                                            "goods_status": "SALE"
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
            assertThat(response.result().listTotalCnt()).isEqualTo(1);
            assertThat(response.result().goodsList()).hasSize(1);

            GiftishowProductListResponse.GiftishowProductItem item =
                    response.result().goodsList().getFirst();
            assertThat(item.goodsCode()).isEqualTo("G001");
            assertThat(item.goodsName()).isEqualTo("테스트 상품");
            assertThat(item.brandCode()).isEqualTo("B001");
            assertThat(item.salePrice()).isEqualTo(10000L);
            assertThat(item.limitDay()).isEqualTo(30);

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
        @DisplayName("상품 상세 조회에 성공하면 응답을 반환한다")
        void shouldReturnProductDetailWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/goods?api_code=test-api-code&auth_code=test-auth-code&auth_token=test-auth-token&dev_yn=Y&goods_code=G001"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "goods_code": "G001",
                                    "goods_name": "테스트 상품",
                                    "goods_img_B": "http://img.test.com/goods.jpg",
                                    "brandCode": "B001",
                                    "brandName": "테스트 브랜드",
                                    "brandIconImg": "http://img.test.com/brand.jpg",
                                    "category1Seq": "1",
                                    "sale_price": 10000,
                                    "real_price": 12000,
                                    "limitDay": 30,
                                    "content": "테스트 상품 설명",
                                    "goods_status": "SALE"
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            GiftishowApiResponse<GiftishowProductDetailResponse> response =
                    giftishowApiClient.getProductDetail("G001");

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.result().goodsCode()).isEqualTo("G001");
            assertThat(response.result().goodsName()).isEqualTo("테스트 상품");
            assertThat(response.result().salePrice()).isEqualTo(10000L);

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("getBrandList (0102)")
    class GetBrandListTest {

        @Test
        @DisplayName("브랜드 조회에 성공하면 응답을 반환한다")
        void shouldReturnBrandListWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/brand"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "list_total_cnt": 1,
                                    "brandList": [
                                        {
                                            "brand_code": "B001",
                                            "brand_name": "테스트 브랜드",
                                            "brand_icon_img": "http://img.test.com/brand.jpg",
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
        @DisplayName("브랜드 상세 조회에 성공하면 응답을 반환한다")
        void shouldReturnBrandDetailWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/brand?api_code=test-api-code&auth_code=test-auth-code&auth_token=test-auth-token&dev_yn=Y&brand_code=B001"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "brand_code": "B001",
                                    "brand_name": "테스트 브랜드",
                                    "brand_icon_img": "http://img.test.com/brand.jpg",
                                    "category1Seq": "1",
                                    "category1Name": "카페/음료"
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            GiftishowApiResponse<GiftishowBrandDetailResponse> response =
                    giftishowApiClient.getBrandDetail("B001");

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.result().brandCode()).isEqualTo("B001");
            assertThat(response.result().category1Name()).isEqualTo("카페/음료");

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("sendCoupon (0204)")
    class SendCouponTest {

        @Test
        @DisplayName("쿠폰 발송에 성공하면 응답을 반환한다")
        void shouldReturnCouponSendResponseWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/coupon"))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "tr_id": "NTCASH_1_20260403120000",
                                    "order_no": "ORD001",
                                    "pin_no": "1234-5678-9012",
                                    "coupon_img_url": "http://img.test.com/coupon.jpg",
                                    "valid_prd_end_dt": "20260503"
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
                            "뉴트리캐시"
                    );

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.result().trId()).isEqualTo("NTCASH_1_20260403120000");
            assertThat(response.result().orderNo()).isEqualTo("ORD001");
            assertThat(response.result().pinNo()).isEqualTo("1234-5678-9012");
            assertThat(response.result().couponImgUrl()).isEqualTo("http://img.test.com/coupon.jpg");
            assertThat(response.result().validPrdEndDt()).isEqualTo("20260503");

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("getCouponDetail (0201)")
    class GetCouponDetailTest {

        @Test
        @DisplayName("쿠폰 상세 조회에 성공하면 응답을 반환한다")
        void shouldReturnCouponDetailWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/coupon/detail"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "tr_id": "NTCASH_1_20260403120000",
                                    "order_no": "ORD001",
                                    "pin_status_cd": "01",
                                    "pin_status_nm": "발행",
                                    "valid_prd_end_dt": "20260503",
                                    "goods_code": "G001",
                                    "goods_name": "테스트 상품"
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            GiftishowApiResponse<GiftishowCouponDetailResponse> response =
                    giftishowApiClient.getCouponDetail("NTCASH_1_20260403120000");

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.result().pinStatusCd()).isEqualTo("01");
            assertThat(response.result().pinStatusNm()).isEqualTo("발행");
            assertThat(response.result().validPrdEndDt()).isEqualTo("20260503");

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("cancelCoupon (0202)")
    class CancelCouponTest {

        @Test
        @DisplayName("쿠폰 취소에 성공하면 응답을 반환한다")
        void shouldReturnCancelResponseWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/coupon/cancel"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "tr_id": "NTCASH_1_20260403120000",
                                    "order_no": "ORD001"
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            GiftishowApiResponse<GiftishowCouponCancelResponse> response =
                    giftishowApiClient.cancelCoupon("NTCASH_1_20260403120000", "user1");

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.result().trId()).isEqualTo("NTCASH_1_20260403120000");

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("resendCoupon (0203)")
    class ResendCouponTest {

        @Test
        @DisplayName("쿠폰 재전송에 성공하면 응답을 반환한다")
        void shouldReturnResendResponseWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/coupon/resend"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "tr_id": "NTCASH_1_20260403120000",
                                    "order_no": "ORD001"
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            GiftishowApiResponse<GiftishowCouponResendResponse> response =
                    giftishowApiClient.resendCoupon("NTCASH_1_20260403120000", "01012345678");

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.result().trId()).isEqualTo("NTCASH_1_20260403120000");

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("getBizMoneyBalance (0301)")
    class GetBizMoneyBalanceTest {

        @Test
        @DisplayName("비즈머니 잔액 조회에 성공하면 응답을 반환한다")
        void shouldReturnBizMoneyBalanceWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/bizMoney"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "user_id": "user1",
                                    "biz_money": 500000
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            GiftishowApiResponse<GiftishowBizMoneyResponse> response =
                    giftishowApiClient.getBizMoneyBalance("user1");

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.result().userId()).isEqualTo("user1");
            assertThat(response.result().bizMoney()).isEqualTo(500000L);

            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("cancelSendFailedCoupon (0205)")
    class CancelSendFailedCouponTest {

        @Test
        @DisplayName("발송실패 취소에 성공하면 응답을 반환한다")
        void shouldReturnCancelResponseWhenRequestSucceeds() {
            // given
            mockServer.expect(requestTo(BASE_URL + "/bizApi/coupon/sendFailCancel"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("""
                            {
                                "code": "0000",
                                "message": "success",
                                "result": {
                                    "tr_id": "NTCASH_1_20260403120000",
                                    "order_no": "ORD001"
                                }
                            }
                            """, MediaType.APPLICATION_JSON));

            // when
            GiftishowApiResponse<GiftishowCouponCancelResponse> response =
                    giftishowApiClient.cancelSendFailedCoupon("NTCASH_1_20260403120000", "user1");

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.result().trId()).isEqualTo("NTCASH_1_20260403120000");

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
            mockServer.expect(requestTo(BASE_URL + "/bizApi/bizMoney"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess("""
                            {
                                "code": "9999",
                                "message": "인증 실패",
                                "result": {
                                    "user_id": "user1",
                                    "biz_money": 0
                                }
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
}
