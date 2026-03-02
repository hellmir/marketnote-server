package com.personal.marketnote.commerce.adapter.in.web.settlement.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

/**
 * 정산 취소 API 문서.
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "(관리자) 정산 취소",
        description = """
                작성일자: 2026-03-02

                작성자: 성효빈

                ---

                ## Description

                - COMPLETED 상태의 정산을 취소합니다.

                - 역분개(reverse journal entry)를 기록하고 CANCELLED 상태로 전이합니다.

                - PaymentAllocation의 settlementId는 유지됩니다 (재실행 시 추적 가능).

                ---

                ## Path Variable

                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | id | number | 정산 ID | Y | 1 |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = @Parameter(name = "id", description = "정산 ID", required = true, example = "1"),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "정산 취소 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-03-02T12:00:00.000",
                                          "content": null,
                                          "message": "정산 취소 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "정산을 찾을 수 없음"
                ),
                @ApiResponse(
                        responseCode = "500",
                        description = "COMPLETED 상태가 아닌 정산에 대한 취소 시도"
                )
        })
public @interface CancelSettlementApiDocs {
}
