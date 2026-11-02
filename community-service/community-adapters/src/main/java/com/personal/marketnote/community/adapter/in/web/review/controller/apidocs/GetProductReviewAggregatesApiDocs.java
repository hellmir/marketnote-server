package com.personal.marketnote.community.adapter.in.web.review.controller.apidocs;

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
        summary = "(관리자) 상품 리뷰 집계 목록 조회",
        description = """
                작성일자: 2026-01-31
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                상품 ID 목록으로 상품 리뷰 평점 평균과 총 리뷰 개수 목록을 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | productIds | array<number> | 상품 ID 목록 | Y | [1, 2, 3] |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-31T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "상품 리뷰 집계 목록 조회 성공" |
                
                ---
                
                ### Response > content
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | reviewAggregates | array | 상품 리뷰 집계 목록 | [ ... ] |
                
                ---
                
                ### Response > content > reviewAggregates
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | productId | number | 상품 ID | 1 |
                | totalCount | number | 총 리뷰 개수 | 8 |
                | averageRating | number(소수점 2자리) | 평점 평균 (scale=2, 0.00~9.99) | 4.50 |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "productIds",
                        description = "상품 ID 목록",
                        in = ParameterIn.QUERY,
                        required = true,
                        schema = @Schema(type = "array", example = "[1, 2, 3]")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "상품 리뷰 집계 목록 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-31T14:32:29.318827",
                                          "content": {
                                            "reviewAggregates": [
                                              {
                                                "productId": 22,
                                                "totalCount": 2,
                                                "averageRating": 3.00
                                              },
                                              {
                                                "productId": 55,
                                                "totalCount": 7,
                                                "averageRating": 3.43
                                              },
                                              {
                                                "productId": 77,
                                                "totalCount": 5,
                                                "averageRating": 3.60
                                              }
                                            ]
                                          },
                                          "message": "상품 리뷰 집계 목록 조회 성공"
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
                                          "timestamp": "2026-01-31T12:12:30.013",
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
                                          "timestamp": "2026-01-31T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetProductReviewAggregatesApiDocs {
}
