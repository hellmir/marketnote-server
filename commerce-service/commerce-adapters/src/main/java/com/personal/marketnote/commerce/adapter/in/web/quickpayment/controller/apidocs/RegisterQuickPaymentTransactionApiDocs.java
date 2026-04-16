package com.personal.marketnote.commerce.adapter.in.web.quickpayment.controller.apidocs;

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
        summary = "빠른결제 거래 등록 (Mobile)",
        description = """
                작성일자: 2026-04-16

                작성자: 성효빈

                ---

                ## Description

                - 빠른결제 카드 등록을 위한 KCP 거래등록 API를 호출합니다.

                - approvalKey와 payUrl을 반환하여 클라이언트가 KCP 결제창을 호출할 수 있도록 합니다.

                - 요청 본문은 필요하지 않으며, 인증 토큰만으로 처리됩니다.

                ---

                ## Response > content

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | transactionId | string | 거래 식별 ID (UUID) | "550e8400-e29b-41d4-a716-446655440000" |
                | approvalKey | string | KCP 승인 키 | "5VRPBBPEo1cQnXV1SapUB8M20OrBegcHydQ/iE3gBBCT" |
                | payUrl | string | Mobile 결제 URL | "https://testsmpay.kcp.co.kr/pay/mobileGW.kcp" |
                | traceNo | string | 추적 번호 | "T0000MGABJUXLY81" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "빠른결제 거래 등록 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-16T17:19:33.409686",
                                          "content": {
                                            "transactionId": "550e8400-e29b-41d4-a716-446655440000",
                                            "approvalKey": "5VRPBBPEo1cQnXV1SapUB8M20OrBegcHydQ/iE3gBBCT",
                                            "payUrl": "https://testsmpay.kcp.co.kr/pay/mobileGW.kcp",
                                            "traceNo": "T0000MGABJUXLY81"
                                          },
                                          "message": "빠른결제 거래 등록 성공"
                                        }
                                        """)
                        )
                )
        })
public @interface RegisterQuickPaymentTransactionApiDocs {
}
