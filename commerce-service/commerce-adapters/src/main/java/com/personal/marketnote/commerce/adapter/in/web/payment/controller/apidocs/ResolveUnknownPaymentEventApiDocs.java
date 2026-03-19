package com.personal.marketnote.commerce.adapter.in.web.payment.controller.apidocs;

import com.personal.marketnote.commerce.adapter.in.web.payment.request.ResolveUnknownPaymentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

/**
 * (관리자) UNKNOWN 결제 이벤트 수동 해소 API 문서 애노테이션.
 *
 * @author 성효빈
 * @since 2026-03-19
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "(관리자) UNKNOWN 결제 이벤트 수동 해소",
        description = """
                작성일자: 2026-03-19
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                관리자가 KCP 가맹점 사이트에서 확인한 결과를 바탕으로 UNKNOWN 이벤트를 COMPLETE 또는 FAILED로 해소합니다.
                
                ---
                
                ## Path Parameters
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | orderKey | string | 주문 키 | Y | "ORD-20260311-001" |
                
                ---
                
                ## Request Body
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | resolvedStatus | string | 해소 상태 (COMPLETE 또는 FAILED) | Y | "COMPLETE" |
                | resultCode | string | 결과 코드 | N | "0000" |
                | resultMessage | string | 결과 메시지 | N | "정상 승인" |
                | pgPaymentKey | string | PG 결제 키 | N | "PG20260311001" |
                | approvalNumber | string | 승인 번호 | N | "12345678" |
                | appTime | string | 승인 시각 | N | "20260311103000" |
                
                ---
                
                ## Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | orderKey | string | 주문 키 | "ORD-20260311-001" |
                | resolvedStatus | string | 해소된 상태 | "COMPLETE" |
                | orderId | number | 주문 ID | 10 |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "orderKey",
                        description = "주문 키",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string")
                )
        },
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = ResolveUnknownPaymentRequest.class),
                        examples = @ExampleObject("""
                                {
                                    "resolvedStatus": "COMPLETE",
                                    "resultCode": "0000",
                                    "resultMessage": "정상 승인",
                                    "pgPaymentKey": "PG20260311001",
                                    "approvalNumber": "12345678",
                                    "appTime": "20260311103000"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "UNKNOWN 결제 이벤트 해소 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-03-19T12:00:00.000",
                                          "content": {
                                            "orderKey": "ORD-20260311-001",
                                            "resolvedStatus": "COMPLETE",
                                            "orderId": 10
                                          },
                                          "message": "UNKNOWN 결제 이벤트 해소 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "인증 실패"
                ),
                @ApiResponse(
                        responseCode = "403",
                        description = "관리자 권한 없음"
                )
        }
)
public @interface ResolveUnknownPaymentEventApiDocs {
}
