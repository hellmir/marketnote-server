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
        summary = "추천 회원 초대 코드 등록 여부 조회",
        description = """
                작성일자: 2026-11-03

                작성자: 성효빈

                ---

                ## Description

                인증된 회원이 자신을 추천한 회원의 초대 코드를 이미 등록했는지 여부를 조회합니다.

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 401: 인증 실패 / 404: 리소스 조회 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "ERR01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-11-03T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "추천 회원 초대 코드 등록 여부 조회 성공" |

                ---

                ### Response > content

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | isRegistered | boolean | 추천인 초대 코드 등록 여부 | true |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "추천 회원 초대 코드 등록 여부 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-11-03T11:20:19.055291",
                                          "content": {
                                            "isRegistered": true
                                          },
                                          "message": "추천 회원 초대 코드 등록 여부 조회 성공"
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
                                          "timestamp": "2026-11-03T11:19:36.206993",
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
                                          "timestamp": "2026-11-03T11:17:15.509897",
                                          "content": null,
                                          "message": "존재하지 않는 회원입니다."
                                        }
                                        """)
                        )
                )
        }
)
public @interface IsReferredUserCodeRegisteredApiDocs {
}
