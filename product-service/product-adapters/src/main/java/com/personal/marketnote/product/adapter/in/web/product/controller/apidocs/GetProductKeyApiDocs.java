package com.personal.marketnote.product.adapter.in.web.product.controller.apidocs;

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
@SecurityRequirement(name = "bearer")
@Operation(
        summary = "상품 productKey 조회",
        description = """
                작성일자: 2026-09-20

                작성자: 성효빈

                ---

                ## Description

                - 상품의 productKey(UUID)를 조회합니다.

                - 관리자 또는 해당 상품의 판매자(sellerId 일치) 본인만 조회할 수 있습니다.

                - 파일 업로드 API의 ownerKey 로 사용됩니다.

                ---

                ## Request

                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | id(path) | number | 상품 ID | Y | 10 |

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-09-20T10:05:12.123456" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "상품 productKey 조회 성공" |

                ---

                ### Response > content

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | productKey | string(uuid) | 상품 productKey | "01890d0a-1234-7000-89ab-0123456789ab" |

                ---

                ## Error

                | HTTP | 상황 |
                | --- | --- |
                | 401 | 인증되지 않은 요청 |
                | 403 | 관리자 또는 해당 상품 판매자가 아닌 경우 |
                | 404 | 상품이 존재하지 않는 경우 |
                """,
        parameters = {
                @Parameter(
                        name = "id",
                        in = ParameterIn.PATH,
                        required = true,
                        description = "상품 ID",
                        schema = @Schema(type = "number", example = "10")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "상품 productKey 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-09-20T10:05:12.123456",
                                          "content": {
                                            "productKey": "01890d0a-1234-7000-89ab-0123456789ab"
                                          },
                                          "message": "상품 productKey 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetProductKeyApiDocs {
}
