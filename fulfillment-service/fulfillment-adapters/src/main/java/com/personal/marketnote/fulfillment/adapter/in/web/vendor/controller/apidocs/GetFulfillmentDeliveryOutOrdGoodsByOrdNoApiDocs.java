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
        summary = "(관리자) 파스토 주문번호 기반 출고중 상품 조회",
        description = """
                작성일자: 2026-02-17
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                파스토 출고중 상품의 주문번호 기준 송장 및 상품 정보를 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **위치** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- | --- |
                | accessToken | header | string | 파스토 액세스 토큰 | Y | your-access-token |
                | customerCode | path | string | 파스토 고객사 코드 | Y | 00000 |
                | startDate | path | string | 조회 시작일(YYYY-MM-DD) | Y | 2026-02-11 |
                | endDate | path | string | 조회 종료일(YYYY-MM-DD) | Y | 2026-02-13 |
                | ordNo | query | string | 고객사 주문번호 | N | ORDER-20260130 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-02-17T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "파스토 주문번호 기반 출고중 상품 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | dataCount | number | 조회 건수 | 1 |
                | goodsByOrdNo | array | 주문번호 기반 상품 정보 | [ ... ] |
                
                ---
                
                ### Response > content > goodsByOrdNo
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | ordNo | string | 주문번호 | "ORDER-20260130" |
                | invoiceNo | string | 송장번호 | "1234567890" |
                | goods | array | 상품 정보 | [ ... ] |
                
                ---
                
                ### Response > content > goodsByOrdNo > goods
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | cstGodCd | string | 고객사 상품코드 | "1" |
                | godNm | string | 상품명 | "테스트상품" |
                | ordQty | number | 상품 수량 | 2 |
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
                        name = "startDate",
                        description = "조회 시작일(YYYY-MM-DD)",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "2026-02-11")
                ),
                @Parameter(
                        name = "endDate",
                        description = "조회 종료일(YYYY-MM-DD)",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "2026-02-13")
                ),
                @Parameter(
                        name = "ordNo",
                        description = "고객사 주문번호",
                        in = ParameterIn.QUERY,
                        required = false,
                        schema = @Schema(type = "string", example = "ORDER-20260130")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "파스토 주문번호 기반 출고중 상품 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-17T12:12:30.013",
                                          "content": {
                                            "dataCount": 1,
                                            "goodsByOrdNo": [
                                              {
                                                "ordNo": "ORDER-20260130",
                                                "invoiceNo": "1234567890",
                                                "goods": [
                                                  {
                                                    "cstGodCd": "1",
                                                    "godNm": "테스트상품",
                                                    "ordQty": 2
                                                  }
                                                ]
                                              }
                                            ]
                                          },
                                          "message": "파스토 주문번호 기반 출고중 상품 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetFulfillmentDeliveryOutOrdGoodsByOrdNoApiDocs {
}
