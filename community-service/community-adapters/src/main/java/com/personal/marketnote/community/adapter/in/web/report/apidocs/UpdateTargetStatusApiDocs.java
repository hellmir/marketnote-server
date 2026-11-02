package com.personal.marketnote.community.adapter.in.web.report.apidocs;

import com.personal.marketnote.community.adapter.in.web.report.request.UpdateTargetStatusRequest;
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
        summary = "(관리자) 대상 리뷰/게시글 노출/숨기기",
        description = """
                작성일자: 2026-01-15
                
                작성자: 성효빈
                ---
                
                ## Description
                
                - 리뷰 또는 게시글을 숨기거나 복구합니다.
                
                - 관리자만 가능합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | targetType | string | 대상 유형(REVIEW/POST) | Y | "REVIEW" |
                | targetId | number | 대상 ID | Y | 10 |
                | isVisible | boolean | 노출/숨기기 여부 | Y | true |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 201 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-15T11:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "대상 노출/숨기기 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | isVisible | boolean | 변경된 노출 상태 | true |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = UpdateTargetStatusRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "targetType": "REVIEW",
                                  "targetId": 10,
                                  "isVisible": true
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "대상 노출/숨기기 성공(신규 생성)",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 201,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-15T11:43:58.303943",
                                          "content": {
                                            "isVisible": true
                                          },
                                          "message": "대상 노출/숨기기 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "200",
                        description = "대상 노출/숨기기 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-15T11:43:58.303943",
                                          "content": {
                                            "isVisible": false
                                          },
                                          "message": "대상 노출/숨기기 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface UpdateTargetStatusApiDocs {
}
