package com.personal.marketnote.product.adapter.in.web.cart.controller.apidocs;

import com.personal.marketnote.product.adapter.in.web.cart.request.GetMyOrderingProductsRequest;
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
        summary = "회원 주문 대기 상품 목록 조회(상품 구매하기 버튼)",
        description = """
                작성일자: 2026-01-05
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 회원의 주문 대기 상품 목록을 조회합니다.
                
                - 주문 상세 페이지 또는 장바구니 페이지에서 구매 버튼 선택 시 호출합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | orderingItemRequests | array | 주문 대기 상품 목록 | Y | [ ... ] |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-05T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "회원 주문 대기 상품 목록 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | orderingProducts | array | 주문 대기 상품 목록 | [ ... ] |
                
                ---
                
                ### Response > content > orderingProducts
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | product | object | 상품 | { ... } |
                | pricePolicy | object | 가격 정책 | { ... } |
                | stock | number | 재고 수량 | 10 |
                | quantity | number | 상품 수량 | 1 |
                | sharerKey | string(uuid) | 링크 공유 회원 식별키 | "550e8400-e29b-41d4-a716-446655440000" |
                
                ---
                
                ### Response > content > orderingProducts > product
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 상품 ID | 1 |
                | sellerId | number | 판매자 ID | 1 |
                | name | string | 상품명 | "건기식테스트1" |
                | brandName | string | 브랜드명 | "노트왕" |
                | imageUrl | string | 상품 이미지 URL | "https://marketnote.s3.amazonaws.com/product/30/1763534195922_image_600.png" |
                | status | string | 상태 | "ACTIVE" |
                
                ---
                
                ### Response > content > orderingProducts > pricePolicy
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 가격 정책 ID | 1 |
                | price | number | 기본 판매 가격(원) | 40000 |
                | discountPrice | number | 할인 판매 가격(원) | 32000 |
                | discountRate | number | 할인율(%, 최대 소수점 1자리) | 20 |
                | accumulatedPoint | number | 구매 시 적립 포인트 | 800 |
                | options | array | 옵션 목록 | [ ... ] |
                
                ---
                
                ### Response > content > orderingProducts > pricePolicy > options
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 옵션 ID | 1 |
                | content | string | 옵션 내용 | "하" |
                | status | string | 옵션 상태 | "ACTIVE" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = GetMyOrderingProductsRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "orderingItemRequests": [
                                    {
                                      "pricePolicyId": 20,
                                      "sharerKey": "550e8400-e29b-41d4-a716-446655440000",
                                      "quantity": 1,
                                      "imageUrl": "https://marketnote.s3.amazonaws.com/product/30/1763533914954_image_600.png"
                                    },
                                    {
                                      "pricePolicyId": 21,
                                      "sharerKey": null,
                                      "quantity": 10,
                                      "imageUrl": "https://marketnote.s3.amazonaws.com/product/30/1763533916081_image_600.png"
                                    },
                                    {
                                      "pricePolicyId": 23,
                                      "sharerKey": null,
                                      "quantity": 5,
                                      "imageUrl": "https://marketnote.s3.amazonaws.com/product/30/1763534195922_image_600.png"
                                    }
                                  ]
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "회원 주문 대기 상품 목록 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-05T11:19:19.771364",
                                          "content": {
                                            "orderingProducts": [
                                              {
                                                "product": {
                                                  "id": 30,
                                                  "sellerId": 1,
                                                  "name": "건기식테스트1",
                                                  "brandName": "노트왕",
                                                  "imageUrl": "https://marketnote.s3.amazonaws.com/product/30/1763533914954_image_600.png",
                                                  "status": "INACTIVE"
                                                },
                                                "pricePolicy": {
                                                  "id": 20,
                                                  "price": 45000,
                                                  "discountPrice": 37000,
                                                  "discountRate": 17.8,
                                                  "accumulatedPoint": 1200,
                                                  "options": [
                                                    {
                                                      "id": 8,
                                                      "content": "1박스",
                                                      "status": "INACTIVE"
                                                    },
                                                    {
                                                      "id": 11,
                                                      "content": "60개입",
                                                      "status": "INACTIVE"
                                                    }
                                                  ]
                                                },
                                                "stock": 2000,
                                                "quantity": 1,
                                                "sharerKey": "550e8400-e29b-41d4-a716-446655440000"
                                              },
                                              {
                                                "product": {
                                                  "id": 30,
                                                  "sellerId": 1,
                                                  "name": "건기식테스트1",
                                                  "brandName": "노트왕",
                                                  "imageUrl": "https://marketnote.s3.amazonaws.com/product/30/1763533916081_image_600.png",
                                                  "status": "INACTIVE"
                                                },
                                                "pricePolicy": {
                                                  "id": 21,
                                                  "price": 40000,
                                                  "discountPrice": 32000,
                                                  "discountRate": 20,
                                                  "accumulatedPoint": 800,
                                                  "options": [
                                                    {
                                                      "id": 8,
                                                      "content": "1박스",
                                                      "status": "INACTIVE"
                                                    },
                                                    {
                                                      "id": 10,
                                                      "content": "30개입",
                                                      "status": "INACTIVE"
                                                    }
                                                  ]
                                                },
                                                "stock": 2100,
                                                "quantity": 10,
                                                "sharerKey": null
                                              },
                                              {
                                                "product": {
                                                  "id": 30,
                                                  "sellerId": 1,
                                                  "name": "건기식테스트1",
                                                  "brandName": "노트왕",
                                                  "imageUrl": "https://marketnote.s3.amazonaws.com/product/30/1763534195922_image_600.png",
                                                  "status": "INACTIVE"
                                                },
                                                "pricePolicy": {
                                                  "id": 23,
                                                  "price": 43000,
                                                  "discountPrice": 36000,
                                                  "discountRate": 16.3,
                                                  "accumulatedPoint": 500,
                                                  "options": [
                                                    {
                                                      "id": 9,
                                                      "content": "3박스",
                                                      "status": "INACTIVE"
                                                    },
                                                    {
                                                      "id": 10,
                                                      "content": "30개입",
                                                      "status": "INACTIVE"
                                                    }
                                                  ]
                                                },
                                                "stock": 2298,
                                                "quantity": 5,
                                                "sharerKey": null
                                              }
                                            ]
                                          },
                                          "message": "회원 주문 대기 상품 목록 조회 성공"
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
                                          "timestamp": "2025-12-30T12:12:30.013",
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
                                          "timestamp": "2025-12-30T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        })
public @interface GetMyOrderingProductsApiDocs {
}
