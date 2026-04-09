package com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs;

import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetGifticonGoodsDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "기프티콘 상품 상세 조회",
        description = """
                작성일자: 2026-04-05

                작성자: 성효빈

                ---

                ## Description

                기프티콘 상품 상세 정보와 사용자의 캐시 잔액을 조회합니다.
                노출 설정 및 판매 중인 상품만 조회 가능합니다.
                """,
        parameters = {
                @Parameter(name = "goodsCode", description = "상품 코드", required = true, example = "GD001")
        },
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "기프티콘 상품 상세 조회 성공",
                        content = @Content(
                                schema = @Schema(implementation = GetGifticonGoodsDetailResponse.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-05T10:00:00.000000",
                                          "content": {
                                            "goodsCode": "GD001",
                                            "goodsName": "아메리카노",
                                            "brandCode": "BR001",
                                            "brandName": "스타벅스",
                                            "brandImageUrl": "https://img.com/sb.png",
                                            "categoryCode": "1",
                                            "realPrice": 5000,
                                            "salePrice": 4500,
                                            "cashPrice": 4000,
                                            "imageUrl": "https://img.com/americano.png",
                                            "description": "맛있는 아메리카노",
                                            "validDays": 30,
                                            "userCashBalance": 10000
                                          },
                                          "message": "기프티콘 상품 상세 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetGifticonGoodsDetailApiDocs {
}
