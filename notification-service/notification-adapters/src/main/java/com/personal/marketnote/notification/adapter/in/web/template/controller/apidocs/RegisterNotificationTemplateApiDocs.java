package com.personal.marketnote.notification.adapter.in.web.template.controller.apidocs;

import com.personal.marketnote.common.adapter.in.api.schema.StringResponseSchema;
import com.personal.marketnote.notification.adapter.in.web.template.request.RegisterNotificationTemplateRequest;
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
        summary = "(관리자) 알림 템플릿 등록",
        description = """
                작성일자: 2026-06-03

                작성자: 성효빈

                ---

                ## Description

                - 알림 템플릿을 등록합니다.

                - 템플릿 코드는 고유해야 합니다.

                - 본문 템플릿에는 {변수명} 형식으로 치환 변수를 포함할 수 있습니다.

                - 관리자만 가능합니다.

                ---

                ## Request

                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | templateCode | string | 템플릿 코드 | Y | "ORDER_PAYMENT_COMPLETED" |
                | notificationType | string | 알림 유형 | Y | "ORDER_PAYMENT_COMPLETED" |
                | notificationCategory | string | 알림 카테고리 (MANDATORY/INFORMATIONAL/PROMOTIONAL) | Y | "INFORMATIONAL" |
                | title | string | 제목 | Y | "주문이 완료되었습니다" |
                | bodyTemplate | string | 본문 템플릿 | Y | "{productName} 외 {count}건이 결제되었습니다." |
                | urlTemplate | string | URL 템플릿 | N | "/order/{orderId}" |

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 201: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-06-03T10:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "알림 템플릿 등록 성공" |

                ### Response > content

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 생성된 템플릿 ID | 1 |
                | notificationCategory | string | 알림 카테고리 | "INFORMATIONAL" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = RegisterNotificationTemplateRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "templateCode": "ORDER_PAYMENT_COMPLETED",
                                  "notificationType": "ORDER_PAYMENT_COMPLETED",
                                  "notificationCategory": "INFORMATIONAL",
                                  "title": "주문이 완료되었습니다",
                                  "bodyTemplate": "{productName} 외 {count}건이 결제되었습니다.",
                                  "urlTemplate": "/order/{orderId}"
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
                                            "id": 1
                                          },
                                          "message": "알림 템플릿 등록 성공"
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
                )
        }
)
public @interface RegisterNotificationTemplateApiDocs {
}
