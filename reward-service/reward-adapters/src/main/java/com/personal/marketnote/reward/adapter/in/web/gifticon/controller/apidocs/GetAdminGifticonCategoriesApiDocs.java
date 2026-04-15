package com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs;

import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetAdminGifticonCategoriesResponse;
import io.swagger.v3.oas.annotations.Operation;
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
        summary = "(관리자) 기프티콘 카테고리 목록 조회",
        description = """
                작성일자: 2026-04-05

                작성자: 성효빈

                ---

                ## Description

                관리자가 전체 기프티콘 카테고리 목록을 조회합니다.

                ---

                ## Response

                | 키 | 타입 | 설명 | 예시 |
                | --- | --- | --- | --- |
                | categories[].id | number | 카테고리 ID | 1 |
                | categories[].categoryCode | string | 카테고리 코드 | "1" |
                | categories[].categoryName | string | 카테고리 원본명 | "커피" |
                | categories[].displayName | string | 관리자 설정 표시명 (null 가능) | "편의점/마트" |
                | categories[].effectiveDisplayName | string | 실제 표시명 (displayName 우선, 없으면 categoryName) | "커피" |
                | categories[].iconUrl | string | 아이콘 URL (null 가능) | "https://..." |
                | categories[].exposed | boolean | 노출 여부 | true |
                | categories[].orderNum | number | 노출 순서 (null 가능) | 1 |
                | categories[].createdAt | string(datetime) | 생성일시 | "2026-04-05T10:00:00" |
                | categories[].modifiedAt | string(datetime) | 수정일시 | "2026-04-05T10:00:00" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "기프티콘 카테고리 목록 조회 성공",
                        content = @Content(
                                schema = @Schema(implementation = GetAdminGifticonCategoriesResponse.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-05T10:00:00.000",
                                          "content": {
                                            "categories": [
                                              {
                                                "id": 1,
                                                "categoryCode": "1",
                                                "categoryName": "커피",
                                                "displayName": null,
                                                "effectiveDisplayName": "커피",
                                                "iconUrl": null,
                                                "exposed": true,
                                                "orderNum": 1,
                                                "createdAt": "2026-04-05T10:00:00",
                                                "modifiedAt": "2026-04-05T10:00:00"
                                              }
                                            ]
                                          },
                                          "message": "기프티콘 카테고리 목록 조회 성공"
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
public @interface GetAdminGifticonCategoriesApiDocs {
}
