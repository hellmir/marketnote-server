package com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

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
                    "listNum": 1,
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

            assertThat(response.listNum()).isEqualTo(1);
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
                    "listNum": 1,
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

            assertThat(response.listNum()).isEqualTo(1);
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
                    "goodsDetail": {
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
                }
                """;

            GiftishowProductDetailResponse response = objectMapper.readValue(json, GiftishowProductDetailResponse.class);

            GiftishowProductDetailResponse.GoodsDetail detail = response.goodsDetail();
            assertThat(detail.goodsCode()).isEqualTo("G001");
            assertThat(detail.goodsName()).isEqualTo("테스트 상품");
            assertThat(detail.goodsImgB()).isEqualTo("http://img.test.com/goods.jpg");
            assertThat(detail.brandCode()).isEqualTo("B001");
            assertThat(detail.brandName()).isEqualTo("스타벅스");
            assertThat(detail.brandIconImg()).isEqualTo("http://img.test.com/brand.jpg");
            assertThat(detail.category1Seq()).isEqualTo("1");
            assertThat(detail.salePrice()).isEqualTo(10000L);
            assertThat(detail.realPrice()).isEqualTo(12000L);
            assertThat(detail.limitDay()).isEqualTo(30);
            assertThat(detail.content()).isEqualTo("상품 설명");
            assertThat(detail.goodsStateCd()).isEqualTo("SALE");
        }

        @Test
        @DisplayName("브랜드 상세 응답(camelCase)을 정상 파싱한다")
        void shouldParseBrandDetailResponseWithCamelCase() throws Exception {
            String json = """
                {
                    "brandDetail": {
                        "brandCode": "B001",
                        "brandName": "스타벅스",
                        "brandIconImg": "http://img.test.com/brand.jpg",
                        "category1Seq": "1",
                        "category1Name": "카페/음료"
                    }
                }
                """;

            GiftishowBrandDetailResponse response = objectMapper.readValue(json, GiftishowBrandDetailResponse.class);

            GiftishowBrandDetailResponse.BrandDetail detail = response.brandDetail();
            assertThat(detail.brandCode()).isEqualTo("B001");
            assertThat(detail.brandName()).isEqualTo("스타벅스");
            assertThat(detail.brandIconImg()).isEqualTo("http://img.test.com/brand.jpg");
            assertThat(detail.category1Seq()).isEqualTo("1");
            assertThat(detail.category1Name()).isEqualTo("카페/음료");
        }

        @Test
        @DisplayName("쿠폰 상세 응답(camelCase)을 정상 파싱한다")
        void shouldParseCouponDetailResponseWithCamelCase() throws Exception {
            String json = """
                {
                    "couponInfoList": [
                        {
                            "trId": "NTCASH_1_20260427000000",
                            "orderNo": "ORD001",
                            "pinStatusCd": "01",
                            "pinStatusNm": "발행",
                            "validPrdEndDt": "20260527",
                            "goodsCd": "G001",
                            "goodsNm": "테스트 상품"
                        }
                    ]
                }
                """;

            GiftishowCouponDetailResponse response = objectMapper.readValue(json, GiftishowCouponDetailResponse.class);

            assertThat(response.couponInfoList()).hasSize(1);
            GiftishowCouponDetailResponse.CouponInfo info = response.couponInfoList().getFirst();
            assertThat(info.trId()).isEqualTo("NTCASH_1_20260427000000");
            assertThat(info.orderNo()).isEqualTo("ORD001");
            assertThat(info.pinStatusCd()).isEqualTo("01");
            assertThat(info.pinStatusNm()).isEqualTo("발행");
            assertThat(info.validPrdEndDt()).isEqualTo("20260527");
            assertThat(info.goodsCd()).isEqualTo("G001");
            assertThat(info.goodsNm()).isEqualTo("테스트 상품");
        }

        @Test
        @DisplayName("쿠폰 발송 응답(gubun=I, couponImgUrl 포함)을 정상 파싱한다")
        void shouldParseCouponSendResponseWithCouponImgUrl() throws Exception {
            String json = """
                {
                    "code": "0000",
                    "message": "success",
                    "result": {
                        "orderNo": "ORD001",
                        "pinNo": "1234-5678-9012",
                        "couponImgUrl": "http://img.test.com/coupon.jpg"
                    }
                }
                """;

            GiftishowCouponSendResponse response = objectMapper.readValue(json, GiftishowCouponSendResponse.class);

            assertThat(response.code()).isEqualTo("0000");
            assertThat(response.message()).isEqualTo("success");
            assertThat(response.result()).isNotNull();
            assertThat(response.result().orderNo()).isEqualTo("ORD001");
            assertThat(response.result().pinNo()).isEqualTo("1234-5678-9012");
            assertThat(response.result().couponImgUrl()).isEqualTo("http://img.test.com/coupon.jpg");
        }

        @Test
        @DisplayName("쿠폰 재전송 응답(빈 record)을 정상 파싱한다")
        void shouldParseCouponResendResponseWithCamelCase() throws Exception {
            String json = """
                {
                    "trId": "NTCASH_1_20260427000000",
                    "orderNo": "ORD001"
                }
                """;

            // 빈 record + @JsonIgnoreProperties(ignoreUnknown=true)로 파싱 성공 (필드 없음)
            GiftishowCouponResendResponse response = objectMapper.readValue(json, GiftishowCouponResendResponse.class);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("쿠폰 취소 응답(빈 record)을 정상 파싱한다")
        void shouldParseCouponCancelResponseWithCamelCase() throws Exception {
            String json = """
                {
                    "trId": "NTCASH_1_20260427000000",
                    "orderNo": "ORD001"
                }
                """;

            // 빈 record + @JsonIgnoreProperties(ignoreUnknown=true)로 파싱 성공 (필드 없음)
            GiftishowCouponCancelResponse response = objectMapper.readValue(json, GiftishowCouponCancelResponse.class);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("비즈머니 잔액 응답(camelCase)을 정상 파싱한다")
        void shouldParseBizMoneyResponseWithCamelCase() throws Exception {
            String json = """
                {
                    "code": "0000",
                    "message": "success",
                    "balance": 500000
                }
                """;

            GiftishowBizMoneyResponse response = objectMapper.readValue(json, GiftishowBizMoneyResponse.class);

            assertThat(response.code()).isEqualTo("0000");
            assertThat(response.message()).isEqualTo("success");
            assertThat(response.balance()).isEqualTo(500000L);
        }

        @Test
        @DisplayName("최외곽 응답 envelope(GiftishowApiResponse) + 상품 리스트 result를 정상 파싱한다")
        void shouldParseGiftishowApiResponseEnvelope() throws Exception {
            String json = """
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
                new TypeReference<GiftishowApiResponse<GiftishowProductListResponse>>() {
                }
            );

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.code()).isEqualTo("0000");
            assertThat(response.message()).isEqualTo("success");
            assertThat(response.result().listNum()).isEqualTo(1);
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

            assertThat(response.listNum()).isZero();
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

            assertThat(response.listNum()).isZero();
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
                    "code": "0000",
                    "message": "success",
                    "result": {
                        "order_no": "ORD001",
                        "pin_no": "1234",
                        "coupon_img_url": "http://x"
                    }
                }
                """;

            GiftishowCouponSendResponse response = objectMapper.readValue(json, GiftishowCouponSendResponse.class);

            assertThat(response.code()).isEqualTo("0000");
            assertThat(response.result()).isNotNull();
            assertThat(response.result().orderNo()).isNull();
            assertThat(response.result().pinNo()).isNull();
            assertThat(response.result().couponImgUrl()).isNull();
        }

        @Test
        @DisplayName("비즈머니 응답이 snake_case로 들어오면 camelCase 필드는 null/0이다")
        void shouldNotMapSnakeCaseKeysForBizMoney() throws Exception {
            String json = """
                {
                    "code": "0000",
                    "message": "success",
                    "biz_money": 500000
                }
                """;

            GiftishowBizMoneyResponse response = objectMapper.readValue(json, GiftishowBizMoneyResponse.class);

            assertThat(response.balance()).isZero();
        }
    }

    @Nested
    @DisplayName("totalCount 필드명(listNum) 회귀 가드 (#5191/#5192 — 벤더 규격서 v1.04 P.10-11/P.17-18 정합)")
    class ListNumFieldRegressionGuardTest {

        @Test
        @DisplayName("상품 리스트 응답의 listNum 키는 GiftishowProductListResponse.listNum으로 정상 파싱된다")
        void shouldMapListNumKeyForProductList() throws Exception {
            String json = """
                {
                    "listNum": 100,
                    "goodsList": []
                }
                """;

            GiftishowProductListResponse response = objectMapper.readValue(json, GiftishowProductListResponse.class);

            assertThat(response.listNum()).isEqualTo(100);
            assertThat(response.goodsList()).isEmpty();
        }

        @Test
        @DisplayName("브랜드 리스트 응답의 listNum 키는 GiftishowBrandListResponse.listNum으로 정상 파싱된다")
        void shouldMapListNumKeyForBrandList() throws Exception {
            String json = """
                {
                    "listNum": 50,
                    "brandList": []
                }
                """;

            GiftishowBrandListResponse response = objectMapper.readValue(json, GiftishowBrandListResponse.class);

            assertThat(response.listNum()).isEqualTo(50);
            assertThat(response.brandList()).isEmpty();
        }

        @Test
        @DisplayName("상품 리스트 응답이 legacy listTotalCnt 키로 들어오면 listNum은 0이다 (#5168 회귀 가드)")
        void shouldNotMapLegacyListTotalCntKeyForProductList() throws Exception {
            String json = """
                {
                    "listTotalCnt": 100,
                    "goodsList": []
                }
                """;

            GiftishowProductListResponse response = objectMapper.readValue(json, GiftishowProductListResponse.class);

            assertThat(response.listNum()).isZero();
        }

        @Test
        @DisplayName("브랜드 리스트 응답이 legacy listTotalCnt 키로 들어오면 listNum은 0이다 (#5168 회귀 가드)")
        void shouldNotMapLegacyListTotalCntKeyForBrandList() throws Exception {
            String json = """
                {
                    "listTotalCnt": 50,
                    "brandList": []
                }
                """;

            GiftishowBrandListResponse response = objectMapper.readValue(json, GiftishowBrandListResponse.class);

            assertThat(response.listNum()).isZero();
        }
    }

    @Nested
    @DisplayName("응답 구조 매핑 라운드트립 (#5195/#5196 — 벤더 규격서 v1.04 정합)")
    class StructureMappingRoundTripTest {

        @Test
        @DisplayName("ProductDetail: result.goodsDetail.{...} 2중 wrapper 구조를 정상 파싱한다")
        void shouldParseProductDetailWithGoodsDetailWrapper() throws Exception {
            String json = """
                {
                    "code": "0000",
                    "message": "success",
                    "result": {
                        "goodsDetail": {
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
                    }
                }
                """;

            GiftishowApiResponse<GiftishowProductDetailResponse> response = objectMapper.readValue(
                json,
                new TypeReference<GiftishowApiResponse<GiftishowProductDetailResponse>>() {
                }
            );

            assertThat(response.isSuccess()).isTrue();
            GiftishowProductDetailResponse.GoodsDetail detail = response.result().goodsDetail();
            assertThat(detail).isNotNull();
            assertThat(detail.goodsCode()).isEqualTo("G001");
            assertThat(detail.category1Seq()).isEqualTo("1");
            assertThat(detail.limitDay()).isEqualTo(30);
            assertThat(detail.goodsStateCd()).isEqualTo("SALE");
        }

        @Test
        @DisplayName("BrandDetail: result.brandDetail.{...} 2중 wrapper 구조를 정상 파싱한다")
        void shouldParseBrandDetailWithBrandDetailWrapper() throws Exception {
            String json = """
                {
                    "code": "0000",
                    "message": "success",
                    "result": {
                        "brandDetail": {
                            "brandCode": "B001",
                            "brandName": "스타벅스",
                            "brandIconImg": "http://img.test.com/brand.jpg",
                            "category1Seq": "1",
                            "category1Name": "카페/음료"
                        }
                    }
                }
                """;

            GiftishowApiResponse<GiftishowBrandDetailResponse> response = objectMapper.readValue(
                json,
                new TypeReference<GiftishowApiResponse<GiftishowBrandDetailResponse>>() {
                }
            );

            assertThat(response.isSuccess()).isTrue();
            GiftishowBrandDetailResponse.BrandDetail detail = response.result().brandDetail();
            assertThat(detail).isNotNull();
            assertThat(detail.brandCode()).isEqualTo("B001");
            assertThat(detail.category1Name()).isEqualTo("카페/음료");
        }

        @Test
        @DisplayName("CouponSend (gubun=N MMS): 2중 wrapper 외곽 result.code/message + 내부 result.orderNo 추출")
        void shouldParseCouponSendGubunNWithDoubleWrapper() throws Exception {
            String json = """
                {
                    "code": "0000",
                    "message": "success",
                    "result": {
                        "code": "0000",
                        "message": "success",
                        "result": {
                            "orderNo": "ORD001"
                        }
                    }
                }
                """;

            GiftishowApiResponse<GiftishowCouponSendResponse> response = objectMapper.readValue(
                json,
                new TypeReference<GiftishowApiResponse<GiftishowCouponSendResponse>>() {
                }
            );

            assertThat(response.isSuccess()).isTrue();
            GiftishowCouponSendResponse outer = response.result();
            assertThat(outer.code()).isEqualTo("0000");
            assertThat(outer.message()).isEqualTo("success");
            GiftishowCouponSendResponse.InnerResult inner = outer.result();
            assertThat(inner).isNotNull();
            assertThat(inner.orderNo()).isEqualTo("ORD001");
            assertThat(inner.pinNo()).isNull();
            assertThat(inner.couponImgUrl()).isNull();
        }

        @Test
        @DisplayName("CouponSend (gubun=Y 핀번호): 내부 result에서 orderNo + pinNo 추출")
        void shouldParseCouponSendGubunYWithPinNo() throws Exception {
            String json = """
                {
                    "code": "0000",
                    "message": "success",
                    "result": {
                        "code": "0000",
                        "message": "success",
                        "result": {
                            "orderNo": "ORD002",
                            "pinNo": "1234-5678-9012"
                        }
                    }
                }
                """;

            GiftishowApiResponse<GiftishowCouponSendResponse> response = objectMapper.readValue(
                json,
                new TypeReference<GiftishowApiResponse<GiftishowCouponSendResponse>>() {
                }
            );

            GiftishowCouponSendResponse.InnerResult inner = response.result().result();
            assertThat(inner.orderNo()).isEqualTo("ORD002");
            assertThat(inner.pinNo()).isEqualTo("1234-5678-9012");
            assertThat(inner.couponImgUrl()).isNull();
        }

        @Test
        @DisplayName("CouponSend (gubun=I 바코드이미지): 내부 result에서 orderNo + pinNo + couponImgUrl 추출")
        void shouldParseCouponSendGubunIWithCouponImgUrl() throws Exception {
            String json = """
                {
                    "code": "0000",
                    "message": "success",
                    "result": {
                        "code": "0000",
                        "message": "success",
                        "result": {
                            "orderNo": "ORD003",
                            "pinNo": "9999-8888-7777",
                            "couponImgUrl": "http://img.test.com/coupon.jpg"
                        }
                    }
                }
                """;

            GiftishowApiResponse<GiftishowCouponSendResponse> response = objectMapper.readValue(
                json,
                new TypeReference<GiftishowApiResponse<GiftishowCouponSendResponse>>() {
                }
            );

            GiftishowCouponSendResponse.InnerResult inner = response.result().result();
            assertThat(inner.orderNo()).isEqualTo("ORD003");
            assertThat(inner.pinNo()).isEqualTo("9999-8888-7777");
            assertThat(inner.couponImgUrl()).isEqualTo("http://img.test.com/coupon.jpg");
        }

        @Test
        @DisplayName("CouponDetail: result 배열 + 각 항목 couponInfoList 구조를 정상 파싱한다")
        void shouldParseCouponDetailWithListAndCouponInfoList() throws Exception {
            String json = """
                {
                    "code": "0000",
                    "message": "success",
                    "result": [
                        {
                            "couponInfoList": [
                                {
                                    "trId": "NTCASH_1_20260427000000",
                                    "orderNo": "ORD001",
                                    "pinStatusCd": "01",
                                    "pinStatusNm": "발행",
                                    "validPrdEndDt": "20260527",
                                    "goodsCd": "G001",
                                    "goodsNm": "테스트 상품"
                                }
                            ]
                        }
                    ]
                }
                """;

            GiftishowApiResponse<List<GiftishowCouponDetailResponse>> response = objectMapper.readValue(
                json,
                new TypeReference<GiftishowApiResponse<List<GiftishowCouponDetailResponse>>>() {
                }
            );

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.result()).hasSize(1);
            List<GiftishowCouponDetailResponse.CouponInfo> infos = response.result().getFirst().couponInfoList();
            assertThat(infos).hasSize(1);
            GiftishowCouponDetailResponse.CouponInfo info = infos.getFirst();
            assertThat(info.trId()).isEqualTo("NTCASH_1_20260427000000");
            assertThat(info.orderNo()).isEqualTo("ORD001");
            assertThat(info.pinStatusCd()).isEqualTo("01");
            assertThat(info.validPrdEndDt()).isEqualTo("20260527");
            assertThat(info.goodsCd()).isEqualTo("G001");
            assertThat(info.goodsNm()).isEqualTo("테스트 상품");
        }

        @Test
        @DisplayName("BizMoney: {code, message, balance} 최상위 평면 구조를 wrapper 미적용으로 정상 파싱한다")
        void shouldParseBizMoneyWithoutWrapper() throws Exception {
            String json = """
                {
                    "code": "0000",
                    "message": "success",
                    "balance": 500000
                }
                """;

            GiftishowBizMoneyResponse response = objectMapper.readValue(json, GiftishowBizMoneyResponse.class);

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.code()).isEqualTo("0000");
            assertThat(response.message()).isEqualTo("success");
            assertThat(response.balance()).isEqualTo(500000L);
        }
    }

    @Nested
    @DisplayName("legacy 키 회귀 가드 (#5195 — 구 매핑 미적용 검증)")
    class LegacyKeyRegressionGuardTest {

        @Test
        @DisplayName("CouponDetail은 goodsCd/goodsNm 키로만 파싱한다 (구 goodsCode/goodsName 미사용)")
        void shouldNotMapLegacyGoodsCodeKeysForCouponDetail() throws Exception {
            String json = """
                {
                    "couponInfoList": [
                        {
                            "trId": "TR001",
                            "goodsCode": "G001",
                            "goodsName": "테스트 상품"
                        }
                    ]
                }
                """;

            GiftishowCouponDetailResponse response = objectMapper.readValue(
                json, GiftishowCouponDetailResponse.class
            );

            GiftishowCouponDetailResponse.CouponInfo info = response.couponInfoList().getFirst();
            assertThat(info.trId()).isEqualTo("TR001");
            assertThat(info.goodsCd()).isNull();
            assertThat(info.goodsNm()).isNull();
        }

        @Test
        @DisplayName("BizMoney는 balance 키로만 파싱한다 (구 bizMoney 미사용)")
        void shouldNotMapLegacyBizMoneyKey() throws Exception {
            String json = """
                {
                    "code": "0000",
                    "message": "success",
                    "bizMoney": 500000
                }
                """;

            GiftishowBizMoneyResponse response = objectMapper.readValue(json, GiftishowBizMoneyResponse.class);

            assertThat(response.balance()).isZero();
        }

        @Test
        @DisplayName("ProductDetail은 category1Seq/limitDay 키로만 파싱한다 (구 categorySeq1/limitday 미사용)")
        void shouldNotMapLegacyCategoryAndLimitDayKeysForProductDetail() throws Exception {
            String json = """
                {
                    "goodsDetail": {
                        "goodsCode": "G001",
                        "categorySeq1": "1",
                        "limitday": 30
                    }
                }
                """;

            GiftishowProductDetailResponse response = objectMapper.readValue(
                json, GiftishowProductDetailResponse.class
            );

            GiftishowProductDetailResponse.GoodsDetail detail = response.goodsDetail();
            assertThat(detail.goodsCode()).isEqualTo("G001");
            assertThat(detail.category1Seq()).isNull();
            assertThat(detail.limitDay()).isZero();
        }
    }

    @Nested
    @DisplayName("category1Seq 키 회귀 가드 (#5220/#5221 — ProductDetail 매핑 정정)")
    class Category1SeqKeyRegressionGuardTest {

        @Test
        @DisplayName("ProductDetail.GoodsDetail: category1Seq 키로 정상 파싱된다")
        void shouldMapCategory1SeqKeyForProductDetail() throws Exception {
            String json = """
                {
                    "goodsDetail": {
                        "goodsCode": "G001",
                        "category1Seq": "1"
                    }
                }
                """;

            GiftishowProductDetailResponse response = objectMapper.readValue(
                json, GiftishowProductDetailResponse.class
            );

            assertThat(response.goodsDetail().category1Seq()).isEqualTo("1");
        }

        @Test
        @DisplayName("ProductDetail.GoodsDetail: legacy categorySeq1 키로 응답 시 category1Seq는 null이다 (#5195 회귀 가드)")
        void shouldNotMapLegacyCategorySeq1KeyForProductDetail() throws Exception {
            String json = """
                {
                    "goodsDetail": {
                        "goodsCode": "G001",
                        "categorySeq1": "1"
                    }
                }
                """;

            GiftishowProductDetailResponse response = objectMapper.readValue(
                json, GiftishowProductDetailResponse.class
            );

            assertThat(response.goodsDetail().category1Seq()).isNull();
        }
    }

    @Nested
    @DisplayName("limitDay 키 회귀 가드 (#5211/#5212 — 운영 실 응답 키 정합)")
    class LimitDayKeyRegressionGuardTest {

        @Test
        @DisplayName("ProductList.GiftishowProductItem: limitDay(camelCase) 키로 정상 파싱된다")
        void shouldMapLimitDayKeyForProductListItem() throws Exception {
            String json = """
                {
                    "listNum": 1,
                    "goodsList": [
                        {
                            "goodsCode": "G001",
                            "limitDay": 30
                        }
                    ]
                }
                """;

            GiftishowProductListResponse response = objectMapper.readValue(
                json, GiftishowProductListResponse.class
            );

            assertThat(response.goodsList().getFirst().limitDay()).isEqualTo(30);
        }

        @Test
        @DisplayName("ProductList.GiftishowProductItem: legacy limitday(소문자) 키로 응답 시 limitDay는 0이다 (#5195 회귀 가드)")
        void shouldNotMapLegacyLimitdayKeyForProductListItem() throws Exception {
            String json = """
                {
                    "listNum": 1,
                    "goodsList": [
                        {
                            "goodsCode": "G001",
                            "limitday": 30
                        }
                    ]
                }
                """;

            GiftishowProductListResponse response = objectMapper.readValue(
                json, GiftishowProductListResponse.class
            );

            assertThat(response.goodsList().getFirst().limitDay()).isZero();
        }

        @Test
        @DisplayName("ProductDetail.GoodsDetail: limitDay(camelCase) 키로 정상 파싱된다")
        void shouldMapLimitDayKeyForProductDetail() throws Exception {
            String json = """
                {
                    "goodsDetail": {
                        "goodsCode": "G001",
                        "limitDay": 30
                    }
                }
                """;

            GiftishowProductDetailResponse response = objectMapper.readValue(
                json, GiftishowProductDetailResponse.class
            );

            assertThat(response.goodsDetail().limitDay()).isEqualTo(30);
        }

        @Test
        @DisplayName("ProductDetail.GoodsDetail: legacy limitday(소문자) 키로 응답 시 limitDay는 0이다 (#5195 회귀 가드)")
        void shouldNotMapLegacyLimitdayKeyForProductDetail() throws Exception {
            String json = """
                {
                    "goodsDetail": {
                        "goodsCode": "G001",
                        "limitday": 30
                    }
                }
                """;

            GiftishowProductDetailResponse response = objectMapper.readValue(
                json, GiftishowProductDetailResponse.class
            );

            assertThat(response.goodsDetail().limitDay()).isZero();
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
                    "brandDetail": {
                        "brandCode": "B001",
                        "brandName": "스타벅스",
                        "brandIconImg": "http://img.test.com/brand.jpg",
                        "category1Seq": "1",
                        "category1Name": "카페/음료",
                        "unknownFieldFromVendorFutureSpec": "ignored"
                    }
                }
                """;

            GiftishowBrandDetailResponse response = objectMapper.readValue(json, GiftishowBrandDetailResponse.class);

            GiftishowBrandDetailResponse.BrandDetail detail = response.brandDetail();
            assertThat(detail.brandCode()).isEqualTo("B001");
            assertThat(detail.category1Name()).isEqualTo("카페/음료");
        }
    }
}
