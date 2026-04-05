package com.personal.marketnote.common.outbox.adapter.in.web.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

/**
 * (관리자) Outbox FAILED 이벤트 해결 API 문서 애노테이션.
 *
 * @author 성효빈
 * @since 2026-04-05
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "(관리자) Outbox FAILED 이벤트 해결",
        description = """
                작성일자: 2026-04-05
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                Outbox FAILED 이벤트를 RETRY(재시도) 또는 DISCARD(폐기)로 해결합니다.
                
                ---
                
                ## Request Body
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | id | number | 이벤트 ID | Y | 1 |
                | action | string | 처리 액션 (RETRY / DISCARD) | Y | "RETRY" |
                | reason | string | 처리 사유 (최대 500자) | N | "수동 재시도" |
                
                ---
                
                ## Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 이벤트 ID | 1 |
                | eventId | string | 이벤트 UUID | "550e8400-e29b-41d4-a716-446655440000" |
                | topic | string | 토픽명 | "commerce.payment.approved" |
                | resolution | string | 처리 결과 (RETRY / DISCARD) | "RETRY" |
                | reason | string | 처리 사유 | "수동 재시도" |
                | alreadyResolved | boolean | 이미 해결된 이벤트 여부 | false |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "FAILED 이벤트 해결 완료",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-05T12:00:00.000",
                                          "content": {
                                            "id": 1,
                                            "eventId": "550e8400-e29b-41d4-a716-446655440000",
                                            "topic": "commerce.payment.approved",
                                            "resolution": "RETRY",
                                            "reason": "수동 재시도",
                                            "alreadyResolved": false
                                          },
                                          "message": "Outbox FAILED 이벤트 해결 완료"
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
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "이벤트를 찾을 수 없음"
                )
        }
)
public @interface ResolveOutboxApiDocs {
}
