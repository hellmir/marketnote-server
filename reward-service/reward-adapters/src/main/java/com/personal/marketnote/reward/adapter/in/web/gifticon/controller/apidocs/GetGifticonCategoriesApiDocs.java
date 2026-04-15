package com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs;

import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetGifticonCategoriesResponse;
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
        summary = "기프티콘 카테고리 목록 조회",
        description = """
                작성일자: 2026-04-05

                작성자: 성효빈

                ---

                ## Description

                노출 설정된 기프티콘 카테고리 목록을 orderNum 오름차순으로 조회합니다.
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "기프티콘 카테고리 목록 조회 성공",
                        content = @Content(
                                schema = @Schema(implementation = GetGifticonCategoriesResponse.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-05T10:00:00.000000",
                                          "content": {
                                            "categories": [
                                              {
                                                "categoryCode": "1",
                                                "displayName": "커피",
                                                "iconUrl": "https://img.com/coffee.png",
                                                "orderNum": 1
                                              },
                                              {
                                                "categoryCode": "2",
                                                "displayName": "베이커리",
                                                "iconUrl": "https://img.com/bakery.png",
                                                "orderNum": 2
                                              }
                                            ]
                                          },
                                          "message": "기프티콘 카테고리 목록 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetGifticonCategoriesApiDocs {
}
