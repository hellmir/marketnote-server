package com.personal.marketnote.notification.adapter.in.web.notification.controller.apidocs;

import com.personal.marketnote.common.adapter.in.api.schema.StringResponseSchema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
        summary = "알림 이력 조회",
        description = """
                작성일자: 2026-06-03

                작성자: 성효빈

                ---

                ## Description

                - 인증된 사용자의 알림 발송 이력을 커서 기반 페이징으로 조회합니다.

                - 최신순(id DESC)으로 정렬되며, 기본 페이지 크기는 20건입니다.

                - 첫 페이지 요청 시 totalElements를 포함하여 반환합니다.

                ---

                ## Request Parameters

                | **파라미터** | **타입** | **필수** | **기본값** | **설명** |
                | --- | --- | --- | --- | --- |
                | cursor | number | N | - | 이전 페이지 마지막 항목의 ID |
                | page-size | number | N | 20 | 페이지 크기 (1~100) |

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-06-03T10:00:00.000" |
                | content | object | 알림 이력 응답 | { notifications: { ... } } |
                | message | string | 처리 결과 | "알림 이력 조회 성공" |

                ### Response > content > notifications

                | **키** | **타입** | **설명** |
                | --- | --- | --- |
                | totalElements | number | 전체 건수 (첫 페이지만) |
                | hasNext | boolean | 다음 페이지 존재 여부 |
                | nextCursor | number | 다음 페이지 커서 |
                | items[] | array | 알림 항목 목록 |

                ### Response > content > notifications > items[]

                | **키** | **타입** | **설명** |
                | --- | --- | --- |
                | id | number | 알림 ID |
                | notificationType | string | 알림 타입 |
                | notificationTypeDescription | string | 알림 타입 설명 |
                | title | string | 알림 제목 |
                | body | string | 알림 본문 |
                | deliveryChannel | string | 발송 채널 |
                | isRead | boolean | 읽음 여부 |
                | landingUrl | string | 딥링크 URL |
                | sendStatus | string | 발송 상태 |
                | scheduledAt | string(datetime) | 예약 발송 시각 |
                | createdAt | string(datetime) | 생성 일시 |
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
                                          "timestamp": "2026-06-03T12:00:00.000",
                                          "content": {
                                            "notifications": {
                                              "totalElements": 25,
                                              "hasNext": true,
                                              "nextCursor": 6,
                                              "items": [
                                                {
                                                  "id": 25,
                                                  "notificationType": "ORDER_PAYMENT_COMPLETED",
                                                  "notificationTypeDescription": "주문 결제 완료",
                                                  "title": "주문이 완료되었습니다",
                                                  "body": "테스트 상품 외 1건이 결제되었습니다.",
                                                  "deliveryChannel": "PUSH_AND_IN_APP",
                                                  "isRead": false,
                                                  "landingUrl": "/order/123",
                                                  "sendStatus": "SENT",
                                                  "scheduledAt": null,
                                                  "createdAt": "2026-06-03T12:00:00"
                                                }
                                              ]
                                            }
                                          },
                                          "message": "알림 이력 조회 성공"
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
                                          "timestamp": "2026-06-03T12:00:00.000",
                                          "content": null,
                                          "message": "Invalid token"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetNotificationHistoryApiDocs {
}
