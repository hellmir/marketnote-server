package com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs;

import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetGifticonBrandsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
        summary = "기프티콘 브랜드 목록 조회",
        description = """
                작성일자: 2026-04-05
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                특정 카테고리에 노출된 판매 중 상품이 있는 브랜드 목록을 조회합니다.
                """,
        parameters = {
                @Parameter(name = "category-code", description = "카테고리 코드", required = true, example = "1")
        },
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "기프티콘 브랜드 목록 조회 성공",
                        content = @Content(
                                schema = @Schema(implementation = GetGifticonBrandsResponse.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-05T10:00:00.000000",
                                          "content": {
                                            "brands": [
                                              {
                                                "brandCode": "BR001",
                                                "brandName": "스타벅스",
                                                "brandImageUrl": "https://img.com/starbucks.png"
                                              },
                                              {
                                                "brandCode": "BR002",
                                                "brandName": "이디야",
                                                "brandImageUrl": "https://img.com/ediya.png"
                                              }
                                            ]
                                          },
                                          "message": "기프티콘 브랜드 목록 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetGifticonBrandsApiDocs {
}
