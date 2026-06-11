package com.personal.marketnote.notification.adapter.in.web.device.controller.apidocs;

import com.personal.marketnote.common.adapter.in.api.schema.StringResponseSchema;
import com.personal.marketnote.notification.adapter.in.web.device.request.RegisterDeviceTokenRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "디바이스 토큰 등록",
        description = """
                작성일자: 2026-06-03

                작성자: 성효빈

                ---

                ## Description

                - 사용자 디바이스의 FCM 토큰을 등록하거나 갱신합니다.

                - deviceId 기준 upsert로 동작합니다. 동일한 deviceId로 이미 등록된 토큰이 있으면 userId/token/platform/lastUsedAt을 갱신하고, 없으면 신규 등록합니다.

                - 1명의 사용자가 여러 기기(ANDROID/IOS)를 등록할 수 있습니다.

                - 인증된 사용자만 가능합니다.

                ---

                ## Request

                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | token | string | FCM 토큰 | Y | "cXXXXXXXX:APA91bH..." |
                | platform | string | 플랫폼 (ANDROID/IOS) | Y | "ANDROID" |
                | deviceId | string | 기기 고유 식별자 | Y | "550e8400-e29b-41d4-a716-446655440000" |

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 201: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-06-03T10:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "디바이스 토큰 등록 성공" |

                ### Response > content

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 디바이스 토큰 ID | 1 |
                | isNew | boolean | 신규 등록 여부 (true: 신규, false: 기존 토큰 갱신) | true |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = RegisterDeviceTokenRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "token": "cXXXXXXXX:APA91bH...",
                                  "platform": "ANDROID",
                                  "deviceId": "550e8400-e29b-41d4-a716-446655440000"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "등록 성공",
                        content = @Content(
                                schema = @Schema(implementation = StringResponseSchema.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 201,
                                          "code": "SUC01",
                                          "timestamp": "2026-06-03T10:00:00.000",
                                          "content": {
                                            "id": 1,
                                            "isNew": true
                                          },
                                          "message": "디바이스 토큰 등록 성공"
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
                )
        }
)
public @interface RegisterDeviceTokenApiDocs {
}
