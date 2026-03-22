package com.personal.marketnote.common.adapter.in.web.kafka.controller.apidocs;

import com.personal.marketnote.common.adapter.in.web.kafka.request.ReprocessDltRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

/**
 * (관리자) DLT 메시지 수동 재처리 API 문서 애노테이션.
 *
 * @author 성효빈
 * @since 2026-03-19
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "(관리자) DLT 메시지 수동 재처리",
        description = """
                작성일자: 2026-03-19
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                지정된 원본 토픽의 DLT(Dead Letter Topic) 메시지를 원본 토픽으로 재전송합니다.
                
                ---
                
                ## Request Body
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | originalTopic | string | 원본 토픽명 | Y | "order-payment-completed" |
                
                ---
                
                ## Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | originalTopic | string | 원본 토픽명 | "order-payment-completed" |
                | dltTopic | string | DLT 토픽명 | "order-payment-completed.DLT" |
                | reprocessedCount | number | 재처리 성공 건수 | 5 |
                | failedCount | number | 재처리 실패 건수 | 0 |
                | skippedCount | number | 이미 처리되어 스킵된 건수 | 2 |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = ReprocessDltRequest.class),
                        examples = @ExampleObject("""
                                {
                                    "originalTopic": "order-payment-completed"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "DLT 메시지 재처리 완료",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-03-19T12:00:00.000",
                                          "content": {
                                            "originalTopic": "order-payment-completed",
                                            "dltTopic": "order-payment-completed.DLT",
                                            "reprocessedCount": 5,
                                            "failedCount": 0,
                                            "skippedCount": 2
                                          },
                                          "message": "DLT 메시지 재처리 완료"
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
public @interface ReprocessDltApiDocs {
}
