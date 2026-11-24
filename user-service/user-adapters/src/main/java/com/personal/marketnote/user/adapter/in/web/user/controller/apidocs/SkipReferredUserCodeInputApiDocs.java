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
        summary = "추천 회원 초대 코드 입력 건너뛰기",
        description = """
                작성일자: 2026-11-22

                작성자: 성효빈

                ---

                ## Description

                회원 가입 직후 초대 코드 입력 화면에서 "건너뛰기"를 명시적으로 선택한 경우 호출합니다.
                인증된 회원의 초대 코드 입력 절차를 종료 상태로 표시하여, 이후 입력 화면이 다시 노출되지 않도록 합니다.

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 401: 인증 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "ERR01" / "ERR02" / "ERR03" |
                | timestamp | string(datetime) | 응답 일시 | "2026-11-22T12:12:30.013" |
                | content | object | 응답 본문 | null |
                | message | string | 처리 결과 | "초대 코드 입력 건너뛰기 성공" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "초대 코드 입력 건너뛰기 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-11-22T11:20:19.055291",
                                          "content": null,
                                          "message": "초대 코드 입력 건너뛰기 성공"
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
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "존재하지 않는 회원",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 404,
                                          "code": "ERR01",
                                          "timestamp": "2026-11-22T11:17:15.509897",
                                          "content": null,
                                          "message": "존재하지 않는 회원입니다."
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "이미 추천 회원 초대 코드 등록 또는 이미 입력 건너뛰기 완료",
                        content = @Content(
                                examples = {
                                        @ExampleObject(
                                                name = "이미 추천 회원 초대 코드 등록",
                                                value = """
                                                        {
                                                          "statusCode": 409,
                                                          "code": "ERR02",
                                                          "timestamp": "2026-11-22T11:18:54.711653",
                                                          "content": null,
                                                          "message": "이미 추천 회원 초대 코드를 등록했습니다."
                                                        }
                                                        """
                                        ),
                                        @ExampleObject(
                                                name = "이미 추천 회원 초대 코드 입력 건너뛰기",
                                                value = """
                                                        {
                                                          "statusCode": 409,
                                                          "code": "ERR03",
                                                          "timestamp": "2026-11-22T11:18:54.711653",
                                                          "content": null,
                                                          "message": "이미 추천 회원 초대 코드 입력을 건너뛰었습니다."
                                                        }
                                                        """
                                        )
                                }
                        )
                )
        }
)
public @interface SkipReferredUserCodeInputApiDocs {
}
