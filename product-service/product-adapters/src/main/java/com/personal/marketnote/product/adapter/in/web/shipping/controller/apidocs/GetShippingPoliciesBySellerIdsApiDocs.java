package com.personal.marketnote.product.adapter.in.web.shipping.controller.apidocs;

import com.personal.marketnote.common.adapter.in.api.schema.StringResponseSchema;
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
        summary = "(관리자) 판매자별 배송비 정책 배치 조회",
        description = """
                작성일자: 2026-03-19
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 판매자 ID 목록으로 배송비 정책을 배치 조회합니다.
                
                - 서비스 간 내부 통신용 API입니다.
                
                - 관리자 권한이 필요합니다.
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 401: 인증 실패 / 403: 인가 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "UNAUTHORIZED" / "FORBIDDEN" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-03-19T10:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "판매자별 배송비 정책 배치 조회 성공" |
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | shippingPolicies | array | 배송비 정책 목록 | [ ... ] |
                
                ### Response > content > shippingPolicies[]
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | sellerId | number | 판매자 ID | 10 |
                | shippingFee | number | 배송비 | 3000 |
                | freeShippingThreshold | number | 무료배송 기준금액 | 20000 |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "sellerIds",
                        description = "판매자 ID 목록",
                        required = true,
                        example = "10,20,30"
                )
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
                                          "timestamp": "2026-03-19T10:00:00.000",
                                          "content": {
                                            "shippingPolicies": [
                                              {
                                                "sellerId": 10,
                                                "shippingFee": 3000,
                                                "freeShippingThreshold": 20000
                                              },
                                              {
                                                "sellerId": 20,
                                                "shippingFee": 2500,
                                                "freeShippingThreshold": 30000
                                              }
                                            ]
                                          },
                                          "message": "판매자별 배송비 정책 배치 조회 성공"
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
                                          "timestamp": "2026-03-19T10:00:00.000",
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
                                          "timestamp": "2026-03-19T10:00:00.000",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetShippingPoliciesBySellerIdsApiDocs {
}
