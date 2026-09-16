package com.personal.marketnote.notification.adapter.in.web.notification.controller.apidocs;

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
        summary = "미읽음 알림 수 조회",
        description = """
                작성일자: 2026-09-03

                작성자: 성효빈

                ---

                ## Description

                - 인증된 사용자의 미읽음 알림 건수를 조회합니다.

                - 앱 상단 알림 뱃지 표시에 사용합니다.

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 401: 인증 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "UNAUTHORIZED" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-09-03T10:00:00.000" |
                | content | object | 미읽음 알림 수 | { "unreadCount": 5 } |
                | message | string | 처리 결과 | "미읽음 알림 수 조회 성공" |

                ### Response > content

                | **키** | **타입** | **설명** |
                | --- | --- | --- |
                | unreadCount | number | 미읽음 알림 건수 |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "조회 성공",
                        content = @Content(
                                schema = @Schema(implementation = StringResponseSchema.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-09-03T10:00:00.000",
                                          "content": {
                                            "unreadCount": 5
                                          },
                                          "message": "미읽음 알림 수 조회 성공"
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
                )
        }
)
public @interface GetUnreadNotificationCountApiDocs {
}
