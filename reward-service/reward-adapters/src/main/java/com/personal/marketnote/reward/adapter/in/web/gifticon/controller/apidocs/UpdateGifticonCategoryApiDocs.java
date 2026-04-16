package com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs;

import com.personal.marketnote.reward.adapter.in.web.gifticon.request.UpdateGifticonCategoryRequest;
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
        summary = "(관리자) 기프티콘 카테고리 수정",
        description = """
                작성일자: 2026-04-05
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                기프티콘 카테고리의 표시명(displayName)과 아이콘 URL(iconUrl)을 수정합니다.
                전달된 필드만 업데이트됩니다.
                displayName을 빈 문자열("")로 전달하면 null로 초기화되어 원래 categoryName이 표시됩니다.
                
                ---
                
                ## Request Body
                
                | 키 | 타입 | 설명 | 필수 | 예시 |
                | --- | --- | --- | --- | --- |
                | displayName | string | 표시명 (빈 문자열이면 초기화) | N | "편의점/마트" |
                | iconUrl | string | 아이콘 URL | N | "https://..." |
                
                ---
                
                ## Response
                
                200 OK (content: null)
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = UpdateGifticonCategoryRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "displayName": "편의점/마트",
                                  "iconUrl": "https://cdn.example.com/icon.png"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "기프티콘 카테고리 수정 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-05T10:00:00.000",
                                          "content": null,
                                          "message": "기프티콘 카테고리 수정 성공"
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
                                          "timestamp": "2026-04-05T10:00:00.000",
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
                                          "timestamp": "2026-04-05T10:00:00.000",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        }
)
public @interface UpdateGifticonCategoryApiDocs {
}
