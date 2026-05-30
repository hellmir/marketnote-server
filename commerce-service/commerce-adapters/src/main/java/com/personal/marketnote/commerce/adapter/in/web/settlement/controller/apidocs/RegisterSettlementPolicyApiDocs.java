package com.personal.marketnote.commerce.adapter.in.web.settlement.controller.apidocs;

import com.personal.marketnote.commerce.adapter.in.web.settlement.request.RegisterSettlementPolicyRequest;
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
        summary = "(관리자) 정산 정책 등록",
        description = """
                작성일자: 2026-03-02
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 판매자별 정산 정책(수수료율, 정산 주기, 최소 지급 금액)을 등록합니다.
                
                - 동일 판매자에 대해 활성 정책이 이미 존재하면 등록에 실패합니다.
                
                - 수수료율은 basis point 단위입니다 (300 = 3%, 10000 = 100%).
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | sellerId | number | 판매자 ID | Y | 100 |
                | pgFeeRate | number | PG 수수료율 (basis point, 0~10000) | Y | 300 |
                | platformFeeRate | number | 플랫폼 수수료율 (basis point, 0~10000) | Y | 500 |
                | settlementCycle | string | 정산 주기 | Y | "MONTHLY" |
                | minPayoutAmount | number | 최소 지급 금액 | Y | 10000 |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = RegisterSettlementPolicyRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "sellerId": 100,
                                  "pgFeeRate": 300,
                                  "platformFeeRate": 500,
                                  "settlementCycle": "MONTHLY",
                                  "minPayoutAmount": 10000
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "정산 정책 등록 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-03-02T12:00:00.000",
                                          "content": {
                                            "id": 1,
                                            "sellerId": 100,
                                            "pgFeeRate": 300,
                                            "platformFeeRate": 500,
                                            "settlementCycle": "MONTHLY",
                                            "minPayoutAmount": 10000,
                                            "status": "ACTIVE"
                                          },
                                          "message": "정산 정책 등록 성공"
                                        }
                                        """)
                        )
                )
        })
public @interface RegisterSettlementPolicyApiDocs {
}
