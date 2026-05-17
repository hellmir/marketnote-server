package com.personal.marketnote.community.adapter.in.web.post.controller.apidocs;

import com.personal.marketnote.community.adapter.in.web.post.request.UpdatePostRequest;
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
        summary = "게시글 수정",
        description = """
                작성일자: 2026-01-15
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 게시글 제목/내용을 수정합니다.
                
                - 공지/이벤트, FAQ 게시판만 가능합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | id(path) | number | 게시글 ID | Y | 1 |
                | title | string | 제목 | Y | "게시글 제목" |
                | content | string | 내용 | Y | "게시글 내용" |
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 201 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-09T16:32:18.828188" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "게시글 수정 성공" |
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 수정된 게시글 ID | 3 |
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
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = UpdatePostRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "title": "게시글 제목",
                                  "content": "게시글 내용"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "게시글 수정 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-13T16:39:31.057206",
                                          "content": {
                                            "id": 3
                                          },
                                          "message": "게시글 수정 성공"
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
                                          "timestamp": "2026-01-03T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "수정할 수 없는 게시판",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 409,
                                          "code": "CONFLICT",
                                          "timestamp": "2026-01-15T16:35:05.895046",
                                          "content": null,
                                          "message": "수정할 수 없는 게시판입니다."
                                        }
                                        """)
                        )
                )
        }
)
public @interface UpdatePostApiDocs {
}
