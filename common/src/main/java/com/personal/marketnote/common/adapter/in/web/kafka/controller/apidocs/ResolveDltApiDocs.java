package com.personal.marketnote.common.adapter.in.web.kafka.controller.apidocs;

import com.personal.marketnote.common.adapter.in.web.kafka.request.ResolveDltRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

/**
 * (관리자) DLT 메시지별 선택적 재처리/폐기 API 문서 애노테이션.
 *
 * @author 성효빈
 * @since 2026-03-14
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "(관리자) DLT 메시지별 선택적 재처리/폐기",
        description = """
                작성일자: 2026-03-14
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                지정된 DLT 메시지를 개별적으로 재처리(RETRY) 또는 폐기(DISCARD)합니다.
                - RETRY: 원본 토픽으로 재발행 후 RETRIED 상태 저장
                - DISCARD: 재발행 없이 DISCARDED 상태만 저장
                - 이미 처리된 메시지는 멱등하게 기존 상태를 반환합니다.
                
                ---
                
                ## Request Body
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | originalTopic | string | 원본 토픽명 | Y | "commerce.order.payment-completed" |
                | partition | integer | DLT 메시지 파티션 번호 | Y | 0 |
                | offset | long | DLT 메시지 오프셋 번호 | Y | 5 |
                | action | string | 처리 액션 (RETRY / DISCARD) | Y | "RETRY" |
                | reason | string | 처리 사유 (최대 500자) | N | "일시적 DB 타임아웃 복구" |
                
                ---
                
                ## Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | originalTopic | string | 원본 토픽명 | "commerce.order.payment-completed" |
                | dltTopic | string | DLT 토픽명 | "commerce.order.payment-completed.dlt" |
                | partition | integer | 파티션 번호 | 0 |
                | offset | long | 오프셋 번호 | 5 |
                | resolution | string | 해결 상태 (RETRIED / DISCARDED) | "RETRIED" |
                | reason | string | 처리 사유 | "일시적 DB 타임아웃 복구" |
                | alreadyResolved | boolean | 이미 처리 여부 | false |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = ResolveDltRequest.class),
                        examples = @ExampleObject("""
                                {
                                    "originalTopic": "commerce.order.payment-completed",
                                    "partition": 0,
                                    "offset": 5,
                                    "action": "RETRY",
                                    "reason": "일시적 DB 타임아웃 복구"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "DLT 메시지 해결 완료",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-03-14T12:00:00.000",
                                          "content": {
                                            "originalTopic": "commerce.order.payment-completed",
                                            "dltTopic": "commerce.order.payment-completed.dlt",
                                            "partition": 0,
                                            "offset": 5,
                                            "resolution": "RETRIED",
                                            "reason": "일시적 DB 타임아웃 복구",
                                            "alreadyResolved": false
                                          },
                                          "message": "DLT 메시지 해결 완료"
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
public @interface ResolveDltApiDocs {
}
