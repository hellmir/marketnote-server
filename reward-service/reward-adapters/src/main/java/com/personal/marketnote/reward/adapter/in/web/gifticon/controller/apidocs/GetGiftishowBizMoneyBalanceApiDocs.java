package com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs;

import com.personal.marketnote.common.adapter.in.api.schema.StringResponseSchema;
import io.swagger.v3.oas.annotations.Operation;
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
        summary = "(관리자) 기프티쇼 비즈머니 잔액 조회",
        description = """
                작성일자: 2026-04-06
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 관리자가 기프티쇼 비즈 계정의 비즈머니 잔액을 조회합니다.
                
                - 기프티쇼 API (0301)를 호출하여 실시간 잔액을 반환합니다.
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-04-06T12:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "기프티쇼 비즈머니 잔액 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | balance | number | 비즈머니 잔액 | 250000 |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "기프티쇼 비즈머니 잔액 조회 성공",
                        content = @Content(
                                schema = @Schema(implementation = StringResponseSchema.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-06T12:00:00.000",
                                          "content": {
                                            "balance": 250000
                                          },
                                          "message": "기프티쇼 비즈머니 잔액 조회 성공"
                                        }
                                        """)
                        )
                )
        })
public @interface GetGiftishowBizMoneyBalanceApiDocs {
}
