package com.personal.marketnote.commerce.adapter.in.web.payment.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

/**
 * (관리자) UNKNOWN 상태 결제 이벤트 조회 API 문서 애노테이션.
 *
 * @author 성효빈
 * @since 2026-03-19
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "(관리자) UNKNOWN 상태 결제 이벤트 조회",
        description = """
                작성일자: 2026-03-19
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                KCP 통신 장애로 승인 여부를 알 수 없는 결제 이벤트 목록을 조회합니다.
                
                ---
                
                ## Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | content | array | UNKNOWN 결제 이벤트 목록 | - |
                | content[].id | number | 결제 이벤트 ID | 1 |
                | content[].orderId | number | 주문 ID | 10 |
                | content[].orderKey | string | 주문 키 | "ORD-20260311-001" |
                | content[].amount | number | 결제 금액 | 50000 |
                | content[].method | string | 결제 수단 | "CARD" |
                | content[].resultCode | string | 결과 코드 | null |
                | content[].resultMessage | string | 결과 메시지 | null |
                | content[].createdAt | string (ISO DateTime) | 생성 일시 | "2026-03-19T10:30:00" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "UNKNOWN 결제 이벤트 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-03-19T12:00:00.000",
                                          "content": [
                                            {
                                              "id": 1,
                                              "orderId": 10,
                                              "orderKey": "ORD-20260311-001",
                                              "amount": 50000,
                                              "method": "CARD",
                                              "resultCode": null,
                                              "resultMessage": null,
                                              "createdAt": "2026-03-19T10:30:00"
                                            }
                                          ],
                                          "message": "UNKNOWN 결제 이벤트 조회 성공"
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
public @interface GetUnknownPaymentEventsApiDocs {
}
