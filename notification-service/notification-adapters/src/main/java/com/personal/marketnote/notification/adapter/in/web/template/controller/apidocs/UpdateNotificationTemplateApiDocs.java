package com.personal.marketnote.notification.adapter.in.web.template.controller.apidocs;

import com.personal.marketnote.common.adapter.in.api.schema.StringResponseSchema;
import com.personal.marketnote.notification.adapter.in.web.template.request.UpdateNotificationTemplateRequest;
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
        summary = "(관리자) 알림 템플릿 수정",
        description = """
                작성일자: 2026-06-03

                작성자: 성효빈

                ---

                ## Description

                - 알림 템플릿의 제목, 본문 템플릿, URL 템플릿을 수정합니다.

                - 템플릿 코드와 알림 유형은 변경할 수 없습니다.

                - 관리자만 가능합니다.

                ---

                ## Request

                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | title | string | 제목 | Y | "수정된 알림 제목" |
                | bodyTemplate | string | 본문 템플릿 | Y | "수정된 본문 {productName}" |
                | urlTemplate | string | URL 템플릿 | N | "/order/{orderId}" |

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 찾을 수 없음 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-06-03T10:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "알림 템플릿 수정 성공" |

                ### Response > content

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 템플릿 ID | 1 |
                | templateCode | string | 템플릿 코드 | "ORDER_PAYMENT_COMPLETED" |
                | notificationType | string | 알림 유형 | "ORDER_PAYMENT_COMPLETED" |
                | notificationCategory | string | 알림 카테고리 | "INFORMATIONAL" |
                | title | string | 제목 | "수정된 알림 제목" |
                | bodyTemplate | string | 본문 템플릿 | "수정된 본문 {productName}" |
                | urlTemplate | string | URL 템플릿 | "/order/{orderId}" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = UpdateNotificationTemplateRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "title": "수정된 알림 제목",
                                  "bodyTemplate": "수정된 본문 {productName}",
                                  "urlTemplate": "/order/{orderId}"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "수정 성공",
                        content = @Content(
                                schema = @Schema(implementation = StringResponseSchema.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-06-03T10:00:00.000",
                                          "content": {
                                            "id": 1,
                                            "templateCode": "ORDER_PAYMENT_COMPLETED",
                                            "notificationType": "ORDER_PAYMENT_COMPLETED",
                                            "notificationCategory": "INFORMATIONAL",
                                            "title": "수정된 알림 제목",
                                            "bodyTemplate": "수정된 본문 {productName}",
                                            "urlTemplate": "/order/{orderId}"
                                          },
                                          "message": "알림 템플릿 수정 성공"
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
                        responseCode = "403",
                        description = "토큰 인가 실패",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 403,
                                          "code": "FORBIDDEN",
                                          "timestamp": "2026-06-03T10:00:00.000",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "템플릿 찾을 수 없음",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 404,
                                          "code": "NOT_FOUND",
                                          "timestamp": "2026-06-03T10:00:00.000",
                                          "content": null,
                                          "message": "ERR_NOTIFICATION_TEMPLATE_03::알림 템플릿을 찾을 수 없습니다. id=999"
                                        }
                                        """)
                        )
                )
        }
)
public @interface UpdateNotificationTemplateApiDocs {
}
