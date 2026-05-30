package com.personal.marketnote.product.adapter.in.web.cart.controller.apidocs;

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
        summary = "장바구니 상품 목록 조회",
        description = """
                작성일자: 2026-01-04
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                회원의 장바구니 상품 목록을 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-04T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "회원 장바구니 상품 목록 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | cartProducts | array | 장바구니 상품 목록 | [ ... ] |
                
                ---
                
                ### Response > content > cartProducts
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | product | object | 상품 | { ... } |
                | pricePolicy | object | 가격 정책 | { ... } |
                | stock | number | 재고 수량 | 10 |
                | quantity | number | 상품 수량 | 1 |
                | sharerKey | string(uuid) | 링크 공유 회원 식별키 | "550e8400-e29b-41d4-a716-446655440000" |
                
                ---
                
                ### Response > content > cartProducts > product
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 상품 ID | 1 |
                | sellerId | number | 판매자 ID | 12 |
                | name | string | 상품명 | "건기식테스트1" |
                | brandName | string | 브랜드명 | "노트왕" |
                | imageUrl | string | 상품 이미지 URL | "https://marketnote.s3.amazonaws.com/product/30/1763534195922_image_600.png" |
                | status | string | 상태 | "ACTIVE" |
                
                ---
                
                ### Response > content > cartProducts > pricePolicy
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 가격 정책 ID | 1 |
                | price | number | 기본 판매 가격(원) | 40000 |
                | discountPrice | number | 할인 판매 가격(원) | 32000 |
                | discountRate | number | 할인율(%, 최대 소수점 1자리) | 20 |
                | accumulatedPoint | number | 구매 시 적립 포인트 | 800 |
                | options | array | 옵션 목록 | [ ... ] |
                
                ---
                
                ### Response > content > cartProducts > pricePolicy > options
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 옵션 ID | 1 |
                | content | string | 옵션 내용 | "하" |
                | status | string | 옵션 상태 | "ACTIVE" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "회원 장바구니 상품 목록 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-04T11:01:58.842088",
                                          "content": {
                                            "cartProducts": [
                                              {
                                                "product": {
                                                  "id": 40,
                                                  "sellerId": 12,
                                                  "name": "스프링노트5",
                                                  "brandName": "노트왕",
                                                  "imageUrl": "https://marketnote.s3.amazonaws.com/product/30/1763534195922_image_600.png",
                                                  "status": "ACTIVE"
                                                },
                                                "pricePolicy": {
                                                  "id": 114,
                                                  "price": 100000,
                                                  "discountPrice": 50000,
                                                  "discountRate": 60,
                                                  "accumulatedPoint": 30000,
                                                  "options": [
                                                    {
                                                      "id": 34,
                                                      "content": "하",
                                                      "status": "ACTIVE"
                                                    },
                                                    {
                                                      "id": 40,
                                                      "content": "5박스",
                                                      "status": "ACTIVE"
                                                    }
                                                  ]
                                                },
                                                "stock": 0,
                                                "quantity": 10,
                                                "sharerKey": "550e8400-e29b-41d4-a716-446655440000"
                                              },
                                              {
                                                "product": {
                                                  "id": 38,
                                                  "sellerId": 12,
                                                  "name": "스프링노트2",
                                                  "brandName": "노트킹",
                                                  "imageUrl": "https://marketnote.s3.amazonaws.com/product/30/1763534195922_image_600.png",
                                                  "status": "ACTIVE"
                                                },
                                                "pricePolicy": {
                                                  "id": 58,
                                                  "price": 20000,
                                                  "discountPrice": 15000,
                                                  "discountRate": 20,
                                                  "accumulatedPoint": 3000,
                                                  "options": [
                                                    {
                                                      "id": 28,
                                                      "content": "60개입",
                                                      "status": "ACTIVE"
                                                    }
                                                  ]
                                                },
                                                "stock": 0,
                                                "quantity": 5,
                                                "sharerKey": "550e8400-e29b-41d4-a716-446655440000"
                                              },
                                              {
                                                "product": {
                                                  "id": 40,
                                                  "sellerId": 12,
                                                  "name": "스프링노트5",
                                                  "brandName": "노트왕",
                                                  "imageUrl": "https://marketnote.s3.amazonaws.com/product/30/1763534195922_image_600.png",
                                                  "status": "ACTIVE"
                                                },
                                                "pricePolicy": {
                                                  "id": 105,
                                                  "price": 10000,
                                                  "discountPrice": 9000,
                                                  "discountRate": 11.1,
                                                  "accumulatedPoint": 1000,
                                                  "options": [
                                                    {
                                                      "id": 32,
                                                      "content": "상",
                                                      "status": "ACTIVE"
                                                    },
                                                    {
                                                      "id": 38,
                                                      "content": "1박스",
                                                      "status": "ACTIVE"
                                                    }
                                                  ]
                                                },
                                                "stock": 0,
                                                "quantity": 7,
                                                "sharerKey": null
                                              }
                                            ]
                                          },
                                          "message": "회원 장바구니 상품 목록 조회 성공"
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
public @interface GetMyCartProductsApiDocs {
}
