package com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs;

import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetGifticonGoodsResponse;
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
        summary = "기프티콘 상품 목록 조회",
        description = """
                작성일자: 2026-10-07

                작성자: 성효빈

                ---

                ## Description

                노출 설정 및 판매 중인 기프티콘 상품 목록을 cursor 기반 무한 스크롤로 조회합니다.
                카테고리 코드, 브랜드 코드로 필터링할 수 있습니다.

                첫 페이지는 `cursor=-1`(또는 미전달)로 호출하며, 응답의 `nextCursor`를 다음 페이지 호출 시
                `cursor` 파라미터로 전달합니다. `totalElements`는 첫 페이지에서만 반환되며, `hasNext=false`이면
                마지막 페이지입니다.
                """,
        parameters = {
                @Parameter(name = "category-code", description = "카테고리 코드", required = false, example = "1"),
                @Parameter(name = "brand-code", description = "브랜드 코드", required = false, example = "BR001"),
                @Parameter(name = "cursor", description = "이전 페이지 응답의 nextCursor. 첫 조회 시 -1", required = false, example = "-1"),
                @Parameter(name = "page-size", description = "페이지 크기 (기본: 20)", required = false, example = "20")
        },
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "기프티콘 상품 목록 조회 성공",
                        content = @Content(
                                schema = @Schema(implementation = GetGifticonGoodsResponse.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-10-07T10:00:00.000000",
                                          "content": {
                                            "goods": {
                                              "totalElements": 30,
                                              "hasNext": true,
                                              "nextCursor": 20,
                                              "items": [
                                                {
                                                  "goodsCode": "GD001",
                                                  "goodsName": "아메리카노",
                                                  "brandCode": "BR001",
                                                  "brandName": "스타벅스",
                                                  "brandImageUrl": "https://img.com/sb.png",
                                                  "salePrice": 4500,
                                                  "cashPrice": 4000,
                                                  "imageUrl": "https://img.com/americano.png",
                                                  "orderNum": 1
                                                }
                                              ]
                                            }
                                          },
                                          "message": "기프티콘 상품 목록 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetGifticonGoodsApiDocs {
}
