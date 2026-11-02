package com.personal.marketnote.community.adapter.in.web.like.controller.apidocs;

import com.personal.marketnote.community.adapter.in.web.like.request.UpsertLikeRequest;
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
        summary = "좋아요 등록/활성화/비활성화",
        description = """
                작성일자: 2026-01-10
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 대상에 좋아요를 등록하거나 활성화/비활성화합니다.
                
                - 최초 요청 시 데이터를 생성하며, 이후 요청 시 기존 데이터를 활성화/비활성화합니다.
                
                - 대상: 리뷰/게시글
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | targetType | string | 대상 유형(REVIEW/BOARD) | Y | "REVIEW" |
                | targetId | number | 대상 ID | Y | 10 |
                | isLiked | boolean | 좋아요 여부(좋아요/취소) | Y | true |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 201 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-10T11:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "좋아요 활성화/비활성화 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | isLiked | boolean | 변경된 좋아요 상태 | true |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = UpsertLikeRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "targetType": "REVIEW",
                                  "targetId": 10,
                                  "isLiked": true
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "좋아요 등록 성공(신규 생성)",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 201,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-10T11:43:58.303943",
                                          "content": {
                                            "isLiked": true
                                          },
                                          "message": "좋아요 등록 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "200",
                        description = "좋아요 활성화/비활성화 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-10T11:43:58.303943",
                                          "content": {
                                            "isLiked": false
                                          },
                                          "message": "좋아요 활성화/비활성화 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface UpsertLikeApiDocs {
}
