package com.personal.marketnote.commerce.adapter.in.web.payment.controller.apidocs;

import com.personal.marketnote.commerce.adapter.in.web.payment.request.ApprovePaymentRequest;
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
        summary = "결제 승인",
        description = """
                작성일자: 2026-02-25
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - KCP 결제 승인 API를 호출합니다.
                
                - 서버 DB 금액으로 위변조 검증 후 승인을 수행합니다.
                
                - 승인 성공 시 주문 상태를 PAID로 변경합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | orderKey | string | 주문 키 (UUID) | Y | "550e8400-e29b-41d4-a716-446655440000" |
                | encData | string | KCP 결제창 인증결과 암호화 데이터 | Y | "..." |
                | encInfo | string | KCP 결제창 인증결과 암호화 정보 | Y | "..." |
                | payType | string | 결제수단 (PACA: 신용카드, PABK: 계좌이체, PAMC: 휴대폰) | Y | "PACA" |
                
                ---
                
                ## Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | orderId | number | 주문 ID | 1 |
                | orderKey | string | 주문 키 (UUID) | "550e8400-..." |
                | pgPaymentKey | string | PG사 거래번호 (tno) | "KCP000001" |
                | amount | number | 결제 금액 | 100000 |
                | resultCode | string | 결과 코드 | "0000" |
                | resultMessage | string | 결과 메시지 | "성공" |
                | payMethod | string | 결제 수단 | "PACA" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = ApprovePaymentRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "orderKey": "550e8400-e29b-41d4-a716-446655440000",
                                  "encData": "encrypted_data_from_kcp",
                                  "encInfo": "encrypted_info_from_kcp",
                                  "payType": "PACA"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "결제 승인 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-25T12:00:00.000",
                                          "content": {
                                            "orderId": 1,
                                            "orderKey": "550e8400-e29b-41d4-a716-446655440000",
                                            "pgPaymentKey": "KCP000001",
                                            "amount": 100000,
                                            "resultCode": "0000",
                                            "resultMessage": "성공",
                                            "payMethod": "PACA"
                                          },
                                          "message": "결제 승인 성공"
                                        }
                                        """)
                        )
                )
        })
public @interface ApprovePaymentApiDocs {
}
