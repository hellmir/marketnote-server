package com.personal.marketnote.commerce.adapter.in.web.payment.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

/**
 * 관리자 환불 목록 조회 API 문서 애노테이션.
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "(관리자) 주문별 환불 목록 조회",
        description = """
                작성일자: 2026-03-04
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 관리자가 주문 ID에 해당하는 환불 상세 목록을 조회합니다.
                
                ---
                
                ## Query Parameters
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | order-id | number | 주문 ID | Y | 1 |
                
                ---
                
                ## Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | content | array | 환불 목록 | - |
                | content[].id | number | 환불 ID | 1 |
                | content[].paymentId | number | 결제 ID | 10 |
                | content[].orderId | number | 주문 ID | 1 |
                | content[].refundType | string (enum) | 환불 유형 (FULL_REFUND, PARTIAL_REFUND) | "FULL_REFUND" |
                | content[].refundAmount | number | 환불 금액 | 50000 |
                | content[].cancelReason | string | 취소 사유 | "단순 변심" |
                | content[].processedBy | string | 처리자 | "ADMIN" |
                | content[].pgRefundKey | string | PG 환불 키 | "RF20260224001" |
                | content[].createdAt | string (ISO DateTime) | 환불 일시 | "2026-03-02T10:30:00" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "환불 목록 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-03-02T10:30:00.000",
                                          "content": [
                                            {
                                              "id": 1,
                                              "paymentId": 10,
                                              "orderId": 1,
                                              "refundType": "FULL_REFUND",
                                              "refundAmount": 50000,
                                              "cancelReason": "단순 변심",
                                              "processedBy": "ADMIN",
                                              "pgRefundKey": "RF20260224001",
                                              "createdAt": "2026-03-02T10:30:00"
                                            },
                                            {
                                              "id": 2,
                                              "paymentId": 10,
                                              "orderId": 1,
                                              "refundType": "PARTIAL_REFUND",
                                              "refundAmount": 15000,
                                              "cancelReason": "상품 불량",
                                              "processedBy": "ADMIN",
                                              "pgRefundKey": "RF20260224002",
                                              "createdAt": "2026-03-02T11:00:00"
                                            }
                                          ],
                                          "message": "환불 목록 조회 성공"
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
        })
public @interface GetAdminRefundsApiDocs {
}
