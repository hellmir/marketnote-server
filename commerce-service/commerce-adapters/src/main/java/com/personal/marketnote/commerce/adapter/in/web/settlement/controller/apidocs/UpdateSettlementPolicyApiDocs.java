package com.personal.marketnote.commerce.adapter.in.web.settlement.controller.apidocs;

import com.personal.marketnote.commerce.adapter.in.web.settlement.request.UpdateSettlementPolicyRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
        summary = "(관리자) 정산 정책 수정",
        description = """
                작성일자: 2026-03-02
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 정산 정책의 수수료율, 정산 주기, 최소 지급 금액을 수정합니다.
                
                - 수수료율은 basis point 단위입니다 (300 = 3%, 10000 = 100%).
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | pgFeeRate | number | PG 수수료율 (basis point, 0~10000) | Y | 250 |
                | platformFeeRate | number | 플랫폼 수수료율 (basis point, 0~10000) | Y | 400 |
                | settlementCycle | string | 정산 주기 | Y | "BIWEEKLY" |
                | minPayoutAmount | number | 최소 지급 금액 | Y | 5000 |
                """,
        parameters = {
                @Parameter(name = "id", description = "정산 정책 ID", required = true, example = "1")
        },
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = UpdateSettlementPolicyRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "pgFeeRate": 250,
                                  "platformFeeRate": 400,
                                  "settlementCycle": "BIWEEKLY",
                                  "minPayoutAmount": 5000
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "정산 정책 수정 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-03-02T12:00:00.000",
                                          "content": {
                                            "id": 1,
                                            "sellerId": 100,
                                            "pgFeeRate": 250,
                                            "platformFeeRate": 400,
                                            "settlementCycle": "BIWEEKLY",
                                            "minPayoutAmount": 5000,
                                            "status": "ACTIVE"
                                          },
                                          "message": "정산 정책 수정 성공"
                                        }
                                        """)
                        )
                )
        })
public @interface UpdateSettlementPolicyApiDocs {
}
