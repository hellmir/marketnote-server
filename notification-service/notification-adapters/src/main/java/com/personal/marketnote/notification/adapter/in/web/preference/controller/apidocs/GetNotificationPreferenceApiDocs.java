package com.personal.marketnote.notification.adapter.in.web.preference.controller.apidocs;

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
        summary = "알림 수신 설정 조회",
        description = """
                작성일자: 2026-06-03

                작성자: 성효빈

                ---

                ## Description

                - 인증된 사용자의 알림 수신 설정 전체 목록을 조회합니다.

                - NotificationType별 수신 여부(enabled)와 수신 동의 시점(consentedAt)을 반환합니다.

                - 회원 가입 시 모든 알림 타입이 enabled=true로 초기화됩니다.

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 401: 인증 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "UNAUTHORIZED" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-06-03T10:00:00.000" |
                | content | array | 알림 수신 설정 목록 | [ ... ] |
                | message | string | 처리 결과 | "알림 수신 설정 조회 성공" |

                ### Response > content[]

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | notificationType | string | 알림 타입 | "ORDER_PAYMENT_COMPLETED" |
                | description | string | 알림 타입 설명 | "주문 결제 완료" |
                | enabled | boolean | 수신 여부 | true |
                | consentedAt | string(datetime) | 수신 동의 시점 (광고성 알림) | "2026-06-03T10:00:00" |
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
                                          "timestamp": "2026-06-03T10:00:00.000",
                                          "content": [
                                            {
                                              "notificationType": "ORDER_PAYMENT_COMPLETED",
                                              "description": "주문 결제 완료",
                                              "enabled": true,
                                              "consentedAt": null
                                            },
                                            {
                                              "notificationType": "SHIPPING_STARTED",
                                              "description": "배송 시작",
                                              "enabled": true,
                                              "consentedAt": null
                                            },
                                            {
                                              "notificationType": "EVENT",
                                              "description": "이벤트",
                                              "enabled": true,
                                              "consentedAt": "2026-06-03T10:00:00"
                                            }
                                          ],
                                          "message": "알림 수신 설정 조회 성공"
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
public @interface GetNotificationPreferenceApiDocs {
}
