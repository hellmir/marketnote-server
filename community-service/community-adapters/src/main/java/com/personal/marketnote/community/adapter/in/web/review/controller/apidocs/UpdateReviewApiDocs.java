package com.personal.marketnote.community.adapter.in.web.review.controller.apidocs;

import com.personal.marketnote.community.adapter.in.web.review.request.RegisterReviewRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
        summary = "리뷰 수정",
        description = """
                작성일자: 2026-01-12
                
                작성자: 성효빈
                
                리뷰를 수정합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | id | number | 리뷰 ID | Y | 1 |
                | rating | number | 리뷰 평점(0.0~5.0, 소수점 1자리) | Y | 4.5 |
                | content | string | 리뷰 내용(10자 이상) | Y | "배송이 빠르고 포장 상태도 좋았습니다." |
                | isPhoto | boolean | 포토 리뷰 여부 | Y | false |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-12T16:32:18.828188" |
                | content | object | 응답 본문 | null |
                | message | string | 처리 결과 | "리뷰 수정 성공" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "id",
                        in = ParameterIn.PATH,
                        required = true,
                        description = "리뷰 ID",
                        schema = @Schema(type = "number", example = "1")
                )
        },
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = RegisterReviewRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "rating": 1,
                                  "content": "배송이 느리고 포장 상태도 나빴습니다.",
                                  "isPhoto": false
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "리뷰 수정 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-12T16:32:18.828188",
                                          "content": null,
                                          "message": "리뷰 수정 성공"
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
                                          "timestamp": "2026-01-12T16:32:18.828188",
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
                                          "timestamp": "2026-01-12T16:32:18.828188",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "리뷰 작성자가 아님",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 409,
                                          "code": "CONFLICT",
                                          "timestamp": "2026-01-12T16:42:29.926731",
                                          "content": null,
                                          "message": "리뷰 작성자가 아닙니다. 리뷰 ID: 1, 회원 ID: 1"
                                        }
                                        """)
                        )
                )
        }
)
public @interface UpdateReviewApiDocs {
}
