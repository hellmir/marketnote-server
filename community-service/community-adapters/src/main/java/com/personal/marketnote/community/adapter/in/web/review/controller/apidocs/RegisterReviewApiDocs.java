package com.personal.marketnote.community.adapter.in.web.review.controller.apidocs;

import com.personal.marketnote.community.adapter.in.web.review.request.RegisterReviewRequest;
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
        summary = "리뷰 등록",
        description = """
                작성일자: 2026-01-09
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 주문 상품에 대한 리뷰를 등록합니다.
                
                - 평점은 1 이상 5 이하의 정수만 가능합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | orderId | number | 주문 ID | Y | 1 |
                | productId | number | 상품 ID | Y | 1 |
                | pricePolicyId | number | 가격 정책 ID | Y | 11 |
                | productImageUrl | string | 상품 이미지 URL | N | "https://example.com/image.jpg" |
                | selectedOptions | string | 선택된 옵션 목록 | N | "30개입, 5박스" |
                | quantity | number | 주문 수량 | Y | 2 |
                | reviewerName | string | 리뷰 작성자 이름 | Y | "홍길동" |
                | rating | number | 리뷰 평점(0.0~5.0, 소수점 1자리) | Y | 4.5 |
                | content | string | 리뷰 내용(10자 이상) | Y | "배송이 빠르고 포장 상태도 좋았습니다." |
                | isPhoto | boolean | 포토 리뷰 여부 | Y | false |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 201 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-09T16:32:18.828188" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "리뷰 등록 성공" |
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 생성된 리뷰 ID | 3 |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = RegisterReviewRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "orderId": 1,
                                  "productId": 1,
                                  "pricePolicyId": 11,
                                  "productImageUrl": "https://example.com/image.jpg",
                                  "selectedOptions": "30개입, 5박스",
                                  "quantity": 2,
                                  "reviewerName": "홍길동",
                                  "rating": 5,
                                  "content": "배송이 빠르고 포장 상태도 좋았습니다.",
                                  "isPhoto": false
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "리뷰 등록 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 201,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-09T16:32:18.828188",
                                          "content": {
                                            "id": 3
                                          },
                                          "message": "리뷰 등록 성공"
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
                                          "timestamp": "2026-01-09T16:32:18.828188",
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
                                          "timestamp": "2026-01-09T16:32:18.828188",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "이미 리뷰가 등록된 주문",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 409,
                                          "code": "CONFLICT",
                                          "timestamp": "2026-01-09T16:42:29.926731",
                                          "content": null,
                                          "message": "이미 해당 주문에 대한 리뷰가 존재합니다. 전송된 주문 ID: 1, 가격 정책 ID: 11"
                                        }
                                        """)
                        )
                )
        }
)
public @interface RegisterReviewApiDocs {
}
