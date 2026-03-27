package com.personal.marketnote.commerce.adapter.in.web.order.controller.apidocs;

import com.personal.marketnote.commerce.adapter.in.web.order.request.RegisterOrderRequest;
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
        summary = "주문 등록(결제하기 버튼)",
        description = """
                작성일자: 2026-01-05
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 주문을 등록합니다.
                
                - 결제하기 버튼 선택 시 호출합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | totalAmount | number | 총 주문 금액(원) | Y | 100000 |
                | couponAmount | number | 쿠폰 할인 금액(원) | N | 5000 |
                | pointAmount | number | 포인트 사용 금액(원) | N | 5000 |
                | shippingAddressId | number | 배송지 ID | Y | 1 |
                | requestMessage | string | 배송 요청사항 | N | "문 앞에 놓아주세요" |
                | orderProducts | array | 주문 상품 목록 | Y | [ ... ] |
                
                ---
                
                ### Request > orderProducts
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | productId | number | 상품 ID | Y | 245 |
                | sellerId | number | 판매자 회원 ID | Y | 1 |
                | pricePolicyId | number | 가격 정책 ID | Y | 1 |
                | sharerId | number | 링크 공유 회원 ID | N | 1 |
                | quantity | number | 주문 수량 | Y | 2 |
                | unitAmount | number | 단위 금액(원) | Y | 50000 |
                | imageUrl | string | 상품 이미지 URL | N | "https://marketnote.s3.amazonaws.com/product/30/1763534195922_image_600.png" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 201: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-05T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "주문 등록 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 생성된 주문 ID | 1 |
                | orderKey | string(uuid) | 주문 키 | "550e8400-e29b-41d4-a716-446655440000" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = RegisterOrderRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "totalAmount": 120000,
                                  "couponAmount": 5000,
                                  "pointAmount": 5000,
                                  "shippingAddressId": 1,
                                  "requestMessage": "문 앞에 놓아주세요",
                                  "orderProducts": [
                                    {
                                      "productId": 245,
                                      "sellerId": 1,
                                      "pricePolicyId": 23,
                                      "sharerId": 1,
                                      "quantity": 2,
                                      "unitAmount": 50000,
                                      "imageUrl": "https://marketnote.s3.amazonaws.com/product/30/1763534195922_image_600.png"
                                    },
                                    {
                                      "productId": 246,
                                      "sellerId": 2,
                                      "pricePolicyId": 14,
                                      "quantity": 10,
                                      "unitAmount": 70000,
                                      "imageUrl": "https://marketnote.s3.amazonaws.com/product/30/1763533916081_image_600.png"
                                    }
                                  ]
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "주문 등록 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 201,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-05T12:12:30.013",
                                          "content": {
                                            "id": 1,
                                            "orderKey": "550e8400-e29b-41d4-a716-446655440000"
                                          },
                                          "message": "주문 등록 성공"
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
public @interface RegisterOrderApiDocs {
}

