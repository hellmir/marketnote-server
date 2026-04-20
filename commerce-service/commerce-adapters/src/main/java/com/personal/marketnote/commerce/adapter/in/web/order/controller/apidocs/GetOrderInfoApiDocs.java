package com.personal.marketnote.commerce.adapter.in.web.order.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "주문 정보 조회",
        description = """
                작성일자: 2026-01-05
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 주문 정보를 조회합니다.
                
                - 주문 상태 목록
                
                    - "PAYMENT_PENDING": 결제 대기
                
                    - "PAID": 결제 완료
                
                    - "FAILED": 결제 실패
                
                    - "PREPARING": 상품 준비중
                
                    - "CANCEL_REQUESTED": 주문 취소 요청됨
                
                    - "CANCELLED": 주문 취소
                
                    - "SHIPPING": 배송중
                
                    - "DELIVERED": 배송 완료
                
                    - "CONFIRMED": 구매 확정
                
                    - "REFUND_REQUESTED": 환불 요청됨
                
                    - "REFUND_RECALLING": 환불 회수 중
                
                    - "REFUND_SHIPPING": 환불 배송 중
                
                    - "PARTIALLY_REFUNDED": 부분 환불됨
                
                    - "REFUNDED": 환불 완료
                
                - 변경 사유 카테고리 목록
                
                    - "CANCEL_ORDER": 구매 의사 취소
                
                    - "CHANGE_OPTION": 색상, 사이즈 등 변경
                
                    - "MISTAKE": 주문 실수
                
                    - "ETC": 직접 입력
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | id | number | 주문 ID | Y | 1 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 201: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-05T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "주문 정보 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 주문 ID | 1 |
                | buyerId | number | 구매자 회원 ID | 1 |
                | orderNumber | string | 주문 번호 | "2026011300001" |
                | orderStatus | string | 주문 상태 | "CANCELLED" |
                | statusChangeReasonCategory | string | 주문 상태 변경 사유 카테고리 | "CANCEL_ORDER" |
                | statusChangeReason | string | 주문 상태 변경 사유 | "상품이 별로임" |
                | totalAmount | number | 총 주문 금액(원) | 100000 |
                | paidAmount | number | 결제 금액(원) | 100000 |
                | couponAmount | number | 쿠폰 할인 금액(원) | 5000 |
                | pointAmount | number | 포인트 사용 금액(원) | 5000 |
                | orderProducts | array | 주문 상품 목록 | [ ... ] |
                
                ---
                
                ### Response > orderProducts
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | sellerId | number | 판매자 회원 ID | 1 |
                | productId | number | 상품 ID | 1 |
                | pricePolicyId | number | 가격 정책 ID | 1 |
                | sharerKey | string(uuid) | 링크 공유 회원 식별키 | "550e8400-e29b-41d4-a716-446655440000" |
                | quantity | number | 주문 수량 | 2 |
                | unitAmount | number | 단위 금액(원) | 50000 |
                | imageUrl | string | 상품 이미지 URL | "https://marketnote.s3.amazonaws.com/product/30/1763534195922_image_600.png" |
                | orderStatus | string | 주문 상태 | "CANCELLED" |
                | brandName | string | 브랜드명 | "브랜드명1" |
                | productName | string | 상품명 | "스프링노트1234" |
                | selectedOptions | array | 선택 옵션 목록 | [ ... ] |
                
                ---
                
                ### Response > orderProducts > selectedOptions
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 옵션 ID | 1 |
                | content | string | 옵션 내용 | "1박스" |
                | status | string | 상태 | "ACTIVE" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "id",
                        description = "주문 ID",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "number")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "주문 정보 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-05T12:06:31.100002",
                                          "content": {
                                            "orderInfo": {
                                              "id": 15,
                                              "buyerId": 17,
                                              "orderNumber": "2026011300001",
                                              "orderStatus": "CANCELLED",
                                              "statusChangeReasonCategory": "CANCEL_ORDER",
                                              "statusChangeReason": "상품이 별로임",
                                              "totalAmount": 120000,
                                              "paidAmount": 120000,
                                              "couponAmount": 5000,
                                              "pointAmount": 5000,
                                              "orderPrducts": [
                                                {
                                                  "sellerId": 12,
                                                  "productId": 1,
                                                  "pricePolicyId": 180,
                                                  "sharerKey": "550e8400-e29b-41d4-a716-446655440000",
                                                  "quantity": 10,
                                                  "unitAmount": 70000,
                                                  "imageUrl": "https://marketnote.s3.amazonaws.com/product/30/1763533916081_image_600.png",
                                                  "orderStatus": "CANCELLED",
                                                  "brandName": "브랜드명1",
                                                  "productName": "스프링노트1234",
                                                  "selectedOptions": [
                                                    {
                                                      "id": 60,
                                                      "content": "3박스",
                                                      "status": "ACTIVE"
                                                    },
                                                    {
                                                      "id": 62,
                                                      "content": "60개입",
                                                      "status": "ACTIVE"
                                                    }
                                                  ],
                                                  "isReviewed": false
                                                },
                                                {
                                                  "sellerId": 11,
                                                  "productId": 2,
                                                  "pricePolicyId": 166,
                                                  "sharerKey": null,
                                                  "quantity": 2,
                                                  "unitAmount": 50000,
                                                  "imageUrl": "https://marketnote.s3.amazonaws.com/product/30/1763534195922_image_600.png",
                                                  "orderStatus": "CANCELLED",
                                                  "brandName": "브랜드명2",
                                                  "productName": "스프링노트1234",
                                                  "selectedOptions": [
                                                    {
                                                      "id": 60,
                                                      "content": "3박스",
                                                      "status": "ACTIVE"
                                                    },
                                                    {
                                                      "id": 61,
                                                      "content": "30개입",
                                                      "status": "ACTIVE"
                                                    }
                                                  ],
                                                  "isReviewed": false
                                                }
                                              ]
                                            }
                                          },
                                          "message": "주문 정보 조회 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "토큰 인증 실패",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 401,
                                          "code": "UNAUTHORIZED",
                                          "timestamp": "2026-01-05T12:12:30.013",
                                          "content": null,
                                          "message": "Invalid token"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "403",
                        description = "토큰 인가 실패",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 403,
                                          "code": "FORBIDDEN",
                                          "timestamp": "2026-01-05T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        })
public @interface GetOrderInfoApiDocs {
}
