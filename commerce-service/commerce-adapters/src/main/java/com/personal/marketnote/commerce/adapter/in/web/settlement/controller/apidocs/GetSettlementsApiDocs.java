package com.personal.marketnote.commerce.adapter.in.web.settlement.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
        summary = "(관리자) 정산 목록 조회",
        description = """
                작성일자: 2026-02-16
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                연/월 기준으로 정산 목록을 조회합니다.
                
                ---
                
                ## Query Parameters
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | year | number | 정산 연도 | Y | 2026 |
                | month | number | 정산 월 | Y | 2 |
                
                ---
                
                ## Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | settlements | array | 정산 목록 | [ ... ] |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "year",
                        in = ParameterIn.QUERY,
                        required = true,
                        description = "정산 연도",
                        schema = @Schema(type = "integer", example = "2026")
                ),
                @Parameter(
                        name = "month",
                        in = ParameterIn.QUERY,
                        required = true,
                        description = "정산 월",
                        schema = @Schema(type = "integer", example = "2")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "정산 목록 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-16T12:00:00.000",
                                          "content": {
                                            "settlements": [
                                              {
                                                "id": 1,
                                                "sellerId": 10,
                                                "year": 2026,
                                                "month": 2,
                                                "totalAllocatedAmount": 100000,
                                                "shippingFee": 0,
                                                "pgFeeAmount": 3000,
                                                "platformFeeAmount": 5000,
                                                "sellerPayoutAmount": 92000,
                                                "status": "COMPLETED",
                                                "createdAt": "2026-02-16T10:00:00",
                                                "modifiedAt": "2026-02-16T10:00:00"
                                              }
                                            ]
                                          },
                                          "message": "정산 목록 조회 성공"
                                        }
                                        """)
                        )
                )
        })
public @interface GetSettlementsApiDocs {
}
