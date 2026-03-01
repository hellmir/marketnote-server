package com.personal.marketnote.commerce.adapter.in.web.settlement.controller.apidocs;

import com.personal.marketnote.commerce.adapter.in.web.settlement.request.ExecuteSettlementRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "(관리자) 정산 실행",
        description = """
                작성일자: 2026-02-16

                작성자: 성효빈

                ---

                ## Description

                - 지정된 연/월에 대해 판매자별 정산을 실행합니다.

                - PG 수수료율과 플랫폼 수수료율은 basis point (1/10000) 단위입니다.

                - 예: pgFeeRate=300은 3%, platformFeeRate=500은 5%

                ---

                ## Request

                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | year | number | 정산 연도 | Y | 2026 |
                | month | number | 정산 월 | Y | 2 |
                | pgFeeRate | number | PG 수수료율 (basis point) | Y | 300 |
                | platformFeeRate | number | 플랫폼 수수료율 (basis point) | Y | 500 |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = ExecuteSettlementRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "year": 2026,
                                  "month": 2,
                                  "pgFeeRate": 300,
                                  "platformFeeRate": 500
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "정산 실행 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-16T12:00:00.000",
                                          "content": null,
                                          "message": "정산 실행 성공"
                                        }
                                        """)
                        )
                )
        })
public @interface ExecuteSettlementApiDocs {
}
