package com.personal.marketnote.common.outbox.adapter.in.web.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

/**
 * (관리자) Outbox FAILED 이벤트 조회 API 문서 애노테이션.
 *
 * @author 성효빈
 * @since 2026-04-05
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "(관리자) Outbox FAILED 이벤트 조회",
        description = """
                작성일자: 2026-04-05

                작성자: 성효빈

                ---

                ## Description

                Outbox FAILED 상태의 이벤트 목록을 조회합니다. 토픽 필터를 적용할 수 있습니다.

                ---

                ## Query Parameters

                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | topic | string | 토픽명 필터 | N | "commerce.payment.approved" |

                ---

                ## Response > content

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | content | array | FAILED 이벤트 목록 | - |
                | content[].id | number | 이벤트 ID | 1 |
                | content[].eventId | string | 이벤트 UUID | "550e8400-e29b-41d4-a716-446655440000" |
                | content[].topic | string | 토픽명 | "commerce.payment.approved" |
                | content[].partitionKey | string | 파티션 키 | "order-123" |
                | content[].eventType | string | 이벤트 타입 | "PaymentApproved" |
                | content[].source | string | 소스 서비스 | "commerce-service" |
                | content[].retryCount | number | 재시도 횟수 | 5 |
                | content[].maxRetries | number | 최대 재시도 횟수 | 5 |
                | content[].createdAt | string | 생성 일시 | "2026-04-05T10:00:00" |
                | content[].failedAt | string | 실패 일시 | "2026-04-05T10:05:00" |
                | content[].lastErrorMessage | string | 마지막 에러 메시지 | "Kafka send failed" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "topic",
                        description = "토픽명 필터",
                        in = ParameterIn.QUERY,
                        required = false,
                        schema = @Schema(type = "string")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "FAILED 이벤트 조회 완료",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-05T12:00:00.000",
                                          "content": [
                                            {
                                              "id": 1,
                                              "eventId": "550e8400-e29b-41d4-a716-446655440000",
                                              "topic": "commerce.payment.approved",
                                              "partitionKey": "order-123",
                                              "eventType": "PaymentApproved",
                                              "source": "commerce-service",
                                              "retryCount": 5,
                                              "maxRetries": 5,
                                              "createdAt": "2026-04-05T10:00:00",
                                              "failedAt": "2026-04-05T10:05:00",
                                              "lastErrorMessage": "Kafka send failed"
                                            }
                                          ],
                                          "message": "Outbox FAILED 이벤트 조회 완료"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "인증 실패"
                ),
                @ApiResponse(
                        responseCode = "403",
                        description = "관리자 권한 없음"
                )
        }
)
public @interface QueryOutboxFailedEventsApiDocs {
}
