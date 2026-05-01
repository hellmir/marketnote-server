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
        summary = "나의 주문 내역 조회",
        description = """
                작성일자: 2026-01-06
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 나의 주문 내역을 조회합니다.
                
                - 주문 상태 목록
                
                    - "PAYMENT_PENDING": 결제 대기
                
                    - "PAID": 결제 완료
                
                    - "FAILED": 결제 실패
                
                    - "PREPARING": 상품 준비중
                
                    - "CANCELLED": 주문 취소
                
                    - "SHIPPING": 배송중
                
                    - "DELIVERED": 배송 완료
                
                    - "PARTIALLY_CONFIRMED": 부분 구매 확정

                    - "CONFIRMED": 구매 확정

                    - "RETURN_REQUESTED": 반품 요청됨

                    - "RETURN_IN_PROGRESS": 반품 진행 중

                    - "PARTIALLY_RETURNED": 부분 반품됨

                    - "RETURNED": 반품 완료
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | period | string | 조회 기간(ONE_MONTH, THREE_MONTHS, SIX_MONTHS, ONE_YEAR, ALL) | N | ONE_YEAR |
                | status | string | 주문 상태 필터(SHIPPING, DELIVERED, CONFIRMED, CANCEL_RETURN, ALL) | N | SHIPPING |
                | productName | string | 상품명 검색 키워드 | N | "공책" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 201: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-06T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "회원 주문 내역 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | orderHistory | array | 날짜별 주문 내역 묶음 | [ ... ] |

                ---
                ### Response > content > orderHistory

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | orderDate | string(date) | 주문 생성일(일 단위) | "2025-11-01" |
                | count | number | 해당 날짜의 주문 건수 | 3 |
                | orders | array | 주문 목록(주문 ID 내림차순) | [ ... ] |

                ---

                ### Response > content > orderHistory > orders

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 주문 ID | 1 |
                | buyerId | number | 구매자 회원 ID | 1 |
                | orderNumber | string | 주문 번호 | "2026011300001" |
                | orderStatus | string | 주문 상태 | "DELIVERED" |
                | totalAmount | number | 총 주문 금액(원) | 100000 |
                | paidAmount | number | 결제 금액(원) | 100000 |
                | couponAmount | number | 쿠폰 할인 금액(원) | 5000 |
                | pointAmount | number | 포인트 사용 금액(원) | 5000 |
                | orderProducts | array | 주문 상품 목록 | [ ... ] |

                ---

                ### Response > content > orderHistory > orders > orderProducts

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | sellerId | number | 판매자 회원 ID | 1 |
                | productId | number | 상품 ID | 1 |
                | pricePolicyId | number | 가격 정책 ID | 1 |
                | sharerKey | string(uuid) | 링크 공유 회원 식별키 | "550e8400-e29b-41d4-a716-446655440000" |
                | quantity | number | 주문 수량 | 2 |
                | unitAmount | number | 단위 금액(원) | 50000 |
                | imageUrl | string | 상품 이미지 URL | "https://marketnote.s3.amazonaws.com/product/30/1763534195922_image_600.png" |
                | orderStatus | string | 주문 상태 | "PAID" |
                | brandName | string | 브랜드명 | "브랜드명1" |
                | productName | string | 상품명 | "공책" |
                | selectedOptions | array | 선택 옵션 목록 | [ ... ] |
                | isReviewed | boolean | 리뷰 작성 여부 | true |

                ---

                ### Response > content > orderHistory > orders > orderProducts > selectedOptions
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 옵션 ID | 1 |
                | content | string | 옵션 내용 | "1박스" |
                | status | string | 상태 | "ACTIVE" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "회원 주문 내역 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-09T22:55:19.633942",
                                          "content": {
                                            "orderHistory": [
                                              {
                                                "orderDate": "2026-01-09",
                                                "count": 1,
                                                "orders": [
                                                  {
                                                    "id": 15,
                                                    "buyerId": 17,
                                                    "orderNumber": "2026011300001",
                                                    "orderStatus": "PAID",
                                                    "totalAmount": 120000,
                                                    "paidAmount": 120000,
                                                    "couponAmount": 5000,
                                                    "pointAmount": 5000,
                                                    "orderProducts": [
                                                      {
                                                        "sellerId": 12,
                                                        "productId": 1,
                                                        "pricePolicyId": 166,
                                                        "sharerKey": "550e8400-e29b-41d4-a716-446655440000",
                                                        "quantity": 2,
                                                        "unitAmount": 50000,
                                                        "imageUrl": "https://marketnote.s3.amazonaws.com/product/30/1763534195922_image_600.png",
                                                        "orderStatus": "PAID",
                                                        "brandName": "브랜드명1",
                                                        "productName": "스프링노트12345",
                                                        "selectedOptions": [
                                                          {
                                                            "id": 55,
                                                            "content": "4박스",
                                                            "status": "ACTIVE"
                                                          }
                                                        ],
                                                        "isReviewed": false
                                                      },
                                                      {
                                                        "sellerId": 12,
                                                        "productId": 2,
                                                        "pricePolicyId": 180,
                                                        "sharerKey": "550e8400-e29b-41d4-a716-446655440000",
                                                        "quantity": 10,
                                                        "unitAmount": 70000,
                                                        "imageUrl": "https://marketnote.s3.amazonaws.com/product/30/1763533916081_image_600.png",
                                                        "orderStatus": "PAID",
                                                        "brandName": "브랜드명2",
                                                        "productName": "스프링노트1234",
                                                        "selectedOptions": [
                                                          {
                                                            "id": 60,
                                                            "content": "3박스",
                                                            "status": "ACTIVE"
                                                          }
                                                        ],
                                                        "isReviewed": false
                                                      }
                                                    ]
                                                  }
                                                ]
                                              },
                                              {
                                                "orderDate": "2026-01-06",
                                                "count": 1,
                                                "orders": [
                                                  {
                                                    "id": 3,
                                                    "buyerId": 17,
                                                    "orderNumber": "2026011300002",
                                                    "orderStatus": "CONFIRMED",
                                                    "totalAmount": 50000,
                                                    "paidAmount": null,
                                                    "couponAmount": 5000,
                                                    "pointAmount": 5000,
                                                    "orderProducts": [
                                                      {
                                                        "sellerId": 1,
                                                        "productId": 3,
                                                        "pricePolicyId": 144,
                                                        "sharerKey": "550e8400-e29b-41d4-a716-446655440000",
                                                        "quantity": 10,
                                                        "unitAmount": 70000,
                                                        "imageUrl": "https://marketnote.s3.amazonaws.com/product/30/1763533916081_image_600.png",
                                                        "orderStatus": "PAID",
                                                        "brandName": "브랜드명3",
                                                        "productName": "스프링노트111",
                                                        "selectedOptions": [
                                                          {
                                                            "id": 42,
                                                            "content": "4박스",
                                                            "status": "ACTIVE"
                                                          },
                                                          {
                                                            "id": 46,
                                                            "content": "상",
                                                            "status": "ACTIVE"
                                                          },
                                                          {
                                                            "id": 47,
                                                            "content": "30개입",
                                                            "status": "ACTIVE"
                                                          }
                                                        ],
                                                        "isReviewed": false
                                                      },
                                                      {
                                                        "sellerId": 1,
                                                        "productId": 4,
                                                        "pricePolicyId": 189,
                                                        "sharerKey": null,
                                                        "quantity": 2,
                                                        "unitAmount": 50000,
                                                        "imageUrl": "https://marketnote.s3.amazonaws.com/product/30/1763534195922_image_600.png",
                                                        "orderStatus": "PAID",
                                                        "brandName": "브랜드명4",
                                                        "productName": "스프링노트1234",
                                                        "selectedOptions": [
                                                          {
                                                            "id": 60,
                                                            "content": "3박스",
                                                            "status": "ACTIVE"
                                                          },
                                                          {
                                                            "id": 63,
                                                            "content": "90개입",
                                                            "status": "ACTIVE"
                                                          }
                                                        ],
                                                        "isReviewed": false
                                                      }
                                                    ]
                                                  }
                                                ]
                                              },
                                              {
                                                "orderDate": "2026-01-05",
                                                "count": 1,
                                                "orders": [
                                                  {
                                                    "id": 2,
                                                    "buyerId": 17,
                                                    "orderNumber": "2026011300003",
                                                    "orderStatus": "DELIVERED",
                                                    "totalAmount": 100000,
                                                    "paidAmount": null,
                                                    "couponAmount": 5000,
                                                    "pointAmount": 5000,
                                                    "orderProducts": [
                                                      {
                                                        "sellerId": 1,
                                                        "productId": 5,
                                                        "pricePolicyId": 159,
                                                        "sharerKey": null,
                                                        "quantity": 2,
                                                        "unitAmount": 50000,
                                                        "imageUrl": "https://marketnote.s3.amazonaws.com/product/30/1763534195922_image_600.png",
                                                        "orderStatus": "PAID",
                                                        "brandName": "브랜드명5",
                                                        "productName": "스프링노트123",
                                                        "selectedOptions": [
                                                          {
                                                            "id": 49,
                                                            "content": "30개입",
                                                            "status": "ACTIVE"
                                                          },
                                                          {
                                                            "id": 52,
                                                            "content": "4박스",
                                                            "status": "ACTIVE"
                                                          }
                                                        ],
                                                        "isReviewed": false
                                                      },
                                                      {
                                                        "sellerId": 1,
                                                        "productId": 6,
                                                        "pricePolicyId": 185,
                                                        "sharerKey": "550e8400-e29b-41d4-a716-446655440000",
                                                        "quantity": 10,
                                                        "unitAmount": 70000,
                                                        "imageUrl": "https://marketnote.s3.amazonaws.com/product/30/1763533916081_image_600.png",
                                                        "orderStatus": "PAID",
                                                        "brandName": "브랜드명6",
                                                        "productName": "스프링노트1234",
                                                        "selectedOptions": [
                                                          {
                                                            "id": 59,
                                                            "content": "1박스",
                                                            "status": "ACTIVE"
                                                          },
                                                          {
                                                            "id": 62,
                                                            "content": "60개입",
                                                            "status": "ACTIVE"
                                                          }
                                                        ],
                                                        "isReviewed": false
                                                      }
                                                    ]
                                                  }
                                                ]
                                              }
                                            ]
                                          },
                                          "message": "회원 주문 내역 조회 성공"
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
                                          "timestamp": "2026-01-06T12:12:30.013",
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
                                          "timestamp": "2026-01-06T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        })
public @interface GetOrdersApiDocs {
}
