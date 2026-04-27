package com.personal.marketnote.product.adapter.in.web.shipping.controller.apidocs;

import com.personal.marketnote.common.adapter.in.api.schema.StringResponseSchema;
import com.personal.marketnote.product.adapter.in.web.shipping.request.UpdateShippingPolicyRequest;
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
        summary = "(판매자/관리자) 배송비 정책 수정",
        description = """
                작성일자: 2026-03-19
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 판매자의 배송비 정책을 수정합니다.
                
                - 판매자 본인 또는 관리자만 가능합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | deliveryCompany | string | 배송업체 | Y | "CJ대한통운" |
                | shippingFee | number | 배송비 | Y | 2500 |
                | freeShippingThreshold | number | 무료배송 기준금액 | Y | 30000 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 정책 미존재 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-03-19T10:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "판매자 배송비 정책 수정 성공" |
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 배송비 정책 ID | 1 |
                | deliveryCompany | string | 배송업체 | "CJ대한통운" |
                | shippingFee | number | 배송비 | 2500 |
                | freeShippingThreshold | number | 무료배송 기준금액 | 30000 |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = UpdateShippingPolicyRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "deliveryCompany": "CJ대한통운",
                                  "shippingFee": 2500,
                                  "freeShippingThreshold": 30000
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "수정 성공",
                        content = @Content(
                                schema = @Schema(implementation = StringResponseSchema.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-03-19T10:00:00.000",
                                          "content": {
                                            "id": 1,
                                            "deliveryCompany": "CJ대한통운",
                                            "shippingFee": 2500,
                                            "freeShippingThreshold": 30000
                                          },
                                          "message": "판매자 배송비 정책 수정 성공"
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
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "배송비 정책 미존재",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 404,
                                          "code": "NOT_FOUND",
                                          "timestamp": "2026-03-19T10:00:00.000",
                                          "content": null,
                                          "message": "해당 판매자의 배송비 정책을 찾을 수 없습니다. sellerId=10"
                                        }
                                        """)
                        )
                )
        }
)
public @interface UpdateShippingPolicyApiDocs {
}
