package com.personal.marketnote.commerce.adapter.in.web.payment.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "결제 정보 조회",
        description = """
                작성일자: 2026-02-25
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 주문 키로 결제 정보를 조회합니다.
                
                ---
                
                ## Path Variable
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | orderKey | string | 주문 키 (UUID) | Y | "550e8400-e29b-41d4-a716-446655440000" |
                
                ---
                
                ## Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | orderId | number | 주문 ID | 1 |
                | orderKey | string | 주문 키 (UUID) | "550e8400-..." |
                | paymentAmount | number | 결제 금액 | 100000 |
                | successYn | boolean | 결제 성공 여부 | true |
                | refundedYn | boolean | 환불 여부 | false |
                | refundAmount | number | 환불 금액 | 0 |
                | pgPaymentKey | string | PG사 거래번호 | "KCP000001" |
                | pgCompanyKey | string | PG사 식별 키 | "NHN_KCP" |
                | method | string | 결제 수단 | "PACA" |
                | cardNumber | string | 카드번호 (마스킹) | "5200********1234" |
                | approvalNumber | string | 승인번호 | "12345678" |
                | installment | number | 할부 개월수 | 0 |
                | issueCompanyName | string | 발급사명 | "신한카드" |
                | resultCode | string | 결과 코드 | "0000" |
                | resultMessage | string | 결과 메시지 | "성공" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "결제 정보 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-25T12:00:00.000",
                                          "content": {
                                            "orderId": 1,
                                            "orderKey": "550e8400-e29b-41d4-a716-446655440000",
                                            "paymentAmount": 100000,
                                            "successYn": true,
                                            "refundedYn": false,
                                            "refundAmount": 0,
                                            "pgPaymentKey": "KCP000001",
                                            "pgCompanyKey": "NHN_KCP",
                                            "method": "PACA",
                                            "cardNumber": "5200********1234",
                                            "approvalNumber": "12345678",
                                            "installment": 0,
                                            "issueCompanyName": "신한카드",
                                            "resultCode": "0000",
                                            "resultMessage": "성공"
                                          },
                                          "message": "결제 정보 조회 성공"
                                        }
                                        """)
                        )
                )
        })
public @interface GetPaymentApiDocs {
}
