package com.personal.marketnote.commerce.adapter.in.web.settlement.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

/**
 * 실패한 정산 목록 조회 API 문서.
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "(관리자) 실패한 정산 목록 조회",
        description = """
                작성일자: 2026-03-02
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - FAILED 상태의 정산 목록을 조회합니다.
                
                - 재시도가 필요한 정산을 식별하기 위해 사용합니다.
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "실패한 정산 목록 조회 성공",
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
                                                "month": 2,
                                                "totalAllocatedAmount": 100000,
                                                "pgFeeAmount": 3000,
                                                "platformFeeAmount": 5000,
                                                "sellerPayoutAmount": 92000,
                                                "status": "FAILED",
                                                "createdAt": "2026-03-02T10:00:00.000",
                                                "modifiedAt": "2026-03-02T10:00:01.000"
                                              }
                                            ]
                                          },
                                          "message": "실패한 정산 목록 조회 성공"
                                        }
                                        """)
                        )
                )
        })
public @interface GetFailedSettlementsApiDocs {
}
