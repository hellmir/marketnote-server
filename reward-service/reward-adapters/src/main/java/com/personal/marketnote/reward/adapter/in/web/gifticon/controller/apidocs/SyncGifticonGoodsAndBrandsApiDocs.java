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
    summary = "(관리자) 기프티콘 상품/브랜드 수동 동기화",
    description = """
        작성일자: 2026-11-22

        작성자: 성효빈

        ---

        ## Description

        - 관리자가 기프티쇼 벤더에서 제공하는 기프티콘 상품/브랜드/카테고리 정보를 즉시 동기화합니다.

        - 정기 스케줄러(월/수/금 03:00, KST)와 동일한 동기화 로직을 트리거하며, 외부 벤더 결함이나 인증 결함이 정정된 직후 격일 회복 사이클을 기다리지 않고 즉시 검증할 수 있도록 합니다.

        - 응답은 동기화 호출 자체가 정상 진입했음을 의미하며, 적재 결과는 서버 로그/메트릭으로 확인합니다.

        ---

        ## Request

        요청 본문 없음.

        ---

        ## Response

        | **키** | **타입** | **설명** | **예시** |
        | --- | --- | --- | --- |
        | statusCode | number | 상태 코드 | 200 |
        | code | string | 응답 코드 | "SUC01" |
        | timestamp | string(datetime) | 응답 일시 | "2026-11-22T12:00:00.000" |
        | content | null | 응답 본문 | null |
        | message | string | 처리 결과 | "기프티콘 상품/브랜드 수동 동기화 성공" |
        """,
    security = {@SecurityRequirement(name = "bearer")},
    responses = {
        @ApiResponse(
            responseCode = "200",
            description = "기프티콘 상품/브랜드 수동 동기화 성공",
            content = @Content(
                schema = @Schema(implementation = StringResponseSchema.class),
                examples = @ExampleObject("""
                    {
                      "statusCode": 200,
                      "code": "SUC01",
                      "timestamp": "2026-11-22T12:00:00.000",
                      "content": null,
                      "message": "기프티콘 상품/브랜드 수동 동기화 성공"
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
                      "timestamp": "2026-11-22T12:00:00.000",
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
                      "timestamp": "2026-11-22T12:00:00.000",
                      "content": null,
                      "message": "Access Denied"
                    }
                    """)
            )
        )
    })
public @interface SyncGifticonGoodsAndBrandsApiDocs {
}
