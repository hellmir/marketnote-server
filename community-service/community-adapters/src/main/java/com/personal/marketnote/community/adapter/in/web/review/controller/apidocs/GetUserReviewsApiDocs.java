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
@SecurityRequirement(name = "bearer")
@Operation(
        summary = "(관리자) 회원 리뷰 내역 조회",
        description = """
                작성일자: 2026-04-01

                작성자: 성효빈

                - 관리자가 특정 회원의 리뷰 내역을 오프셋 페이지네이션으로 조회합니다.

                ---

                ## Request

                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | userId | number (path) | 회원 ID | Y | 1 |
                | page | number | 페이지 번호 (1부터 시작) | N (기본값: 1) | 1 |
                | page-size | number | 페이지 크기 | N (기본값: 10) | 10 |
                | sort-direction | string | 정렬 방향(DESC/ASC) | N (기본값: DESC) | "DESC" |
                | sort-property | string | 정렬 기준 | N (기본값: ID) | "ID" |

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-04-01T10:00:00" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "회원 리뷰 내역 조회 성공" |

                ### Response > content > reviews

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | page | number | 현재 페이지 | 1 |
                | pageSize | number | 페이지 크기 | 10 |
                | totalElements | number | 총 리뷰 수 | 25 |
                | totalPages | number | 총 페이지 수 | 3 |
                | items | array | 리뷰 목록 | [ ... ] |
                """,
        parameters = {
                @Parameter(
                        name = "userId",
                        in = ParameterIn.PATH,
                        required = true,
                        description = "회원 ID",
                        schema = @Schema(type = "number", example = "1")
                ),
                @Parameter(
                        name = "page",
                        in = ParameterIn.QUERY,
                        description = "페이지 번호 (1부터 시작)",
                        schema = @Schema(type = "number", example = "1", defaultValue = "1")
                ),
                @Parameter(
                        name = "page-size",
                        in = ParameterIn.QUERY,
                        description = "페이지 크기",
                        schema = @Schema(type = "number", example = "10", defaultValue = "10")
                ),
                @Parameter(
                        name = "sort-direction",
                        in = ParameterIn.QUERY,
                        description = "정렬 방향",
                        schema = @Schema(
                                type = "string",
                                example = "DESC",
                                allowableValues = {"ASC", "DESC"},
                                defaultValue = "DESC"
                        )
                ),
                @Parameter(
                        name = "sort-property",
                        in = ParameterIn.QUERY,
                        description = "정렬 기준",
                        schema = @Schema(
                                type = "string",
                                example = "ID",
                                allowableValues = {"ID", "LIKE", "RATING", "ORDER_NUM"},
                                defaultValue = "ID"
                        )
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "회원 리뷰 내역 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-01T10:00:00",
                                          "content": {
                                            "reviews": {
                                              "page": 1,
                                              "pageSize": 10,
                                              "totalElements": 2,
                                              "totalPages": 1,
                                              "items": [
                                                {
                                                  "id": 10,
                                                  "reviewerId": 1,
                                                  "orderId": 100,
                                                  "productId": 50,
                                                  "pricePolicyId": 30,
                                                  "productImageUrl": "https://s3/product.jpg",
                                                  "selectedOptions": "색상: 블랙 / 사이즈: M",
                                                  "quantity": 1,
                                                  "reviewerName": "홍*동",
                                                  "rating": 5.0,
                                                  "content": "좋은 상품입니다",
                                                  "isPhoto": true,
                                                  "images": [],
                                                  "isEdited": false,
                                                  "likeCount": 3,
                                                  "isUserLiked": false,
                                                  "status": "ACTIVE",
                                                  "createdAt": "2026-03-29T10:00:00",
                                                  "modifiedAt": "2026-03-29T10:00:00",
                                                  "orderNum": 10,
                                                  "product": {
                                                    "productId": 50,
                                                    "productName": "테스트 상품",
                                                    "pricePolicyName": "기본 옵션"
                                                  }
                                                }
                                              ]
                                            }
                                          },
                                          "message": "회원 리뷰 내역 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetUserReviewsApiDocs {
}
