package com.personal.marketnote.common.adapter.in.web.kafka.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

/**
 * (관리자) 전체 DLT 토픽 요약 조회 API 문서 애노테이션.
 *
 * @author 성효빈
 * @since 2026-03-19
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "(관리자) 전체 DLT 토픽 요약 조회",
        description = """
                작성일자: 2026-03-19
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                전체 DLT 토픽의 메시지 건수를 조회합니다.
                
                ---
                
                ## Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | content | array | DLT 토픽 요약 목록 | - |
                | content[].originalTopic | string | 원본 토픽명 | "order-payment-completed" |
                | content[].dltTopic | string | DLT 토픽명 | "order-payment-completed.DLT" |
                | content[].messageCount | number | 메시지 건수 | 3 |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "DLT 토픽 요약 조회 완료",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-03-19T12:00:00.000",
                                          "content": [
                                            {
                                              "originalTopic": "order-payment-completed",
                                              "dltTopic": "order-payment-completed.DLT",
                                              "messageCount": 3
                                            },
                                            {
                                              "originalTopic": "inventory-sync",
                                              "dltTopic": "inventory-sync.DLT",
                                              "messageCount": 1
                                            }
                                          ],
                                          "message": "DLT 토픽 요약 조회 완료"
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
public @interface QueryDltTopicSummariesApiDocs {
}
