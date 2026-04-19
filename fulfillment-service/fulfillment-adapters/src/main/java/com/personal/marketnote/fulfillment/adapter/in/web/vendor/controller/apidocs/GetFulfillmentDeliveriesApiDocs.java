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
        summary = "(관리자) 파스토 출고 목록 조회",
        description = """
                작성일자: 2026-02-11
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                파스토 출고 목록을 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **위치** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- | --- |
                | accessToken | header | string | 파스토 액세스 토큰 | Y | your-access-token |
                | customerCode | path | string | 파스토 고객사 코드 | Y | 00000 |
                | startDate | path | string | 조회 시작일(YYYY-MM-DD) | Y | 2026-02-11 |
                | endDate | path | string | 조회 종료일(YYYY-MM-DD) | Y | 2026-02-11 |
                | status | path | string | 작업상태 코드(ALL:전체, ORDER:출고요청, WORKING:출고작업중, DONE:출고완료, PARTDONE:부분출고, CANCEL:출고요청취소, SHORTAGE:재고부족결품) | Y | ALL |
                | outDiv | path | string | 출고 구분(1:택배, 2:차량배송) | Y | 1 |
                | ordNo | query | string | 고객사 주문번호 | N | 202601200001 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-02-11T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "파스토 출고 목록 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | dataCount | number | 조회 건수 | 0 |
                | deliveries | array | 출고 목록 | [ ... ] |
                
                ---
                
                ### Response > content > deliveries
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | outDt | string | 출고일자 | null |
                | ordDt | string | 주문일자 | "20260121" |
                | whCd | string | 창고코드 | "TEST" |
                | whNm | string | 창고명 | "테스트" |
                | slipNo | string | 전표번호 | "TESTOO260121000042" |
                | cstCd | string | 고객사코드 | "00000" |
                | cstNm | string | 고객사명 | "마켓노트 주식회사 테스트" |
                | shopCd | string | 출고처코드 | "99999999" |
                | mapSlipNo | string | 매핑전표번호 | "" |
                | shopNm | string | 출고처명 | "미지정 출고" |
                | sku | number | SKU 수량 | 1 |
                | ordQty | number | 지시수량(요청수량) | 1 |
                | addGodOrdQty | number | 딸린상품 지시수량 | 0 |
                | outDiv | string | 출고구분(1:택배, 2:차량/기타) | null |
                | outDivNm | string | 출고구분명 | null |
                | cstShopCd | string | 고객사출고처코드 | null |
                | ordNo | string | 고객사 주문번호 | "asdf" |
                | ordSeq | number | 고객사 주문 요청순번 | 1 |
                | shipReqTerm | string | 배송요청사항 | "" |
                | salChanel | string | 판매채널 | "" |
                | outWay | string | 출고방식(1:선입선출,2:후입선출,3:유통기한지정) | "1" |
                | ordDiv | string | 배송유형구분 | "N" |
                | outWayNm | string | 출고방식명 | "선입선출" |
                | wrkStat | string | 작업진행상태코드 | "1" |
                | wrkStatNm | string | 작업진행상태명 | "출고요청" |
                | invoiceNo | string | 송장번호 | null |
                | parcelNm | string | 택배사명 | null |
                | parcelCd | string | 택배사코드 | null |
                | custNm | string | 배송 수령인 | "홍길동" |
                | custAddr | string | 배송 주소 | "서울특별시 마포구 와우산로29바길 12" |
                | custTelNo | string | 수령인 번호 | "01012345678" |
                | sendNm | string | 발송자명 | "" |
                | sendTelNo | string | 발송자 번호 | "" |
                | updUserNm | string | 수정자 | "OPEN_API (00000)" |
                | updTime | string | 수정일 | "202601211159" |
                | goodsSerialNo | array | 상품 일련번호 목록 | [] |
                | supCd | string | 공급사코드 | "" |
                | supNm | string | 공급사명 | null |
                | remark | string | 비고 | "" |
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
                        schema = @Schema(type = "string", example = "2026-02-11")
                ),
                @Parameter(
                        name = "status",
                        description = "작업상태 코드(ALL:전체, ORDER:출고요청, WORKING:출고작업중, DONE:출고완료, PARTDONE:부분출고, CANCEL:출고요청취소, SHORTAGE:재고부족결품)",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "ALL")
                ),
                @Parameter(
                        name = "outDiv",
                        description = "출고 구분(1:택배, 2:차량배송)",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "1")
                ),
                @Parameter(
                        name = "ordNo",
                        description = "고객사 주문번호",
                        in = ParameterIn.QUERY,
                        required = false,
                        schema = @Schema(type = "string", example = "202601200001")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "파스토 출고 목록 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-12T12:12:30.013",
                                          "content": {
                                            "dataCount": 1,
                                            "deliveries": [
                                              {
                                                "outDt": null,
                                                "ordDt": "20260121",
                                                "whCd": "TEST",
                                                "whNm": "테스트",
                                                "slipNo": "TESTOO260121000042",
                                                "cstCd": "00000",
                                                "cstNm": "마켓노트 주식회사 테스트",
                                                "shopCd": "99999999",
                                                "mapSlipNo": "",
                                                "shopNm": "미지정 출고",
                                                "sku": 1,
                                                "ordQty": 1,
                                                "addGodOrdQty": 0,
                                                "outDiv": null,
                                                "outDivNm": null,
                                                "cstShopCd": null,
                                                "ordNo": "asdf",
                                                "ordSeq": 1,
                                                "shipReqTerm": "",
                                                "salChanel": "",
                                                "outWay": "1",
                                                "ordDiv": "N",
                                                "outWayNm": "선입선출",
                                                "wrkStat": "1",
                                                "wrkStatNm": "출고요청",
                                                "invoiceNo": null,
                                                "parcelNm": null,
                                                "parcelCd": null,
                                                "custNm": "홍길동",
                                                "custAddr": "서울특별시 마포구 와우산로29바길 12",
                                                "custTelNo": "01012345678",
                                                "sendNm": "",
                                                "sendTelNo": "",
                                                "updUserNm": "OPEN_API (00000)",
                                                "updTime": "202601211159",
                                                "goodsSerialNo": [],
                                                "supCd": "",
                                                "supNm": null,
                                                "remark": ""
                                              }
                                            ]
                                          },
                                          "message": "파스토 출고 목록 조회 성공"
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
                                          "timestamp": "2026-02-11T12:12:30.013",
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
                                          "timestamp": "2026-02-11T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetFulfillmentDeliveriesApiDocs {
}
