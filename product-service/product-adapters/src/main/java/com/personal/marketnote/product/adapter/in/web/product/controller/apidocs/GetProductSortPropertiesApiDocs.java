package com.personal.marketnote.product.adapter.in.web.product.controller.apidocs;

import com.personal.marketnote.common.adapter.in.api.schema.StringResponseSchema;
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
        summary = "(비회원) 상품 정렬 속성 목록 조회",
        description = """
                작성일자: 2026-01-02
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                상품 정렬 속성 목록을 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-02T10:37:32.320824" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "상품 정렬 속성 목록 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | properties | array | 상품 정렬 속성 목록 | [ ... ] |
                
                ---
                
                ### Response > content > properties
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | name | string | 정렬 속성 이름 | "ORDER_NUM" |
                | description | string | 정렬 속성 설명 | "정렬 순서" |
                """, security = {
        @SecurityRequirement(name = "bearer")
},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "조회 성공",
                        content = @Content(
                                schema = @Schema(implementation = StringResponseSchema.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-02T10:54:07.722551",
                                          "content": {
                                            "properties": [
                                              {
                                                "name": "ORDER_NUM",
                                                "description": "정렬 순서"
                                              },
                                              {
                                                "name": "ACCUMULATED_POINT",
                                                "description": "적립금"
                                              },
                                              {
                                                "name": "POPULARITY",
                                                "description": "인기도"
                                              },
                                              {
                                                "name": "DISCOUNT_PRICE",
                                                "description": "할인 가격"
                                              }
                                            ]
                                          },
                                          "message": "상품 정렬 속성 목록 조회 성공"
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
                                          "timestamp": "2026-01-02T10:19:52.558748",
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
                                          "timestamp": "2026-01-02T10:19:52.558748",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        })
public @interface GetProductSortPropertiesApiDocs {
}
