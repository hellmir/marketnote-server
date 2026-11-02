package com.personal.marketnote.commerce.adapter.in.web.order.controller.apidocs;

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
        summary = "(관리자) 주문 상품의 리뷰 작성 여부 업데이트",
        description = """
                작성일자: 2026-01-12
                
                작성자: 성효빈
                
                주문 상품의 리뷰 작성 여부를 업데이트합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | orderId | number | 주문 ID | Y | 1 |
                | pricePolicyId | number | 가격 정책 ID | Y | 11 |
                | isReviewed | boolean | 리뷰 작성 여부 | Y | true |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 201: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-05T12:12:30.013" |
                | content | object | 응답 본문 | null |
                | message | string | 처리 결과 | "주문 상품의 리뷰 작성 여부 업데이트 성공" |
                """, security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "orderId",
                        description = "주문 ID",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "number", example = "1")
                ),
                @Parameter(
                        name = "pricePolicyId",
                        description = "가격 정책 ID",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "number", example = "11")
                ),
                @Parameter(
                        name = "isReviewed",
                        description = "리뷰 작성 여부",
                        in = ParameterIn.QUERY,
                        required = true,
                        schema = @Schema(type = "boolean", example = "true")
                )
        },

        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "리뷰 작성 여부 업데이트 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-05T12:12:30.013",
                                          "content": null,
                                          "message": "주문 상품의 리뷰 작성 여부 업데이트 성공"
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
                                          "timestamp": "2026-01-05T12:12:30.013",
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
                                          "timestamp": "2026-01-05T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        })
public @interface UpdateOrderProductReviewStatusApiDocs {
}

