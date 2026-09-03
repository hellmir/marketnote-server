package com.personal.marketnote.notification.adapter.in.web.sse.controller.apidocs;

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
        summary = "SSE 알림 스트림 구독",
        description = """
                작성일자: 2026-09-03

                작성자: 성효빈

                ---

                ## Description

                - 인증된 사용자의 실시간 알림 SSE 스트림을 구독합니다.

                - 연결 즉시 `event: connect / data: CONNECTED` 초기 이벤트가 전송됩니다.

                - 30초 간격으로 heartbeat가 전송되어 커넥션이 유지됩니다.

                - 동일 사용자가 재연결하면 이전 커넥션은 자동으로 종료됩니다.

                ---

                ## SSE Event Types

                | **이벤트** | **설명** |
                | --- | --- |
                | connect | 초기 연결 확인 이벤트 |
                | heartbeat | 커넥션 유지용 (comment, 클라이언트 핸들러에 전달되지 않음) |

                ---

                ## Response

                `text/event-stream` 형식으로 스트리밍됩니다.

                ```
                event: connect
                data: CONNECTED

                : heartbeat
                ```
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "SSE 스트림 연결 성공",
                        content = @Content(
                                mediaType = "text/event-stream",
                                examples = @ExampleObject("""
                                        event: connect
                                        data: CONNECTED
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
                                          "timestamp": "2026-09-03T12:00:00.000",
                                          "content": null,
                                          "message": "Invalid token"
                                        }
                                        """)
                        )
                )
        }
)
public @interface SubscribeSseStreamApiDocs {
}
