package com.personal.marketnote.notification.adapter.in.web.notification.controller.apidocs;

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
        summary = "알림 전체 읽음 처리",
        description = """
                작성일자: 2026-10-24

                작성자: 성효빈

                ---

                ## Description

                - 인증된 사용자의 모든 미읽음 알림을 한 번에 읽음 상태로 변경합니다.

                - 미읽음 알림이 0건 상태에서 호출해도 정상 응답합니다 (멱등).

                - 다른 사용자의 알림은 영향받지 않습니다.

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 401: 인증 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "UNAUTHORIZED" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-10-24T10:00:00.000" |
                | content | null | 응답 본문 | null |
                | message | string | 처리 결과 | "알림 전체 읽음 처리 성공" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "전체 읽음 처리 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-10-24T10:00:00.000",
                                          "content": null,
                                          "message": "알림 전체 읽음 처리 성공"
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
                                          "timestamp": "2026-10-24T10:00:00.000",
                                          "content": null,
                                          "message": "Invalid token"
                                        }
                                        """)
                        )
                )
        }
)
public @interface MarkAllNotificationAsReadApiDocs {
}
