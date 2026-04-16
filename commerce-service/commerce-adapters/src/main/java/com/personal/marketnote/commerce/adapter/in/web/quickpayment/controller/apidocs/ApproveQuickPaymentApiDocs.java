package com.personal.marketnote.commerce.adapter.in.web.quickpayment.controller.apidocs;

import com.personal.marketnote.commerce.adapter.in.web.quickpayment.request.ApproveQuickPaymentRequest;
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
        summary = "빠른결제 결제 승인",
        description = """
                작성일자: 2026-04-16

                작성자: 성효빈

                ---

                ## Description

                - 저장된 배치키를 사용하여 서버 투 서버로 KCP 결제 승인을 수행합니다.

                - 클라이언트 결제창 경유 없이 배치키 기반으로 즉시 승인합니다.

                ---

                ## Request

                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | orderKey | string | 주문 키 (UUID) | Y | "550e8400-e29b-41d4-a716-446655440000" |
                | quickPaymentCardId | number | 빠른결제 카드 ID | Y | 1 |
                | goodName | string | 상품명 | Y | "건강식품 외 2건" |

                ---

                ## Response > content

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | orderId | number | 주문 ID | 1 |
                | orderKey | string | 주문 키 (UUID) | "550e8400-e29b-41d4-a716-446655440000" |
                | pgPaymentKey | string | PG 결제 키 (KCP tno) | "T0000..." |
                | amount | number | 결제 금액 | 50000 |
                | resultCode | string | 결과 코드 | "0000" |
                | resultMessage | string | 결과 메시지 | "승인 성공" |
                | payMethod | string | 결제 수단 | "PACA" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = ApproveQuickPaymentRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "orderKey": "550e8400-e29b-41d4-a716-446655440000",
                                  "quickPaymentCardId": 1,
                                  "goodName": "건강식품 외 2건"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "빠른결제 승인 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-16T17:19:33.409686",
                                          "content": {
                                            "orderId": 1,
                                            "orderKey": "550e8400-e29b-41d4-a716-446655440000",
                                            "pgPaymentKey": "T0000MGABJUXLY81",
                                            "amount": 50000,
                                            "resultCode": "0000",
                                            "resultMessage": "승인 성공",
                                            "payMethod": "PACA"
                                          },
                                          "message": "빠른결제 승인 성공"
                                        }
                                        """)
                        )
                )
        })
public @interface ApproveQuickPaymentApiDocs {
}
