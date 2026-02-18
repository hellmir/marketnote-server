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
        summary = "(관리자) 파스토 출고 상품 상세 목록 조회",
        description = """
                작성일자: 2026-02-18
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                파스토 출고 상품의 상세 정보(금액, 배송유형 등)를 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **위치** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- | --- |
                | accessToken | header | string | 파스토 액세스 토큰 | Y | 039a797bf66d11f0be620ab49498ff55 |
                | customerCode | path | string | 파스토 고객사 코드 | Y | 94388 |
                | startDate | path | string | 조회 시작일(YYYY-MM-DD) | Y | 2026-02-01 |
                | endDate | path | string | 조회 종료일(YYYY-MM-DD) | Y | 2026-02-18 |
                | ordNo | query | string | 고객사 주문번호 | N | ORDER-20260202 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-02-18T12:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "파스토 출고 상품 상세 목록 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | dataCount | number | 조회 건수 | 1 |
                | goodDetails | array | 출고 상품 상세 정보 | [ ... ] |
                
                ---
                
                ### Response > content > goodDetails
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | outDt | string | 출고일 | "2026-02-18" |
                | slipNo | string | 출고요청번호 | "SLP20260202001" |
                | outOrdSlipNo | string | 출고지시번호 | "OUT20260202001" |
                | orderNo | string | 주문번호 | "ORDER-20260202" |
                | productOrderNo | string | 상품주문번호 | "PROD-20260202" |
                | ordDiv | string | 주문구분 | "1" |
                | invoiceNo | string | 송장번호 | "1234567890" |
                | sellerChannel | string | 판매채널 | "NAVER" |
                | custNm | string | 수하인명 | "홍길동" |
                | godCd | string | 상품코드 | "GOD001" |
                | cstGodCd | string | 고객사 상품코드 | "CST001" |
                | godNm | string | 상품명 | "테스트상품" |
                | outQty | number | 출고수량 | 2 |
                | markedPrAmount | number | 정가금액 | 15000.00 |
                | sellingPrAmount | number | 판매금액 | 12000.00 |
                | dcAmount | number | 할인금액 | 3000.00 |
                | sellerDcAmount | number | 판매자할인금액 | 1000.00 |
                | naverDcAmount | number | 네이버할인금액 | 2000.00 |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "accessToken",
                        description = "파스토 액세스 토큰",
                        in = ParameterIn.HEADER,
                        required = true,
                        schema = @Schema(type = "string", example = "039a797bf66d11f0be620ab49498ff55")
                ),
                @Parameter(
                        name = "customerCode",
                        description = "파스토 고객사 코드",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "94388")
                ),
                @Parameter(
                        name = "startDate",
                        description = "조회 시작일(YYYY-MM-DD)",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "2026-02-01")
                ),
                @Parameter(
                        name = "endDate",
                        description = "조회 종료일(YYYY-MM-DD)",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "2026-02-18")
                ),
                @Parameter(
                        name = "ordNo",
                        description = "고객사 주문번호",
                        in = ParameterIn.QUERY,
                        required = false,
                        schema = @Schema(type = "string", example = "ORDER-20260202")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "파스토 출고 상품 상세 목록 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-18T12:00:00.000",
                                          "content": {
                                            "dataCount": 1,
                                            "goodDetails": [
                                              {
                                                "outDt": "2026-02-18",
                                                "slipNo": "SLP20260202001",
                                                "outOrdSlipNo": "OUT20260202001",
                                                "orderNo": "ORDER-20260202",
                                                "productOrderNo": "PROD-20260202",
                                                "ordDiv": "1",
                                                "invoiceNo": "1234567890",
                                                "sellerChannel": "NAVER",
                                                "custNm": "홍길동",
                                                "godCd": "GOD001",
                                                "cstGodCd": "CST001",
                                                "godNm": "테스트상품",
                                                "outQty": 2,
                                                "markedPrAmount": 15000.00,
                                                "sellingPrAmount": 12000.00,
                                                "dcAmount": 3000.00,
                                                "sellerDcAmount": 1000.00,
                                                "naverDcAmount": 2000.00
                                              }
                                            ]
                                          },
                                          "message": "파스토 출고 상품 상세 목록 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetFasstoDeliveryGoodDetailApiDocs {
}
