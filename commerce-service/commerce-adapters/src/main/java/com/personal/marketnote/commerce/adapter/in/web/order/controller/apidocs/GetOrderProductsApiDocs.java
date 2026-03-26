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
        summary = "나의 주문 상품 목록 조회",
        description = """
                작성일자: 2026-01-31
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 나의 주문 상품 목록을 조회합니다.
                
                - isReviewed 값이 null이면 전체 주문 상품을 반환합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | isReviewed | boolean | 리뷰 작성 여부 | N | false |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 201: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-31T10:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "나의 주문 상품 목록 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | orderProducts | array | 주문 상품 목록 | [ ... ] |
                
                ---
                
                ### Response > content > orderProducts
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | orderId | number | 주문 ID | 1 |
                | orderNumber | string | 주문 번호 | "2026011200001" |
                | orderDate | string(date) | 주문 생성일(일 단위) | "2026-01-31" |
                | orderStatus | string | 주문 상태 | "DELIVERED" |
                | sellerId | number | 판매자 회원 ID | 1 |
                | productId | number | 상품 ID | 1 |
                | pricePolicyId | number | 가격 정책 ID | 1 |
                | sharerId | number | 링크 공유 회원 ID | 1 |
                | quantity | number | 주문 수량 | 2 |
                | unitAmount | number | 단위 금액(원) | 50000 |
                | imageUrl | string | 상품 이미지 URL | "https://marketnote.s3.amazonaws.com/product/30/1763534195922_image_600.png" |
                | brandName | string | 브랜드명 | "브랜드명1" |
                | productName | string | 상품명 | "공책" |
                | selectedOptions | array | 선택 옵션 목록 | [ ... ] |
                | isReviewed | boolean | 리뷰 작성 여부 | false |
                | reviewDeadline | string(datetime) | 리뷰 작성기한 (구매 확정 후 30일) | "2026-02-25T14:30:00" |
                
                ---
                
                ### Response > content > orderProducts > selectedOptions
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 옵션 ID | 1 |
                | content | string | 옵션 내용 | "1박스" |
                | status | string | 상태 | "ACTIVE" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "isReviewed",
                        description = "리뷰 작성 여부",
                        in = ParameterIn.QUERY,
                        required = false,
                        schema = @Schema(type = "boolean", example = "false")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "나의 주문 상품 목록 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-31T10:00:00.000",
                                          "content": {
                                            "orderProducts": [
                                              {
                                                "orderId": 15,
                                                "orderNumber": "2026011200001",
                                                "orderDate": "2026-01-31",
                                                "orderStatus": "DELIVERED",
                                                "sellerId": 12,
                                                "productId": 1,
                                                "pricePolicyId": 166,
                                                "sharerId": 1,
                                                "quantity": 2,
                                                "unitAmount": 50000,
                                                "imageUrl": "https://marketnote.s3.amazonaws.com/product/30/1763534195922_image_600.png",
                                                "brandName": "브랜드명1",
                                                "productName": "공책",
                                                "selectedOptions": [
                                                  {
                                                    "id": 55,
                                                    "content": "4박스",
                                                    "status": "ACTIVE"
                                                  }
                                                ],
                                                "isReviewed": false,
                                                "reviewDeadline": "2026-02-25T14:30:00"
                                              }
                                            ]
                                          },
                                          "message": "나의 주문 상품 목록 조회 성공"
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
                                          "timestamp": "2026-01-31T10:00:00.000",
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
                                          "timestamp": "2026-01-31T10:00:00.000",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetOrderProductsApiDocs {
}
