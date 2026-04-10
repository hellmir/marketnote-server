package com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs;

import com.personal.marketnote.common.adapter.in.api.schema.StringResponseSchema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
        summary = "(관리자) 기프티콘 전체 상품 목록 조회",
        description = """
                작성일자: 2026-04-05

                작성자: 성효빈

                ---

                ## Description

                - 관리자가 기프티쇼에서 동기화된 전체 기프티콘 상품 목록을 조회합니다.

                - 오프셋 기반 페이지네이션을 사용합니다. (page: 1-based)

                ---

                ## Request

                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | page | number | 페이지 번호 (1-based) | N | 1 |
                | page-size | number | 페이지 크기 | N | 20 |
                | goods-status | string | 상품 상태 필터 (SALE/SUS) | N | SALE |
                | exposed | boolean | 노출 여부 필터 | N | true |
                | keyword | string | 상품명 검색 키워드 | N | "아메리카노" |

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-04-05T12:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "관리자 기프티콘 상품 목록 조회 성공" |

                ---

                ### Response > content > goods

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | page | number | 현재 페이지 번호 (1-based) | 1 |
                | pageSize | number | 페이지 크기 | 20 |
                | totalElements | number | 총 아이템 수 | 50 |
                | totalPages | number | 총 페이지 수 | 3 |
                | items | array | 상품 목록 | [ ... ] |

                ---

                ### Response > content > goods > items

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | goodsCode | string | 상품 코드 | "G00001" |
                | goodsName | string | 상품명 | "아메리카노" |
                | brandCode | string | 브랜드 코드 | "BR001" |
                | brandName | string | 브랜드명 | "스타벅스" |
                | categoryCode | string | 카테고리 코드 | "1" |
                | realPrice | number | 정상가 | 5000 |
                | salePrice | number | 판매가 | 4500 |
                | cashPrice | number | 현금가 | 3500 |
                | goodsStatus | string | 상품 상태 | "SALE" |
                | exposed | boolean | 노출 여부 | true |
                | orderNum | number | 노출 순서 | 1 |
                | imageUrl | string | 이미지 URL | "https://img.com/goods.png" |
                | createdAt | string(datetime) | 생성일시 | "2026-04-01T10:00:00" |
                | modifiedAt | string(datetime) | 수정일시 | "2026-04-01T10:00:00" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "page",
                        in = ParameterIn.QUERY,
                        description = "페이지 번호 (1-based, 기본값: 1)",
                        schema = @Schema(type = "number", example = "1", defaultValue = "1")
                ),
                @Parameter(
                        name = "page-size",
                        in = ParameterIn.QUERY,
                        description = "페이지 크기 (기본값: 20)",
                        schema = @Schema(type = "number", example = "20", defaultValue = "20")
                ),
                @Parameter(
                        name = "goods-status",
                        in = ParameterIn.QUERY,
                        description = "상품 상태 필터",
                        schema = @Schema(type = "string", allowableValues = {"SALE", "SUS"})
                ),
                @Parameter(
                        name = "exposed",
                        in = ParameterIn.QUERY,
                        description = "노출 여부 필터",
                        schema = @Schema(type = "boolean")
                ),
                @Parameter(
                        name = "keyword",
                        in = ParameterIn.QUERY,
                        description = "상품명 검색 키워드",
                        schema = @Schema(type = "string")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "관리자 기프티콘 상품 목록 조회 성공",
                        content = @Content(
                                schema = @Schema(implementation = StringResponseSchema.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-05T12:00:00.000",
                                          "content": {
                                            "goods": {
                                              "page": 1,
                                              "pageSize": 20,
                                              "totalElements": 50,
                                              "totalPages": 3,
                                              "items": [
                                                {
                                                  "goodsCode": "G00001",
                                                  "goodsName": "아메리카노",
                                                  "brandCode": "BR001",
                                                  "brandName": "스타벅스",
                                                  "categoryCode": "1",
                                                  "realPrice": 5000,
                                                  "salePrice": 4500,
                                                  "cashPrice": 3500,
                                                  "goodsStatus": "SALE",
                                                  "exposed": true,
                                                  "orderNum": 1,
                                                  "imageUrl": "https://img.com/goods.png",
                                                  "createdAt": "2026-04-01T10:00:00",
                                                  "modifiedAt": "2026-04-01T10:00:00"
                                                }
                                              ]
                                            }
                                          },
                                          "message": "관리자 기프티콘 상품 목록 조회 성공"
                                        }
                                        """)
                        )
                )
        })
public @interface GetAdminGifticonGoodsApiDocs {
}
