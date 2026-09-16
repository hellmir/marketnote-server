package com.personal.marketnote.notification.adapter.in.web.notification.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "알림 읽음 처리",
        description = """
                작성일자: 2026-09-03

                작성자: 성효빈

                ---

                ## Description

                - 특정 알림을 읽음 상태로 변경합니다.

                - 본인 소유 알림만 읽음 처리할 수 있습니다. 다른 사용자의 알림 또는 존재하지 않는 알림일 경우 404를 반환합니다.

                - 이미 읽음 상태인 알림에 대해 재요청해도 정상 응답합니다 (멱등).

                ---

                ## Path Variable

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 알림 ID | 25 |

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 401: 인증 실패 / 404: 찾을 수 없음 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "UNAUTHORIZED" / "NOT_FOUND" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-09-03T10:00:00.000" |
                | content | null | 응답 본문 | null |
                | message | string | 처리 결과 | "알림 읽음 처리 성공" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(name = "id", description = "알림 ID", required = true, example = "25")
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "읽음 처리 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-09-03T10:00:00.000",
                                          "content": null,
                                          "message": "알림 읽음 처리 성공"
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
                                          "timestamp": "2026-09-03T10:00:00.000",
                                          "content": null,
                                          "message": "Invalid token"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "알림 찾을 수 없음",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 404,
                                          "code": "NOT_FOUND",
                                          "timestamp": "2026-09-03T10:00:00.000",
                                          "content": null,
                                          "message": "알림을 찾을 수 없습니다."
                                        }
                                        """)
                        )
                )
        }
)
public @interface MarkNotificationAsReadApiDocs {
}
