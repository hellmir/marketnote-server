package com.personal.marketnote.community.adapter.in.web.review.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "(비회원) 상품 리뷰 평점 평균 및 점수별 개수 현황 조회",
        description = """
                작성일자: 2026-01-10
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                상품 리뷰 평점 평균 및 점수별 개수 현황을 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | productId | number | 상품 ID | Y | 1 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 201 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-10T16:32:18.828188" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "상품 리뷰 평점 평균 및 점수별 개수 현황 조회 성공" |
                
                ---
                
                ### Response > content
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | totalCount | number | 총 리뷰 개수 | 100 |
                | fivePointCount | number | 5점 리뷰 개수 | 10 |
                | fourPointCount | number | 4점 리뷰 개수 | 10 |
                | threePointCount | number | 3점 리뷰 개수 | 10 |
                | twoPointCount | number | 2점 리뷰 개수 | 10 |
                | onePointCount | number | 1점 리뷰 개수 | 10 |
                | averageRating | number(소수점 2자리) | 평점 평균 (scale=2, 0.00~9.99) | 4.50 |
                """,
        parameters = {
                @Parameter(
                        name = "productId",
                        in = ParameterIn.PATH,
                        description = "상품 ID",
                        schema = @Schema(type = "number")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "상품 리뷰 목록 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-10T15:51:58.377507",
                                          "content": {
                                            "totalCount": 5,
                                            "fivePointCount": 2,
                                            "fourPointCount": 1,
                                            "threePointCount": 1,
                                            "twoPointCount": 0,
                                            "onePointCount": 1,
                                            "averageRating": 3.60
                                          },
                                          "message": "상품 리뷰 평점 평균 및 점수별 개수 현황 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetProductReviewAggregateApiDocs {
}
