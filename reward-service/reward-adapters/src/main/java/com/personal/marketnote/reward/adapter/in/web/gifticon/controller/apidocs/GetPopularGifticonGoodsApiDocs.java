package com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs;

import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetPopularGifticonGoodsResponse;
import io.swagger.v3.oas.annotations.Operation;
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
        summary = "기프티콘 인기 상품 목록 조회",
        description = """
                작성일자: 2026-04-05

                작성자: 성효빈

                ---

                ## Description

                인기 설정된 기프티콘 상품 목록을 조회합니다. 최대 10개까지 반환합니다.
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "기프티콘 인기 상품 목록 조회 성공",
                        content = @Content(
                                schema = @Schema(implementation = GetPopularGifticonGoodsResponse.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-05T10:00:00.000000",
                                          "content": {
                                            "items": [
                                              {
                                                "goodsCode": "GD001",
                                                "goodsName": "아메리카노",
                                                "brandCode": "BR001",
                                                "brandName": "스타벅스",
                                                "brandImageUrl": "https://img.com/sb.png",
                                                "salePrice": 4500,
                                                "cashPrice": 4000,
                                                "imageUrl": "https://img.com/americano.png"
                                              }
                                            ]
                                          },
                                          "message": "기프티콘 인기 상품 목록 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetPopularGifticonGoodsApiDocs {
}
