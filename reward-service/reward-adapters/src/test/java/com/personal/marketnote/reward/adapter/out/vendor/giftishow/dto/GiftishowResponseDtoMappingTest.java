package com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("기프티쇼 비즈 API 응답 DTO JSON 매핑 회귀 테스트")
class GiftishowResponseDtoMappingTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    @DisplayName("camelCase 응답 매핑 (벤더 규격서 v1.04 정합)")
    class CamelCaseMappingTest {

        @Test
        @DisplayName("브랜드 리스트 응답(camelCase)을 정상 파싱한다")
        void shouldParseBrandListResponseWithCamelCase() throws Exception {
            String json = """
                    {
                        "listTotalCnt": 1,
                        "brandList": [
                            {
                                "brandCode": "B001",
                                "brandName": "스타벅스",
                                "brandIconImg": "http://img.test.com/brand.jpg",
                                "category1Seq": "1",
                                "category1Name": "카페/음료"
                            }
                        ]
                    }
                    """;

            GiftishowBrandListResponse response = objectMapper.readValue(json, GiftishowBrandListResponse.class);

            assertThat(response.listTotalCnt()).isEqualTo(1);
            assertThat(response.brandList()).hasSize(1);
            GiftishowBrandListResponse.GiftishowBrandItem item = response.brandList().getFirst();
            assertThat(item.brandCode()).isEqualTo("B001");
            assertThat(item.brandName()).isEqualTo("스타벅스");
            assertThat(item.brandIconImg()).isEqualTo("http://img.test.com/brand.jpg");
            assertThat(item.category1Seq()).isEqualTo("1");
            assertThat(item.category1Name()).isEqualTo("카페/음료");
        }

        @Test
        @DisplayName("상품 리스트 응답(camelCase + goodsStateCd)을 정상 파싱한다")
        void shouldParseProductListResponseWithCamelCase() throws Exception {
            String json = """
                    {
                        "listTotalCnt": 1,
                        "goodsList": [
                            {
                                "goodsCode": "G001",
                                "goodsName": "테스트 상품",
                                "goodsImgB": "http://img.test.com/goods.jpg",
                                "brandCode": "B001",
                                "brandName": "스타벅스",
                                "brandIconImg": "http://img.test.com/brand.jpg",
                                "category1Seq": "1",
                                "salePrice": 10000,
                                "realPrice": 12000,
                                "limitDay": 30,
                                "content": "상품 설명",
                                "goodsStateCd": "SALE"
                            }
                        ]
                    }
                    """;

            GiftishowProductListResponse response = objectMapper.readValue(json, GiftishowProductListResponse.class);

            assertThat(response.listTotalCnt()).isEqualTo(1);
            assertThat(response.goodsList()).hasSize(1);
            GiftishowProductListResponse.GiftishowProductItem item = response.goodsList().getFirst();
            assertThat(item.goodsCode()).isEqualTo("G001");
            assertThat(item.goodsName()).isEqualTo("테스트 상품");
            assertThat(item.goodsImgB()).isEqualTo("http://img.test.com/goods.jpg");
            assertThat(item.brandCode()).isEqualTo("B001");
            assertThat(item.brandName()).isEqualTo("스타벅스");
            assertThat(item.brandIconImg()).isEqualTo("http://img.test.com/brand.jpg");
            assertThat(item.category1Seq()).isEqualTo("1");
            assertThat(item.salePrice()).isEqualTo(10000L);
            assertThat(item.realPrice()).isEqualTo(12000L);
            assertThat(item.limitDay()).isEqualTo(30);
            assertThat(item.content()).isEqualTo("상품 설명");
            assertThat(item.goodsStateCd()).isEqualTo("SALE");
        }

        @Test
        @DisplayName("상품 상세 응답(camelCase + goodsStateCd)을 정상 파싱한다")
        void shouldParseProductDetailResponseWithCamelCase() throws Exception {
            String json = """
                    {
                        "goodsCode": "G001",
                        "goodsName": "테스트 상품",
                        "goodsImgB": "http://img.test.com/goods.jpg",
                        "brandCode": "B001",
                        "brandName": "스타벅스",
                        "brandIconImg": "http://img.test.com/brand.jpg",
                        "category1Seq": "1",
                        "salePrice": 10000,
                        "realPrice": 12000,
                        "limitDay": 30,
                        "content": "상품 설명",
                        "goodsStateCd": "SALE"
                    }
                    """;

            GiftishowProductDetailResponse response = objectMapper.readValue(json, GiftishowProductDetailResponse.class);

            assertThat(response.goodsCode()).isEqualTo("G001");
            assertThat(response.goodsName()).isEqualTo("테스트 상품");
            assertThat(response.goodsImgB()).isEqualTo("http://img.test.com/goods.jpg");
            assertThat(response.brandCode()).isEqualTo("B001");
            assertThat(response.brandName()).isEqualTo("스타벅스");
            assertThat(response.brandIconImg()).isEqualTo("http://img.test.com/brand.jpg");
            assertThat(response.category1Seq()).isEqualTo("1");
            assertThat(response.salePrice()).isEqualTo(10000L);
            assertThat(response.realPrice()).isEqualTo(12000L);
            assertThat(response.limitDay()).isEqualTo(30);
            assertThat(response.content()).isEqualTo("상품 설명");
            assertThat(response.goodsStateCd()).isEqualTo("SALE");
        }

        @Test
        @DisplayName("브랜드 상세 응답(camelCase)을 정상 파싱한다")
        void shouldParseBrandDetailResponseWithCamelCase() throws Exception {
            String json = """
                    {
                        "brandCode": "B001",
                        "brandName": "스타벅스",
                        "brandIconImg": "http://img.test.com/brand.jpg",
                        "category1Seq": "1",
                        "category1Name": "카페/음료"
                    }
                    """;

            GiftishowBrandDetailResponse response = objectMapper.readValue(json, GiftishowBrandDetailResponse.class);

            assertThat(response.brandCode()).isEqualTo("B001");
            assertThat(response.brandName()).isEqualTo("스타벅스");
            assertThat(response.brandIconImg()).isEqualTo("http://img.test.com/brand.jpg");
            assertThat(response.category1Seq()).isEqualTo("1");
            assertThat(response.category1Name()).isEqualTo("카페/음료");
        }

        @Test
        @DisplayName("쿠폰 상세 응답(camelCase)을 정상 파싱한다")
        void shouldParseCouponDetailResponseWithCamelCase() throws Exception {
            String json = """
                    {
                        "trId": "NTCASH_1_20260427000000",
                        "orderNo": "ORD001",
                        "pinStatusCd": "01",
                        "pinStatusNm": "발행",
                        "validPrdEndDt": "20260527",
                        "goodsCode": "G001",
                        "goodsName": "테스트 상품"
                    }
                    """;

            GiftishowCouponDetailResponse response = objectMapper.readValue(json, GiftishowCouponDetailResponse.class);

            assertThat(response.trId()).isEqualTo("NTCASH_1_20260427000000");
            assertThat(response.orderNo()).isEqualTo("ORD001");
            assertThat(response.pinStatusCd()).isEqualTo("01");
            assertThat(response.pinStatusNm()).isEqualTo("발행");
            assertThat(response.validPrdEndDt()).isEqualTo("20260527");
            assertThat(response.goodsCode()).isEqualTo("G001");
            assertThat(response.goodsName()).isEqualTo("테스트 상품");
        }

        @Test
        @DisplayName("쿠폰 발송 응답(gubun=I, couponImgUrl 포함)을 정상 파싱한다")
        void shouldParseCouponSendResponseWithCouponImgUrl() throws Exception {
            String json = """
                    {
                        "trId": "NTCASH_1_20260427000000",
                        "orderNo": "ORD001",
                        "pinNo": "1234-5678-9012",
                        "couponImgUrl": "http://img.test.com/coupon.jpg",
                        "validPrdEndDt": "20260527"
                    }
                    """;

            GiftishowCouponSendResponse response = objectMapper.readValue(json, GiftishowCouponSendResponse.class);

            assertThat(response.trId()).isEqualTo("NTCASH_1_20260427000000");
            assertThat(response.orderNo()).isEqualTo("ORD001");
            assertThat(response.pinNo()).isEqualTo("1234-5678-9012");
            assertThat(response.couponImgUrl()).isEqualTo("http://img.test.com/coupon.jpg");
            assertThat(response.validPrdEndDt()).isEqualTo("20260527");
        }

        @Test
        @DisplayName("쿠폰 재전송 응답(camelCase)을 정상 파싱한다")
        void shouldParseCouponResendResponseWithCamelCase() throws Exception {
            String json = """
                    {
                        "trId": "NTCASH_1_20260427000000",
                        "orderNo": "ORD001"
                    }
                    """;

            GiftishowCouponResendResponse response = objectMapper.readValue(json, GiftishowCouponResendResponse.class);

            assertThat(response.trId()).isEqualTo("NTCASH_1_20260427000000");
            assertThat(response.orderNo()).isEqualTo("ORD001");
        }

        @Test
        @DisplayName("쿠폰 취소 응답(camelCase)을 정상 파싱한다")
        void shouldParseCouponCancelResponseWithCamelCase() throws Exception {
            String json = """
                    {
                        "trId": "NTCASH_1_20260427000000",
                        "orderNo": "ORD001"
                    }
                    """;

            GiftishowCouponCancelResponse response = objectMapper.readValue(json, GiftishowCouponCancelResponse.class);

            assertThat(response.trId()).isEqualTo("NTCASH_1_20260427000000");
            assertThat(response.orderNo()).isEqualTo("ORD001");
        }

        @Test
        @DisplayName("비즈머니 잔액 응답(camelCase)을 정상 파싱한다")
        void shouldParseBizMoneyResponseWithCamelCase() throws Exception {
            String json = """
                    {
                        "userId": "user1",
                        "bizMoney": 500000
                    }
                    """;

            GiftishowBizMoneyResponse response = objectMapper.readValue(json, GiftishowBizMoneyResponse.class);

            assertThat(response.userId()).isEqualTo("user1");
            assertThat(response.bizMoney()).isEqualTo(500000L);
        }

        @Test
        @DisplayName("최외곽 응답 envelope(GiftishowApiResponse) + 상품 리스트 result를 정상 파싱한다")
        void shouldParseGiftishowApiResponseEnvelope() throws Exception {
            String json = """
                    {
                        "code": "0000",
                        "message": "success",
                        "result": {
                            "listTotalCnt": 1,
                            "goodsList": [
                                {
                                    "goodsCode": "G001",
                                    "goodsName": "테스트 상품",
                                    "goodsImgB": "http://img.test.com/goods.jpg",
                                    "brandCode": "B001",
                                    "brandName": "스타벅스",
                                    "brandIconImg": "http://img.test.com/brand.jpg",
                                    "category1Seq": "1",
                                    "salePrice": 10000,
                                    "realPrice": 12000,
                                    "limitDay": 30,
                                    "content": "상품 설명",
                                    "goodsStateCd": "SALE"
                                }
                            ]
                        }
                    }
                    """;

            GiftishowApiResponse<GiftishowProductListResponse> response = objectMapper.readValue(
                    json,
                    new TypeReference<GiftishowApiResponse<GiftishowProductListResponse>>() {}
            );

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.code()).isEqualTo("0000");
            assertThat(response.message()).isEqualTo("success");
            assertThat(response.result().listTotalCnt()).isEqualTo(1);
            assertThat(response.result().goodsList().getFirst().goodsCode()).isEqualTo("G001");
        }
    }

    @Nested
    @DisplayName("snake_case 응답 회귀 가드 (legacy 매핑 미사용 검증)")
    class SnakeCaseRegressionGuardTest {

        @Test
        @DisplayName("브랜드 리스트 응답이 snake_case로 들어오면 모든 camelCase 필드는 null이다 (legacy @JsonProperty 미사용 검증)")
        void shouldNotMapSnakeCaseKeysForBrandList() throws Exception {
            String json = """
                    {
                        "list_total_cnt": 1,
                        "brandList": [
                            {
                                "brand_code": "B001",
                                "brand_name": "스타벅스",
                                "brand_icon_img": "http://img.test.com/brand.jpg"
                            }
                        ]
                    }
                    """;

            GiftishowBrandListResponse response = objectMapper.readValue(json, GiftishowBrandListResponse.class);

            assertThat(response.listTotalCnt()).isZero();
            assertThat(response.brandList()).hasSize(1);
            GiftishowBrandListResponse.GiftishowBrandItem item = response.brandList().getFirst();
            assertThat(item.brandCode()).isNull();
            assertThat(item.brandName()).isNull();
            assertThat(item.brandIconImg()).isNull();
        }

        @Test
        @DisplayName("상품 리스트 응답이 snake_case로 들어오면 모든 camelCase 필드는 null/0이다")
        void shouldNotMapSnakeCaseKeysForProductList() throws Exception {
            String json = """
                    {
                        "list_total_cnt": 1,
                        "goodsList": [
                            {
                                "goods_code": "G001",
                                "goods_name": "테스트",
                                "goods_img_B": "http://x",
                                "sale_price": 1000,
                                "real_price": 2000,
                                "goods_status": "SALE"
                            }
                        ]
                    }
                    """;

            GiftishowProductListResponse response = objectMapper.readValue(json, GiftishowProductListResponse.class);

            assertThat(response.listTotalCnt()).isZero();
            GiftishowProductListResponse.GiftishowProductItem item = response.goodsList().getFirst();
            assertThat(item.goodsCode()).isNull();
            assertThat(item.goodsName()).isNull();
            assertThat(item.goodsImgB()).isNull();
            assertThat(item.salePrice()).isZero();
            assertThat(item.realPrice()).isZero();
            assertThat(item.goodsStateCd()).isNull();
        }

        @Test
        @DisplayName("쿠폰 발송 응답이 snake_case로 들어오면 모든 camelCase 필드는 null이다")
        void shouldNotMapSnakeCaseKeysForCouponSend() throws Exception {
            String json = """
                    {
                        "tr_id": "TR001",
                        "order_no": "ORD001",
                        "pin_no": "1234",
                        "coupon_img_url": "http://x",
                        "valid_prd_end_dt": "20260527"
                    }
                    """;

            GiftishowCouponSendResponse response = objectMapper.readValue(json, GiftishowCouponSendResponse.class);

            assertThat(response.trId()).isNull();
            assertThat(response.orderNo()).isNull();
            assertThat(response.pinNo()).isNull();
            assertThat(response.couponImgUrl()).isNull();
            assertThat(response.validPrdEndDt()).isNull();
        }

        @Test
        @DisplayName("비즈머니 응답이 snake_case로 들어오면 camelCase 필드는 null/0이다")
        void shouldNotMapSnakeCaseKeysForBizMoney() throws Exception {
            String json = """
                    {
                        "user_id": "user1",
                        "biz_money": 500000
                    }
                    """;

            GiftishowBizMoneyResponse response = objectMapper.readValue(json, GiftishowBizMoneyResponse.class);

            assertThat(response.userId()).isNull();
            assertThat(response.bizMoney()).isZero();
        }
    }

    @Nested
    @DisplayName("미지 필드 무시 (@JsonIgnoreProperties(ignoreUnknown = true) 보존 검증)")
    class IgnoreUnknownPropertiesTest {

        @Test
        @DisplayName("응답에 정의되지 않은 필드가 포함되어도 파싱 실패하지 않는다")
        void shouldIgnoreUnknownFieldsForBrandDetail() throws Exception {
            String json = """
                    {
                        "brandCode": "B001",
                        "brandName": "스타벅스",
                        "brandIconImg": "http://img.test.com/brand.jpg",
                        "category1Seq": "1",
                        "category1Name": "카페/음료",
                        "unknownFieldFromVendorFutureSpec": "ignored"
                    }
                    """;

            GiftishowBrandDetailResponse response = objectMapper.readValue(json, GiftishowBrandDetailResponse.class);

            assertThat(response.brandCode()).isEqualTo("B001");
            assertThat(response.category1Name()).isEqualTo("카페/음료");
        }
    }
}
