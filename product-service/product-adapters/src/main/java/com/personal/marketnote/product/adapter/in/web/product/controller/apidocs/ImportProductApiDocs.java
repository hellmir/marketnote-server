package com.personal.marketnote.product.adapter.in.web.product.controller.apidocs;

import com.personal.marketnote.product.adapter.in.web.product.request.ImportProductRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "(관리자) 파스토 기등록 상품 import",
        description = """
                작성일자: 2026-11-22

                작성자: 성효빈

                ---

                ## Description

                - 파스토에 이미 등록된 상품의 운영코드(cstGodCd)를 입력받아 자사 카탈로그에 등록합니다.

                - Fastto 송신을 스킵하므로 fulfillmentVendorGoods 옵션은 받지 않습니다.

                - 관리자만 가능합니다.

                ---

                ## Request

                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | sellerId | number | 판매자 회원 ID | Y | 1 |
                | name | string | 상품명 | Y | "기등록 상품1" |
                | brandName | string | 브랜드명 | N | "노트왕" |
                | detail | string | 상품 설명 | Y | "기등록 상품1 설명" |
                | price | number | 상품 기본 판매 가격(원) | Y | 100000 |
                | discountPrice | number | 상품 할인 가격(원) | Y | 90000 |
                | accumulatedPoint | number | 구매 시 적립 포인트 | Y | 1000 |
                | isFindAllOptions | boolean | 상품 목록 조회 시 옵션마다 개별 상품으로 조회 여부 | N | true |
                | tags | array<string> | 상품 태그 목록 | Y | ["루테인"] |
                | fasstoProductCode | string | 파스토 운영코드(cstGodCd) — 1~25자 영숫자 | Y | "FASSTO00000000000000001" |

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 201: 성공 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-11-22T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "파스토 기등록 상품 import 성공" |
                """, security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = ImportProductRequest.class),
                        examples = @ExampleObject("""
                                {
                                    "sellerId": 1,
                                    "name": "기등록 상품1",
                                    "brandName": "노트왕",
                                    "detail": "기등록 상품1 설명",
                                    "price": 10000,
                                    "discountPrice": 9000,
                                    "accumulatedPoint": 1000,
                                    "isFindAllOptions": true,
                                    "tags": ["루테인"],
                                    "fasstoProductCode": "FASSTO00000000000000001"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "파스토 기등록 상품 import 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 201,
                                          "code": "SUC01",
                                          "timestamp": "2026-11-22T12:12:30.013",
                                          "content": {
                                            "id": 1,
                                            "productKey": "01890d0a-1234-7000-89ab-0123456789ab"
                                          },
                                          "message": "파스토 기등록 상품 import 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "fasstoProductCode 형식 오류",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 400,
                                          "code": "BAD_REQUEST",
                                          "timestamp": "2026-11-22T12:12:30.013",
                                          "content": null,
                                          "message": "fasstoProductCode는 1-25자 영숫자만 허용됩니다."
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "토큰 인증 실패",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 401,
                                          "code": "UNAUTHORIZED",
                                          "timestamp": "2026-11-22T12:12:30.013",
                                          "content": null,
                                          "message": "Invalid token"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "403",
                        description = "토큰 인가 실패",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 403,
                                          "code": "FORBIDDEN",
                                          "timestamp": "2026-11-22T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        })
public @interface ImportProductApiDocs {
}
