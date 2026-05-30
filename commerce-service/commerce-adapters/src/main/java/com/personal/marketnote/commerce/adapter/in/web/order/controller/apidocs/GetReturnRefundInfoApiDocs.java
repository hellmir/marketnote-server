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
        summary = "반품 환불 예정 정보 조회",
        description = """
                작성일자: 2026-04-09
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 반품 신청 전 환불 예정 정보(총 상품 금액, 반품 배송비, 환불 방법, 환불 예정 금액, 환불 예정 캐시)를 조회합니다.
                - 구매자 소유권 검증 및 반품 가능 상태 검증을 수행합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **위치** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- | --- |
                | id | number | path | 주문 ID | Y | 1 |
                | reason-category | string | query | 반품 사유 카테고리 | Y | "SIMPLE_CHANGE_OF_MIND" |
                | return-price-policy-ids | number[] | query | 반품 대상 가격 정책 ID 목록 (미지정 시 전체 반품) | N | [200, 201] |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-04-09T12:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "반품 환불 예정 정보 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | totalProductAmount | number | 총 상품 금액 (반품 대상 상품의 금액 합계) | 50000 |
                | returnShippingFee | number | 반품 배송비 | 3000 |
                | refundMethod | string | 환불 방법 (결제 수단명) | "신용카드" |
                | estimatedRefundAmount | number | 환불 예정 금액 (PG 환불분) | 37000 |
                | estimatedRefundCash | number | 환불 예정 캐시 (포인트 환불분) | 10000 |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "id",
                        description = "주문 ID",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "number")
                ),
                @Parameter(
                        name = "reason-category",
                        description = "반품 사유 카테고리",
                        in = ParameterIn.QUERY,
                        required = true,
                        schema = @Schema(type = "string", example = "SIMPLE_CHANGE_OF_MIND")
                ),
                @Parameter(
                        name = "return-price-policy-ids",
                        description = "반품 대상 가격 정책 ID 목록 (미지정 시 전체 반품)",
                        in = ParameterIn.QUERY,
                        required = false,
                        schema = @Schema(type = "array", example = "[200, 201]")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "반품 환불 예정 정보 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-09T12:00:00.000",
                                          "content": {
                                            "totalProductAmount": 50000,
                                            "returnShippingFee": 3000,
                                            "refundMethod": "신용카드",
                                            "estimatedRefundAmount": 37000,
                                            "estimatedRefundCash": 10000
                                          },
                                          "message": "반품 환불 예정 정보 조회 성공"
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
                                          "timestamp": "2026-04-09T12:00:00.000",
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
                                          "timestamp": "2026-04-09T12:00:00.000",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "주문 조회 실패",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 404,
                                          "code": "NOT_FOUND",
                                          "timestamp": "2026-04-09T12:00:00.000",
                                          "content": null,
                                          "message": "주문을 찾을 수 없습니다."
                                        }
                                        """)
                        )
                )
        })
public @interface GetReturnRefundInfoApiDocs {
}
