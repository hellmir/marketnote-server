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
        summary = "(관리자) 파스토 재고 목록 조회",
        description = """
                작성일자: 2026-02-03
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                파스토 재고 목록을 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **위치** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- | --- |
                | accessToken | header | string | 파스토 액세스 토큰 | Y | your-access-token |
                | customerCode | path | string | 파스토 고객사 코드 | Y | 00000 |
                | outOfStockYn | query | string | 품절 상품 조회 여부(Y/N) | N | Y |
                | whCd | query | string | 창고 코드 | N | TEST |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-02-03T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "파스토 재고 목록 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | dataCount | number | 조회 건수 | 1 |
                | stocks | array | 재고 목록 | [ ... ] |
                
                ---
                
                ### Response > content > stocks
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | whCd | string | 창고번호 | "TEST" |
                | godCd | string | 상품코드 | "00000IBA00001" |
                | cstGodCd | string | 고객사상품코드 | "IBA00001" |
                | godNm | string | 상품명 | "테스트상품" |
                | distTermDt | string | 유통기한일자 | "" |
                | distTermMgtYn | string | 유통기한관리여부 | "N" |
                | godBarcd | string | 상품바코드 | "IBA00001" |
                | stockQty | number | 재고수량 | 10000 |
                | badStockQty | number | 불용재고 | 0 |
                | canStockQty | number | 주문가능수량 | 9999 |
                | cstSupCd | string | 고객사공급사코드 | null |
                | supNm | string | 공급사명 | "미지정 공급사" |
                | giftDiv | string | 사은품구분(01:본품, 02:사은품, 03:부자재) | "01" |
                | goodsSerialNo | array | 상품 일련번호 목록 | [] |
                | slipNo | string | 입고지시번호 | "TESTIO260113000001" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "accessToken",
                        description = "파스토 액세스 토큰",
                        in = ParameterIn.HEADER,
                        required = true,
                        schema = @Schema(type = "string", example = "your-access-token")
                ),
                @Parameter(
                        name = "customerCode",
                        description = "파스토 고객사 코드",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "00000")
                ),
                @Parameter(
                        name = "outOfStockYn",
                        description = "품절 상품 조회 여부(Y/N)",
                        in = ParameterIn.QUERY,
                        required = false,
                        schema = @Schema(type = "string", example = "Y")
                ),
                @Parameter(
                        name = "whCd",
                        description = "창고 코드",
                        in = ParameterIn.QUERY,
                        required = false,
                        schema = @Schema(type = "string", example = "TEST")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "파스토 재고 목록 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-03T12:12:30.013",
                                          "content": {
                                            "dataCount": 1,
                                            "stocks": [
                                              {
                                                "whCd": "TEST",
                                                "godCd": "00000IBA00001",
                                                "cstGodCd": "IBA00001",
                                                "godNm": "테스트상품",
                                                "distTermDt": "",
                                                "distTermMgtYn": "N",
                                                "godBarcd": "IBA00001",
                                                "stockQty": 10000,
                                                "badStockQty": 0,
                                                "canStockQty": 9999,
                                                "cstSupCd": null,
                                                "supNm": "미지정 공급사",
                                                "giftDiv": "01",
                                                "goodsSerialNo": [],
                                                "slipNo": "TESTIO260113000001"
                                              }
                                            ]
                                          },
                                          "message": "파스토 재고 목록 조회 성공"
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
                                          "timestamp": "2026-02-03T12:12:30.013",
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
                                          "timestamp": "2026-02-03T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        })
public @interface GetFulfillmentStocksApiDocs {
}
