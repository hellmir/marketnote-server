package com.personal.marketnote.product.adapter.in.web.category.controller.apidocs;

import com.personal.marketnote.common.adapter.in.api.schema.StringResponseSchema;
import com.personal.marketnote.product.adapter.in.web.category.request.RegisterCategoryRequest;
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
        summary = "(관리자) 카테고리 등록",
        description = """
                 작성일자: 2025-12-31
                
                 작성자: 성효빈
                
                 ---
                
                 ## Description
                
                 - parentCategoryId와 카테고리명을 전송해 카테고리를 등록합니다.
                
                 - parentCategoryId를 전송하지 않는 경우 최상위 카테고리로 등록됩니다. 
                
                 - 관리자만 가능합니다.
                
                 ---
                
                 ## Request
                
                 | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                 | --- | --- | --- | --- | --- |
                 | parentCategoryId | number | 상위 카테고리 ID | N | 1 |
                 | name | string | 카테고리명 | Y | "루테인" |
                
                 ---
                
                 ## Response
                
                 | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                 | --- | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 201: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                 | timestamp | string(datetime) | 응답 일시 |  | "2025-12-31T12:00:00.000" |
                 | content | object | 응답 본문 |  | { ... } |
                 | message | string | 처리 결과 |  | "카테고리 등록 성공" |
                
                 ---
                
                 ### Response > content
                
                 | **키** | **타입** | **설명** | **예시** |
                 | --- | --- | --- | --- |
                 | optionCategoryId | number | 생성된 카테고리 ID | 10004 |
                 | parentCategoryId | number | 상위 카테고리 ID | 1 |
                 | name | string | 카테고리명 | "루테인" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = RegisterCategoryRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "parentCategoryId": 1,
                                  "name": "루테인"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Created",
                        content = @Content(
                                schema = @Schema(implementation = StringResponseSchema.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 201,
                                          "code": "SUC01",
                                          "timestamp": "2025-12-31T16:06:44.009132",
                                          "content": {
                                            "optionCategoryId": 10004,
                                            "parentCategoryId": 1001,
                                            "name": "루테인"
                                          },
                                          "message": "카테고리 등록 성공"
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
                                          "timestamp": "2025-12-31T12:00:00.000",
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
                                          "timestamp": "2025-12-31T12:00:00.000",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "존재하지 않는 상위 카테고리 ID",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 404,
                                          "code": "NOT_FOUND",
                                          "timestamp": "2025-12-31T16:17:14.612354",
                                          "content": null,
                                          "message": "존재하지 않는 상위 카테고리 ID입니다. 전송된 상위 카테고리 ID: 1003"
                                        }
                                        """)
                        )
                )
        }
)
public @interface RegisterCategoryApiDocs {
}


