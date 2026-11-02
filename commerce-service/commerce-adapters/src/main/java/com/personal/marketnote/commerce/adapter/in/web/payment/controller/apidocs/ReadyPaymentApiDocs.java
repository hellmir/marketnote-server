package com.personal.marketnote.commerce.adapter.in.web.payment.controller.apidocs;

import com.personal.marketnote.commerce.adapter.in.web.payment.request.ReadyPaymentRequest;
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
        summary = "거래 등록 (Mobile)",
        description = """
                작성일자: 2026-02-25
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - KCP 거래등록 API를 호출하여 결제 준비를 수행합니다.
                
                - Mobile 결제 시 approvalKey와 payUrl을 반환합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | orderKey | string | 주문 키 (UUID) | Y | "550e8400-e29b-41d4-a716-446655440000" |
                | payMethod | string | 결제 수단 | Y | "CARD" |
                | goodName | string | 상품명 | Y | "건강식품 외 2건" |
                
                ---
                
                ## Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | orderKey | string | 주문 키 (UUID) | "550e8400-e29b-41d4-a716-446655440000" |
                | approvalKey | string | KCP 승인 키 | "ABCD1234..." |
                | payUrl | string | Mobile 결제 URL | "https://testsmpay.kcp.co.kr/..." |
                | traceNo | string | 추적 번호 | "T0000..." |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = ReadyPaymentRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "orderKey": "550e8400-e29b-41d4-a716-446655440000",
                                  "payMethod": "CARD",
                                  "goodName": "건강식품 외 2건"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "거래 등록 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-25T17:19:33.409686",
                                          "content": {
                                            "orderKey": "550e8400-e29b-41d4-a716-446655440000",
                                            "approvalKey": "5VRPBBPEo1cQnXV1SapUB8M20OrBegcHydQ/iE3gBBCT",
                                            "payUrl": "https://testsmpay.kcp.co.kr/pay/mobileGW.kcp",
                                            "traceNo": "T0000MGABJUXLY81"
                                          },
                                          "message": "거래 등록 성공"
                                        }
                                        """)
                        )
                )
        })
public @interface ReadyPaymentApiDocs {
}
