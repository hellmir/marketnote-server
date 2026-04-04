package com.personal.marketnote.common.outbox.adapter.in.web.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

/**
 * (관리자) Outbox FAILED 토픽별 요약 조회 API 문서 애노테이션.
 *
 * @author 성효빈
 * @since 2026-04-05
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "(관리자) Outbox FAILED 토픽별 요약 조회",
        description = """
                작성일자: 2026-04-05

                작성자: 성효빈

                ---

                ## Description

                Outbox FAILED 이벤트의 토픽별 건수 요약을 조회합니다.

                ---

                ## Response > content

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | content | array | 토픽별 요약 목록 | - |
                | content[].topic | string | 토픽명 | "commerce.payment.approved" |
                | content[].failedCount | number | FAILED 건수 | 3 |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "FAILED 토픽별 요약 조회 완료",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-05T12:00:00.000",
                                          "content": [
                                            {
                                              "topic": "commerce.payment.approved",
                                              "failedCount": 3
                                            },
                                            {
                                              "topic": "commerce.order.created",
                                              "failedCount": 1
                                            }
                                          ],
                                          "message": "Outbox FAILED 토픽별 요약 조회 완료"
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
public @interface QueryOutboxFailedSummaryApiDocs {
}
