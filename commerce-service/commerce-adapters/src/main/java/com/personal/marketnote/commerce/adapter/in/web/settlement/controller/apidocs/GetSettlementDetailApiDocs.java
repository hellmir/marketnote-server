package com.personal.marketnote.commerce.adapter.in.web.settlement.controller.apidocs;

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
        summary = "(관리자) 정산 내역 상세 조회",
        description = """
                작성일자: 2026-03-02
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                정산 ID로 해당 정산에 포함된 결제 배분(PaymentAllocation) 목록을 조회합니다.
                
                ---
                
                ## Path Variable
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | id | number | 정산 ID | Y | 1 |
                
                ---
                
                ## Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 배분 ID | 1 |
                | orderId | number | 주문 ID | 100 |
                | sellerId | number | 판매자 ID | 10 |
                | allocatedAmount | number | 배분 금액 | 50000 |
                | shippingFee | number | 배송비 | 0 |
                | transactionType | string | 거래 유형 | "ORDER_REGISTRATION" |
                | targetType | string | 대상 유형 | "ORDER" |
                | createdAt | string | 생성일시 | "2026-02-15T14:30:00" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "정산 내역 상세 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-03-02T12:00:00.000",
                                          "content": [
                                            {
                                              "id": 1,
                                              "orderId": 100,
                                              "sellerId": 10,
                                              "allocatedAmount": 50000,
                                              "shippingFee": 0,
                                              "transactionType": "ORDER_REGISTRATION",
                                              "targetType": "ORDER",
                                              "createdAt": "2026-02-15T14:30:00"
                                            },
                                            {
                                              "id": 2,
                                              "orderId": 101,
                                              "sellerId": 10,
                                              "allocatedAmount": 30000,
                                              "shippingFee": 0,
                                              "transactionType": "ORDER_REGISTRATION",
                                              "targetType": "ORDER",
                                              "createdAt": "2026-02-15T15:00:00"
                                            }
                                          ],
                                          "message": "정산 내역 상세 조회 성공"
                                        }
                                        """)
                        )
                )
        })
public @interface GetSettlementDetailApiDocs {
}
