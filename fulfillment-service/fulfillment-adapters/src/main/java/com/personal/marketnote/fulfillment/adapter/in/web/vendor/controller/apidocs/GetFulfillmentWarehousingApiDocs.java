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
        summary = "(관리자) 파스토 상품 입고 목록 조회",
        description = """
                작성일자: 2026-02-03
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                파스토 상품 입고 목록을 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **위치** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- | --- |
                | accessToken | header | string | 파스토 액세스 토큰 | Y | 3169eb15ef7a11f0be620ab49498ff55 |
                | customerCode | path | string | 파스토 고객사 코드 | Y | 94388 |
                | startDate | path | string | 조회 시작일(YYYYMMDD) | Y | 20260113 |
                | endDate | path | string | 조회 종료일(YYYYMMDD) | Y | 20260113 |
                | inWay | query | string | 입고방법(비어있으면:전체,01:택배,02:차량) | N | 01 |
                | ordNo | query | string | 주문번호 | N | 202601130001 |
                | wrkStat | query | string | 작업상태(비어있으면:전체,1:입고요청,2:센터도착,3:입고검수,4:입고확정,5:입고완료) | N | 1 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-02-03T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "파스토 상품 입고 목록 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | dataCount | number | 조회 건수 | 1 |
                | warehousing | array | 상품 입고 목록 | [ ... ] |
                
                ---
                
                ### Response > content > warehousing
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | ordDt | string | 입고일자 | "20260113" |
                | whCd | string | 창고코드 | "TEST" |
                | whNm | string | 창고명 | "테스트" |
                | ordNo | string | 주문번호 | "" |
                | slipNo | string | 전표번호(입고요청번호) | "TESTIO260113000003" |
                | cstCd | string | 고객사코드 | "94388" |
                | cstNm | string | 고객사명 | "마켓노트 주식회사 테스트" |
                | supCd | string | FSS공급사코드 | "99999999" |
                | cstSupCd | string | 고객사공급사코드 | null |
                | sku | number | sku(상품 종류수) | 1 |
                | supNm | string | 공급사명 | "미지정 공급사" |
                | ordQty | number | 입고 요청 수량 | 1 |
                | inQty | number | 입고 완료 수량 | 0 |
                | inWay | string | 입고방식(01:택배,02:차량) | "01" |
                | inWayNm | string | 입고방식명 | "택배" |
                | parcelComp | string | 택배사명 | "" |
                | parcelInvoiceNo | string | 입고시 송장번호 | "" |
                | wrkStat | string | 작업상태코드(1 : 입고요청 or 센터도착, 2 : 검수중, 3 : 검수완료, 4 : 입고완료, 5 : 입고취소) | "1" |
                | wrkStatNm | string | 작업상태명 | "입고요청" |
                | emgrYn | string | 긴급입고여부 | null |
                | remark | string | 입고요청내용 | "" |
                | goodsSerialNo | array | 상품 일련번호 목록 | [] |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "accessToken",
                        description = "파스토 액세스 토큰",
                        in = ParameterIn.HEADER,
                        required = true,
                        schema = @Schema(type = "string", example = "3169eb15ef7a11f0be620ab49498ff55")
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
                        description = "조회 시작일(YYYYMMDD)",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "20260113")
                ),
                @Parameter(
                        name = "endDate",
                        description = "조회 종료일(YYYYMMDD)",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "20260113")
                ),
                @Parameter(
                        name = "inWay",
                        description = "입고방법(비어있으면:전체,01:택배,02:차량)",
                        in = ParameterIn.QUERY,
                        required = false,
                        schema = @Schema(type = "string", example = "01")
                ),
                @Parameter(
                        name = "ordNo",
                        description = "주문번호",
                        in = ParameterIn.QUERY,
                        required = false,
                        schema = @Schema(type = "string", example = "202601130001")
                ),
                @Parameter(
                        name = "wrkStat",
                        description = "작업상태(비어있으면:전체,1:입고요청,2:센터도착,3:입고검수,4:입고확정,5:입고완료)",
                        in = ParameterIn.QUERY,
                        required = false,
                        schema = @Schema(type = "string", example = "1")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "파스토 상품 입고 목록 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-03T12:12:30.013",
                                          "content": {
                                            "dataCount": 1,
                                            "warehousing": [
                                              {
                                                "ordDt": "20260113",
                                                "whCd": "TEST",
                                                "whNm": "테스트",
                                                "ordNo": "",
                                                "slipNo": "TESTIO260113000003",
                                                "cstCd": "94388",
                                                "cstNm": "마켓노트 주식회사 테스트",
                                                "supCd": "99999999",
                                                "cstSupCd": null,
                                                "sku": 1,
                                                "supNm": "미지정 공급사",
                                                "ordQty": 1,
                                                "inQty": 0,
                                                "inWay": "01",
                                                "inWayNm": "택배",
                                                "parcelComp": "",
                                                "parcelInvoiceNo": "",
                                                "wrkStat": "1",
                                                "wrkStatNm": "입고요청",
                                                "emgrYn": null,
                                                "remark": "",
                                                "goodsSerialNo": []
                                              }
                                            ]
                                          },
                                          "message": "파스토 상품 입고 목록 조회 성공"
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
public @interface GetFulfillmentWarehousingApiDocs {
}
