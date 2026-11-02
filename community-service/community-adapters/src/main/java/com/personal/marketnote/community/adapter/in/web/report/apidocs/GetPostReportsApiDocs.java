package com.personal.marketnote.community.adapter.in.web.report.apidocs;

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
        summary = "(관리자) 게시글 신고 내역 조회",
        description = """
                작성일자: 2026-01-13
                
                작성자: 성효빈
                
                게시글 신고 내역을 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | id | number | 게시글 ID | Y | 1 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-13T16:32:18.828188" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "게시글 신고 내역 조회 성공" |
                
                ---
                
                ### Response > content
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | reports | array | 신고 내역 목록 | [ ... ] |
                
                ---
                
                ### Response > content > reports
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | targetId | number | 게시글 ID | 30 |
                | reporterId | number | 신고자 ID | 17 |
                | reason | string | 신고 사유 | "부적절한 내용" |
                | createdAt | string(datetime) | 신고 일시 | "2026-01-13T17:39:04.638613" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "id",
                        in = ParameterIn.PATH,
                        required = true,
                        description = "게시글 ID",
                        schema = @Schema(type = "number", example = "1")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "게시글 신고 내역 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-13T18:04:09.630337",
                                          "content": {
                                            "reports": [
                                              {
                                                "targetId": 30,
                                                "reporterId": 17,
                                                "reason": "부적절한 내용",
                                                "createdAt": "2026-01-13T17:39:04.638613"
                                              }
                                            ]
                                          },
                                          "message": "게시글 신고 내역 조회 성공"
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
                                          "timestamp": "2026-01-13T16:32:18.828188",
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
                                          "timestamp": "2026-01-13T16:32:18.828188",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "대상 게시글 조회 실패",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 404,
                                          "code": "NOT_FOUND",
                                          "timestamp": "2026-01-13T23:20:05.915945975",
                                          "content": null,
                                          "message": "게시글을 찾을 수 없습니다. 전송된 게시글 ID: 1"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetPostReportsApiDocs {
}
