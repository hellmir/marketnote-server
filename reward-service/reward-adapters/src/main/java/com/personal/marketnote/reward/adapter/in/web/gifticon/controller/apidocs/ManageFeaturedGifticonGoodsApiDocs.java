package com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs;

import com.personal.marketnote.common.adapter.in.api.schema.StringResponseSchema;
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
        summary = "(관리자) 기프티콘 인기상품 관리",
        description = """
                작성일자: 2026-04-06

                작성자: 성효빈

                ---

                ## Description

                - 관리자가 기프티콘 상품의 인기상품 여부와 정렬 순서를 관리합니다.

                - 노출(exposed=true) 상태인 상품만 인기상품으로 설정 가능합니다.

                - 인기상품은 최대 10개까지 설정 가능합니다.

                ---

                ## Request Body

                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | items | array | 인기상품 관리 항목 목록 (최대 10개) | Y | [ ... ] |
                | items[].goodsCode | string | 상품 코드 | Y | "G00001" |
                | items[].popular | boolean | 인기상품 여부 | Y | true |
                | items[].popularOrderNum | number | 인기상품 정렬 순서 | N | 1 |

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-04-06T12:00:00.000" |
                | content | null | 응답 본문 | null |
                | message | string | 처리 결과 | "기프티콘 인기상품 관리 성공" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "기프티콘 인기상품 관리 성공",
                        content = @Content(
                                schema = @Schema(implementation = StringResponseSchema.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-06T12:00:00.000",
                                          "content": null,
                                          "message": "기프티콘 인기상품 관리 성공"
                                        }
                                        """)
                        )
                )
        })
public @interface ManageFeaturedGifticonGoodsApiDocs {
}
