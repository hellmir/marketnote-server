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
        summary = "(관리자) 기프티콘 상품 사용자 판매가 관리",
        description = """
                작성일자: 2026-11-21

                작성자: 성효빈

                ---

                ## Description

                - 관리자가 기프티콘 상품의 사용자 판매가(cashPrice)를 직접 설정합니다.

                - 기프티쇼 동기화 시 벤더 salePrice가 초기값으로 들어가지만, 동기화 로직(syncFromApi)이 cashPrice를 건드리지 않아 관리자가 설정한 값이 유지됩니다.

                - cashPrice는 0 이상이어야 합니다.

                ---

                ## Request Body

                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | items | array | 현금가 관리 항목 목록 | Y | [ ... ] |
                | items[].goodsCode | string | 상품 코드 | Y | "G00001" |
                | items[].cashPrice | number | 사용자 판매가 (현금가, 0 이상) | Y | 2900 |

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-11-21T12:00:00.000" |
                | content | null | 응답 본문 | null |
                | message | string | 처리 결과 | "기프티콘 상품 사용자 판매가 관리 성공" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "기프티콘 상품 사용자 판매가 관리 성공",
                        content = @Content(
                                schema = @Schema(implementation = StringResponseSchema.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-11-21T12:00:00.000",
                                          "content": null,
                                          "message": "기프티콘 상품 사용자 판매가 관리 성공"
                                        }
                                        """)
                        )
                )
        })
public @interface ManageGifticonGoodsCashPriceApiDocs {
}
