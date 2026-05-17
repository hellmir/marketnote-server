package com.personal.marketnote.commerce.adapter.in.web.settlement.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "(관리자) 정산 단건 조회",
        description = """
                작성일자: 2026-02-16
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                정산 ID로 정산 상세 정보를 조회합니다.
                
                ---
                
                ## Path Variable
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | id | number | 정산 ID | Y | 1 |
                
                ---
                
                ## Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 정산 ID | 1 |
                | sellerId | number | 판매자 ID | 10 |
                | year | number | 정산 연도 | 2026 |
                | month | number | 정산 월 | 2 |
                | totalAllocatedAmount | number | 총 배분 금액 | 100000 |
                | pgFeeAmount | number | PG 수수료 | 3000 |
                | platformFeeAmount | number | 플랫폼 수수료 | 5000 |
                | sellerPayoutAmount | number | 판매자 지급액 | 92000 |
                | status | string | 정산 상태 | "COMPLETED" |
                | createdAt | string | 생성일시 | "2026-02-16T10:00:00" |
                | modifiedAt | string | 수정일시 | "2026-02-16T10:00:00" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "정산 단건 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-16T12:00:00.000",
                                          "content": {
                                            "id": 1,
                                            "sellerId": 10,
                                            "year": 2026,
                                            "month": 2,
                                            "totalAllocatedAmount": 100000,
                                            "pgFeeAmount": 3000,
                                            "platformFeeAmount": 5000,
                                            "sellerPayoutAmount": 92000,
                                            "status": "COMPLETED",
                                            "createdAt": "2026-02-16T10:00:00",
                                            "modifiedAt": "2026-02-16T10:00:00"
                                          },
                                          "message": "정산 단건 조회 성공"
                                        }
                                        """)
                        )
                )
        })
public @interface GetSettlementApiDocs {
}
