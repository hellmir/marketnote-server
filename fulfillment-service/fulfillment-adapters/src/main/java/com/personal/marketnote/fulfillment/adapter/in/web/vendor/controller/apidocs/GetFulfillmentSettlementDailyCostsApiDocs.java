package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs;

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
        summary = "(관리자) 파스토 물류비 일별 비용 조회",
        description = """
                작성일자: 2026-02-08
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                파스토 물류비 일별 비용을 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **위치** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- | --- |
                | accessToken | header | string | 파스토 액세스 토큰 | Y | ad8c0accf2a011f0be620ab49498ff55 |
                | yearMonth | path | string | 정산 월(YYYYMM) | Y | 202601 |
                | whCd | path | string | 파스토 센터 코드 | Y | TEST |
                | customerCode | path | string | 파스토 고객사 코드 | Y | 94388 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-02-08T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "파스토 물류비 일별 비용 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | dataCount | number | 조회 건수 | 2 |
                | dailyCosts | array | 일별 비용 목록 | [ ... ] |
                
                ---
                
                ### Response > content > dailyCosts
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | cloDt | string | 마감일자 | "2026-02-08" |
                | whCd | string | 파스토 센터 코드 | "YI21" |
                | cstCd | string | 고객사 코드 | "94EVA" |
                | inpAmt | string | 입고 비용 | "0" |
                | outAmt | string | 출고 비용 | "46650" |
                | outcarAmt | string | 차량 출고 비용 | "0" |
                | outairAmt | string | 항공 출고 비용 | "0" |
                | keepAmt | string | 보관 비용 | "4420" |
                | retAmt | string | 반품 비용 | "0" |
                | cashAmt | string | 현금 결제 비용 | "0" |
                | founAmt | string | 파손 비용 | "0" |
                | othAmt | string | 기타 비용 | "0" |
                | totAmt | string | 총 비용 | "51070" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "accessToken",
                        description = "파스토 액세스 토큰",
                        in = ParameterIn.HEADER,
                        required = true,
                        schema = @Schema(type = "string", example = "ad8c0accf2a011f0be620ab49498ff55")
                ),
                @Parameter(
                        name = "yearMonth",
                        description = "정산 월(YYYYMM)",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "202601")
                ),
                @Parameter(
                        name = "whCd",
                        description = "파스토 센터 코드",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "TEST")
                ),
                @Parameter(
                        name = "customerCode",
                        description = "파스토 고객사 코드",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "94388")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "파스토 물류비 일별 비용 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-08T12:12:30.013",
                                          "content": {
                                            "dataCount": 2,
                                            "dailyCosts": [
                                              {
                                                "cloDt": "2026-02-08",
                                                "whCd": "YI21",
                                                "cstCd": "94EVA",
                                                "inpAmt": "0",
                                                "outAmt": "46650",
                                                "outcarAmt": "0",
                                                "outairAmt": "0",
                                                "keepAmt": "4420",
                                                "retAmt": "0",
                                                "cashAmt": "0",
                                                "founAmt": "0",
                                                "othAmt": "0",
                                                "totAmt": "51070"
                                              },
                                              {
                                                "cloDt": "2026-02-05",
                                                "whCd": "YI21",
                                                "cstCd": "94EVA",
                                                "inpAmt": "0",
                                                "outAmt": "96100",
                                                "outcarAmt": "0",
                                                "outairAmt": "0",
                                                "keepAmt": "4420",
                                                "retAmt": "3650",
                                                "cashAmt": "0",
                                                "founAmt": "0",
                                                "othAmt": "0",
                                                "totAmt": "104170"
                                              }
                                            ]
                                          },
                                          "message": "파스토 물류비 일별 비용 조회 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "토큰 인증 실패",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 401,
                                          "code": "UNAUTHORIZED",
                                          "timestamp": "2026-02-08T12:12:30.013",
                                          "content": null,
                                          "message": "Invalid token"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "403",
                        description = "토큰 인가 실패",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 403,
                                          "code": "FORBIDDEN",
                                          "timestamp": "2026-02-08T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        })
public @interface GetFulfillmentSettlementDailyCostsApiDocs {
}
