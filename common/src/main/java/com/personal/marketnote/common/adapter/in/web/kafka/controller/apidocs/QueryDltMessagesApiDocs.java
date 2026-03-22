package com.personal.marketnote.common.adapter.in.web.kafka.controller.apidocs;

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
 * (관리자) DLT 메시지 조회 API 문서 애노테이션.
 *
 * @author 성효빈
 * @since 2026-03-19
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "(관리자) DLT 메시지 조회",
        description = """
                작성일자: 2026-03-19
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                지정된 원본 토픽의 DLT(Dead Letter Topic) 메시지를 조회합니다.
                
                ---
                
                ## Query Parameters
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | original-topic | string | 원본 토픽명 | Y | "order-payment-completed" |
                | limit | number | 조회 건수 (1~500, 기본값: 100) | N | 100 |
                
                ---
                
                ## Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | content | array | DLT 메시지 목록 | - |
                | content[].dltTopic | string | DLT 토픽명 | "order-payment-completed.DLT" |
                | content[].partition | number | 파티션 번호 | 0 |
                | content[].offset | number | 오프셋 | 42 |
                | content[].key | string | 메시지 키 | "order-123" |
                | content[].originalTopic | string | 원본 토픽명 | "order-payment-completed" |
                | content[].errorFqcn | string | 예외 클래스명 | "NullPointerException" |
                | content[].errorMessage | string | 에러 메시지 (최대 200자) | "Cannot invoke method on null" |
                | content[].timestamp | number | 메시지 타임스탬프 (epoch millis) | 1710144000000 |
                | content[].resolution | string | 처리 상태 (UNRESOLVED / RETRIED / DISCARDED) | "UNRESOLVED" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "original-topic",
                        description = "원본 토픽명",
                        in = ParameterIn.QUERY,
                        required = true,
                        schema = @Schema(type = "string")
                ),
                @Parameter(
                        name = "limit",
                        description = "조회 건수 (1~500, 기본값: 100)",
                        in = ParameterIn.QUERY,
                        required = false,
                        schema = @Schema(type = "integer", defaultValue = "100", minimum = "1", maximum = "500")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "DLT 메시지 조회 완료",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-03-19T12:00:00.000",
                                          "content": [
                                            {
                                              "dltTopic": "order-payment-completed.DLT",
                                              "partition": 0,
                                              "offset": 42,
                                              "key": "order-123",
                                              "originalTopic": "order-payment-completed",
                                              "errorFqcn": "NullPointerException",
                                              "errorMessage": "Cannot invoke method on null",
                                              "timestamp": 1710144000000,
                                              "resolution": "UNRESOLVED"
                                            }
                                          ],
                                          "message": "DLT 메시지 조회 완료"
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
public @interface QueryDltMessagesApiDocs {
}
