package com.personal.marketnote.commerce.adapter.in.web.payment.controller.apidocs;

import com.personal.marketnote.commerce.adapter.in.web.payment.request.CancelPaymentRequest;
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
        summary = "결제 취소",
        description = """
                작성일자: 2026-02-25
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - KCP 결제 취소 API를 호출합니다.
                
                - 전체취소(FULL)와 부분취소(PARTIAL) 모두 지원합니다.
                
                ---
                
                ## Path Variable

                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | orderKey | string | 주문 키 (UUID) | Y | "550e8400-e29b-41d4-a716-446655440000" |

                ---

                ## Header

                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | Idempotency-Key | string | 요청 식별 멱등성 키 (UUID v4) | Y | "11111111-2222-4333-8444-555555555555" |

                같은 키로 재요청 시 최초 응답을 그대로 반환합니다. 처리 중(IN_PROGRESS)이면 409를 반환합니다.
                멱등성 레코드는 생성 후 24시간까지 유지됩니다.

                ---

                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | cancelType | string | 취소 유형 (FULL/PARTIAL) | Y | "FULL" |
                | cancelAmount | number | 취소 금액 (부분취소 시 필수) | N | 50000 |
                | cancelReason | string | 취소 사유 | N | "고객 변심" |
                | cancelProducts | array | 취소 대상 상품 목록 (부분취소 시 재고 복구용, 선택) | N | [{"pricePolicyId": 1, "quantity": 2}] |
                | cancelProducts[].pricePolicyId | number | 가격 정책 ID | Y | 1 |
                | cancelProducts[].quantity | number | 취소 수량 | Y | 2 |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = CancelPaymentRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "cancelType": "FULL",
                                  "cancelAmount": null,
                                  "cancelReason": "고객 변심",
                                  "cancelProducts": [
                                    {
                                      "pricePolicyId": 1,
                                      "quantity": 2
                                    }
                                  ]
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "결제 취소 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-25T12:00:00.000",
                                          "content": null,
                                          "message": "결제 취소 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "Idempotency-Key 재사용 또는 처리 중 (IN_PROGRESS)",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 409,
                                          "code": "ERR_PAYMENT_CANCEL_IDEMPOTENCY_11",
                                          "timestamp": "2026-02-25T12:00:00.000",
                                          "message": "동일 멱등성 키로 이미 처리 중입니다."
                                        }
                                        """)
                        )
                )
        })
public @interface CancelPaymentApiDocs {
}
