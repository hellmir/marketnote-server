package com.personal.marketnote.user.adapter.in.web.user.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
    summary = "초대 코드 존재 여부 조회",
    description = """
        작성일자: 2026-11-22

        작성자: 성효빈

        ---

        ## Description

        입력한 초대 코드에 해당하는 회원이 존재하는지 여부를 조회합니다.

        ---

        ## Response

        | **키** | **타입** | **설명** | **예시** |
        | --- | --- | --- | --- |
        | statusCode | number | 상태 코드 | 200: 성공 / 400: 형식 오류 / 401: 인증 실패 / 500: 그 외 |
        | code | string | 응답 코드 | "SUC01" / "ERR01" |
        | timestamp | string(datetime) | 응답 일시 | "2026-11-22T12:12:30.013" |
        | content | object | 응답 본문 | { ... } |
        | message | string | 처리 결과 | "초대 코드 존재 여부 조회 성공" |

        ---

        ### Response > content

        | **키** | **타입** | **설명** | **예시** |
        | --- | --- | --- | --- |
        | exists | boolean | 초대 코드 존재 여부 | true |
        """,
    security = {@SecurityRequirement(name = "bearer")},
    responses = {
        @ApiResponse(
            responseCode = "200",
            description = "초대 코드 존재 여부 조회 성공",
            content = @Content(
                examples = @ExampleObject("""
                    {
                      "statusCode": 200,
                      "code": "SUC01",
                      "timestamp": "2026-11-22T11:20:19.055291",
                      "content": {
                        "exists": true
                      },
                      "message": "초대 코드 존재 여부 조회 성공"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "초대 코드 형식 오류",
            content = @Content(
                examples = @ExampleObject("""
                    {
                      "statusCode": 400,
                      "code": "ERR01",
                      "timestamp": "2026-11-22T11:18:36.206993",
                      "content": null,
                      "message": "유효하지 않은 추천인 코드입니다."
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
                      "timestamp": "2026-11-22T11:19:36.206993",
                      "content": null,
                      "message": "Invalid token"
                    }
                    """)
            )
        )
    }
)
public @interface IsReferredUserCodeExistsApiDocs {
}
