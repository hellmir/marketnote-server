package com.personal.marketnote.product.adapter.in.web.product.controller.apidocs;

import com.personal.marketnote.product.adapter.in.web.product.request.ReorderProductTagsRequest;
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
        summary = "(판매자/관리자) 상품 태그 순서 변경",
        description = """
                작성일자: 2026-03-17
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 상품 태그의 순서(orderNum)를 변경합니다.
                
                - 판매자(소유자) 또는 관리자만 가능합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | tagOrders | array | 태그 순서 목록 | Y | [{"tagId": 1, "orderNum": 2}, {"tagId": 2, "orderNum": 1}] |
                | tagOrders[].tagId | number | 태그 ID | Y | 1 |
                | tagOrders[].orderNum | number | 순서 번호 (1 이상) | Y | 2 |
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-03-17T12:00:00.000" |
                | content | object | 응답 본문 | null |
                | message | string | 처리 결과 | "상품 태그 순서 변경 성공" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = ReorderProductTagsRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "tagOrders": [
                                    {"tagId": 1, "orderNum": 2},
                                    {"tagId": 2, "orderNum": 1}
                                  ]
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "상품 태그 순서 변경 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-03-17T12:00:00.000",
                                          "content": null,
                                          "message": "상품 태그 순서 변경 성공"
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
                                          "timestamp": "2026-03-17T12:00:00.000",
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
                                          "timestamp": "2026-03-17T12:00:00.000",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        })
public @interface ReorderProductTagsApiDocs {
}
