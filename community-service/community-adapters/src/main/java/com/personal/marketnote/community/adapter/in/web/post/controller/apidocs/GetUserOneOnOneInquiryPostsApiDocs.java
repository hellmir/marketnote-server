package com.personal.marketnote.community.adapter.in.web.post.controller.apidocs;

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
        summary = "(관리자) 회원 1:1 문의 내역 조회",
        description = """
                작성일자: 2026-04-01

                작성자: 성효빈

                - 관리자가 특정 회원의 1:1 문의 내역을 오프셋 페이지네이션으로 조회합니다.

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
                | message | string | 처리 결과 | "회원 1:1 문의 내역 조회 성공" |

                ### Response > content > posts

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | page | number | 현재 페이지 | 1 |
                | pageSize | number | 페이지 크기 | 10 |
                | totalElements | number | 총 게시글 수 | 25 |
                | totalPages | number | 총 페이지 수 | 3 |
                | items | array | 게시글 목록 | [ ... ] |
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
                                allowableValues = {"ID", "IS_ANSWERED"},
                                defaultValue = "ID"
                        )
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "회원 1:1 문의 내역 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-01T10:00:00",
                                          "content": {
                                            "posts": {
                                              "page": 1,
                                              "pageSize": 10,
                                              "totalElements": 2,
                                              "totalPages": 1,
                                              "items": [
                                                {
                                                  "id": 10,
                                                  "userId": 1,
                                                  "parentId": null,
                                                  "board": "ONE_ON_ONE_INQUERY",
                                                  "category": "ORDER_PAYMENT",
                                                  "targetType": null,
                                                  "targetId": null,
                                                  "productImageUrl": null,
                                                  "writerName": "홍*동",
                                                  "writerMaskedName": "홍*동",
                                                  "title": "1:1 문의 제목",
                                                  "content": "1:1 문의 내용",
                                                  "isPrivate": true,
                                                  "isPhoto": false,
                                                  "images": null,
                                                  "isMasked": false,
                                                  "isAnswered": true,
                                                  "createdAt": "2026-03-29T10:00:00",
                                                  "modifiedAt": "2026-03-29T10:00:00",
                                                  "product": null,
                                                  "replies": [
                                                    {
                                                      "id": 11,
                                                      "userId": 4,
                                                      "parentId": 10,
                                                      "board": "ONE_ON_ONE_INQUERY",
                                                      "category": "ORDER_PAYMENT",
                                                      "targetType": null,
                                                      "targetId": null,
                                                      "productImageUrl": null,
                                                      "writerName": "관리자",
                                                      "writerMaskedName": "관*자",
                                                      "title": "답변 제목",
                                                      "content": "답변 내용",
                                                      "isPrivate": false,
                                                      "isPhoto": false,
                                                      "images": null,
                                                      "isMasked": false,
                                                      "isAnswered": false,
                                                      "createdAt": "2026-03-29T11:00:00",
                                                      "modifiedAt": "2026-03-29T11:00:00",
                                                      "product": null,
                                                      "replies": []
                                                    }
                                                  ]
                                                }
                                              ]
                                            }
                                          },
                                          "message": "회원 1:1 문의 내역 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetUserOneOnOneInquiryPostsApiDocs {
}
