package com.personal.marketnote.notification.adapter.in.web.device.controller.apidocs;

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
        summary = "디바이스 토큰 삭제",
        description = """
                작성일자: 2026-06-03

                작성자: 성효빈

                ---

                ## Description

                - 로그인한 사용자의 특정 기기 FCM 디바이스 토큰을 삭제합니다.

                - 로그아웃, 앱 삭제 시 호출합니다.

                - 본인 소유 기기만 삭제할 수 있습니다. 다른 사용자의 기기 또는 존재하지 않는 기기일 경우 404를 반환합니다.

                ---

                ## Path Variable

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | deviceId | string | 기기 고유 식별자 | "550e8400-e29b-41d4-a716-446655440000" |

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 401: 인증 실패 / 404: 찾을 수 없음 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "UNAUTHORIZED" / "NOT_FOUND" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-06-03T10:00:00.000" |
                | content | null | 응답 본문 | null |
                | message | string | 처리 결과 | "디바이스 토큰 삭제 성공" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(name = "deviceId", description = "기기 고유 식별자", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "삭제 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-06-03T10:00:00.000",
                                          "content": null,
                                          "message": "디바이스 토큰 삭제 성공"
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
                                          "timestamp": "2026-06-03T10:00:00.000",
                                          "content": null,
                                          "message": "Invalid token"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "디바이스 토큰 찾을 수 없음",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 404,
                                          "code": "NOT_FOUND",
                                          "timestamp": "2026-06-03T10:00:00.000",
                                          "content": null,
                                          "message": "디바이스 토큰을 찾을 수 없습니다."
                                        }
                                        """)
                        )
                )
        }
)
public @interface DeleteDeviceTokenApiDocs {
}
