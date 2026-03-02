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
        summary = "(판매자) 나의 정산 내역 조회",
        description = """
                작성일자: 2026-03-02
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 판매자 본인의 정산 내역을 조회합니다.
                
                - year는 필수, month는 선택입니다.
                
                - month를 지정하면 해당 월의 정산만, 미지정 시 해당 연도 전체 정산을 조회합니다.
                
                ---
                
                ## Query Parameters
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | year | number | 정산 연도 | Y | 2026 |
                | month | number | 정산 월 | N | 2 |
                
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
                        required = false,
                        description = "정산 월 (미지정 시 해당 연도 전체)",
                        schema = @Schema(type = "integer", example = "2")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "나의 정산 내역 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-03-02T12:00:00.000",
                                          "content": {
                                            "settlements": [
                                              {
                                                "id": 1,
                                                "sellerId": 10,
                                                "year": 2026,
                                                "month": 1,
                                                "totalAllocatedAmount": 100000,
                                                "pgFeeAmount": 3000,
                                                "platformFeeAmount": 5000,
                                                "sellerPayoutAmount": 92000,
                                                "status": "COMPLETED",
                                                "createdAt": "2026-02-16T10:00:00",
                                                "modifiedAt": "2026-02-16T10:00:00"
                                              }
                                            ]
                                          },
                                          "message": "나의 정산 내역 조회 성공"
                                        }
                                        """)
                        )
                )
        })
public @interface GetSellerSettlementsApiDocs {
}
