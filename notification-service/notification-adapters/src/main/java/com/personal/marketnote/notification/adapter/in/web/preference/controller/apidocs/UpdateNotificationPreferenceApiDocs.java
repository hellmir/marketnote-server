package com.personal.marketnote.notification.adapter.in.web.preference.controller.apidocs;

import com.personal.marketnote.common.adapter.in.api.schema.StringResponseSchema;
import com.personal.marketnote.notification.adapter.in.web.preference.request.UpdateNotificationPreferenceRequest;
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
        summary = "알림 수신 설정 변경",
        description = """
                작성일자: 2026-06-03

                작성자: 성효빈

                ---

                ## Description

                - 인증된 사용자의 특정 알림 타입의 수신 설정을 변경합니다.

                - enabled=true로 변경 시 consentedAt(수신 동의 시점)이 현재 시점으로 기록됩니다.

                - 광고성 알림(EVENT 등)의 수신 동의 시점 기록은 정보통신망법 준수를 위한 것입니다.

                ---

                ## Request

                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | notificationType | string | 알림 타입 | Y | "EVENT" |
                | enabled | boolean | 수신 여부 | Y | true |

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 404: 설정 없음 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "NOT_FOUND" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-06-03T10:00:00.000" |
                | content | null | 응답 본문 | null |
                | message | string | 처리 결과 | "알림 수신 설정 변경 성공" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = UpdateNotificationPreferenceRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "notificationType": "EVENT",
                                  "enabled": true
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "변경 성공",
                        content = @Content(
                                schema = @Schema(implementation = StringResponseSchema.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-06-03T10:00:00.000",
                                          "content": null,
                                          "message": "알림 수신 설정 변경 성공"
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
                        description = "알림 수신 설정 없음",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 404,
                                          "code": "NOT_FOUND",
                                          "timestamp": "2026-06-03T10:00:00.000",
                                          "content": null,
                                          "message": "알림 수신 설정을 찾을 수 없습니다."
                                        }
                                        """)
                        )
                )
        }
)
public @interface UpdateNotificationPreferenceApiDocs {
}
