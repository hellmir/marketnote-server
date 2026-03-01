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
        summary = "(관리자) 주문 내역 조회",
        description = """
                작성일자: 2026-03-02

                작성자: 성효빈

                ---

                ## Description

                - 관리자가 전체 주문 내역을 판매자별, 기간별, 상태별로 조회합니다.
                - 모든 필터는 선택 사항이며, 필터를 지정하지 않으면 전체 주문을 조회합니다.
                - PAYMENT_PENDING 상태의 주문은 제외됩니다.

                ---

                ## Query Parameters

                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | sellerId | number | 판매자 ID | N | 10 |
                | startDate | string (ISO DateTime) | 조회 시작 일시 | N | 2026-02-01T00:00:00 |
                | endDate | string (ISO DateTime) | 조회 종료 일시 | N | 2026-02-28T23:59:59 |
                | orderStatus | string (enum) | 주문 상태 | N | PAID |

                ---

                ## Response > content

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | orders | array | 주문 목록 | - |
                | orders[].orderInfo.id | number | 주문 ID | 1 |
                | orders[].orderInfo.buyerId | number | 구매자 ID | 100 |
                | orders[].orderInfo.orderNumber | string | 주문 번호 | "ORD20260220001" |
                | orders[].orderInfo.orderStatus | string | 주문 상태 | "PAID" |
                | orders[].orderInfo.totalAmount | number | 총 금액 | 100000 |
                | orders[].orderInfo.paidAmount | number | 결제 금액 | 95000 |
                | orders[].orderInfo.orderProducts | array | 주문 상품 목록 | - |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "관리자 주문 내역 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-03-02T12:00:00.000",
                                          "content": {
                                            "orders": [
                                              {
                                                "orderInfo": {
                                                  "id": 1,
                                                  "buyerId": 100,
                                                  "orderNumber": "ORD20260220001",
                                                  "orderStatus": "PAID",
                                                  "totalAmount": 100000,
                                                  "paidAmount": 95000,
                                                  "couponAmount": 3000,
                                                  "pointAmount": 2000,
                                                  "orderProducts": [
                                                    {
                                                      "sellerId": 10,
                                                      "productId": 50,
                                                      "pricePolicyId": 30,
                                                      "quantity": 2,
                                                      "unitAmount": 50000,
                                                      "brandName": "테스트 브랜드",
                                                      "productName": "테스트 상품"
                                                    }
                                                  ]
                                                }
                                              }
                                            ]
                                          },
                                          "message": "관리자 주문 내역 조회 성공"
                                        }
                                        """)
                        )
                )
        })
public @interface GetAdminOrdersApiDocs {
}
