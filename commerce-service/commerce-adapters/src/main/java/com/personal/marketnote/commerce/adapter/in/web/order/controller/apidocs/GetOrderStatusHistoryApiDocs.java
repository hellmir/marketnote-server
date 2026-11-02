package com.personal.marketnote.commerce.adapter.in.web.order.controller.apidocs;

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
        summary = "(관리자) 주문 상태 이력 조회",
        description = """
                작성일자: 2026-03-02
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 관리자가 특정 주문의 상태 변경 이력을 조회합니다.
                
                - CS 대응을 위해 주문의 전체 상태 변경 이력을 시간순(오름차순)으로 반환합니다.
                
                - 각 이력에는 변경된 상태, 사유 카테고리, 사유, 변경 시간이 포함됩니다.
                
                ---
                
                ## Path Parameters
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | id | number | 주문 ID | Y | 1 |
                
                ---
                
                ## Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | orderId | number | 주문 ID | 1 |
                | statusHistory | array | 상태 변경 이력 목록 | - |
                | statusHistory[].id | number | 이력 ID | 1 |
                | statusHistory[].orderStatus | string | 주문 상태 | "PAID" |
                | statusHistory[].orderStatusDescription | string | 주문 상태 설명 | "결제 완료" |
                | statusHistory[].reasonCategory | string | 사유 카테고리 | "CANCEL_ORDER" |
                | statusHistory[].reason | string | 변경 사유 | "결제 완료" |
                | statusHistory[].createdAt | string | 변경 일시 | "2026-03-02T12:00:00" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "주문 상태 이력 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-03-02T12:00:00.000",
                                          "content": {
                                            "orderId": 1,
                                            "statusHistory": [
                                              {
                                                "id": 1,
                                                "orderStatus": "PAYMENT_PENDING",
                                                "orderStatusDescription": "결제 대기",
                                                "reasonCategory": null,
                                                "reason": "결제 대기",
                                                "createdAt": "2026-03-02T10:00:00"
                                              },
                                              {
                                                "id": 2,
                                                "orderStatus": "PAID",
                                                "orderStatusDescription": "결제 완료",
                                                "reasonCategory": null,
                                                "reason": "결제 완료",
                                                "createdAt": "2026-03-02T10:05:00"
                                              },
                                              {
                                                "id": 3,
                                                "orderStatus": "CANCELLED",
                                                "orderStatusDescription": "주문 취소",
                                                "reasonCategory": "CANCEL_ORDER",
                                                "reason": "구매 의사 취소",
                                                "createdAt": "2026-03-02T11:00:00"
                                              }
                                            ]
                                          },
                                          "message": "주문 상태 이력 조회 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
        })
public @interface GetOrderStatusHistoryApiDocs {
}
