package com.personal.marketnote.product.adapter.in.web.category.controller.apidocs;

import com.personal.marketnote.common.adapter.in.api.schema.StringResponseSchema;
import com.personal.marketnote.product.adapter.in.web.category.request.RegisterProductCategoriesRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
        summary = "(판매자/관리자) 상품 카테고리 등록/수정",
        description = """
                작성일자: 2025-12-31
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 요청 상품의 카테고리를 전부 교체합니다.
                
                - 상품 판매자 본인 또는 관리자만 가능합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | productId (path) | number | 상품 ID | Y | 1001 |
                | categoryIds | array<number> | 등록할 카테고리 IDs | Y | [1,10004,10005] |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "ERR01" / "ERR02" |
                | timestamp | string(datetime) | 응답 일시 | "2025-12-31T10:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "상품 카테고리 등록 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | productId | number | 상품 ID | 1001 |
                | categoryIds | array<number> | 최종 등록된 카테고리 ID 목록 | [1001,10004,10005] |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "productId",
                        in = ParameterIn.PATH,
                        required = true,
                        description = "상품 ID",
                        schema = @Schema(type = "number", example = "1")
                )
        },
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = RegisterProductCategoriesRequest.class),
                        examples = @ExampleObject("""
                                {
                                    "categoryIds": [1001,10004,10005]
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "OK",
                        content = @Content(
                                schema = @Schema(implementation = StringResponseSchema.class),
                                examples = {
                                        @ExampleObject(name = "성공", value = """
                                                {
                                                  "statusCode": 200,
                                                  "code": "SUC01",
                                                  "timestamp": "2025-12-31T10:00:00.000",
                                                  "content": {
                                                    "productId": 1001,
                                                    "categoryIds": [1,10004,10005]
                                                  },
                                                  "message": "상품 카테고리 등록 성공"
                                                }
                                                """)
                                }
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
                                          "timestamp": "2025-12-29T10:19:52.558748",
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
                                          "timestamp": "2025-12-29T10:19:52.558748",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "존재하지 않는 상품",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 404,
                                          "code": "NOT_FOUND",
                                          "timestamp": "2025-12-31T14:56:53.347885",
                                          "content": null,
                                          "message": "상품을 찾을 수 없습니다. 전송된 상품 ID: 55"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "상품 소유자가 아님",
                        content = @Content(
                                schema = @Schema(implementation = StringResponseSchema.class),
                                examples = @ExampleObject("""
                                        {
                                            "statusCode": 400,
                                            "code": "ERR01",
                                            "timestamp": "2025-12-31T14:51:42.27111",
                                            "content": null,
                                            "message": "관리자 또는 상품 판매자가 아닙니다. 전송된 상품 ID: 11"
                                        }
                                        """)
                        )
                )
        }
)
public @interface RegisterProductCategoriesApiDocs {
}
