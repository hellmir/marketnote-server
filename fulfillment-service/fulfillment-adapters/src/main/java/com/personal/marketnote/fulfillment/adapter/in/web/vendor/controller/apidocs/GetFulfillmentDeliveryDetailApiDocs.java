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
        summary = "(관리자) 파스토 출고 상세 조회",
        description = """
                작성일자: 2026-02-12
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                파스토 출고 상세 정보를 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **위치** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- | --- |
                | accessToken | header | string | 파스토 액세스 토큰 | Y | your-access-token |
                | customerCode | path | string | 파스토 고객사 코드 | Y | 00000 |
                | slipNo | path | string | 출고요청번호 | Y | TESTOO260121000042 |
                | ordNo | query | string | 주문번호 | N | asdf |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-02-12T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "파스토 출고 상세 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | dataCount | number | 조회 건수 | 1 |
                | deliveries | array | 출고 상세 목록 | [ ... ] |
                
                ---
                
                ### Response > content > deliveries
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | outDt | string | 출고일자 | null |
                | ordDt | string | 출고요청일자 | "20260121" |
                | whCd | string | 창고코드 | "TEST" |
                | whNm | string | 창고명 | "테스트" |
                | slipNo | string | 전표번호 | "TESTOO260121000042" |
                | cstCd | string | 고객사코드 | "00000" |
                | cstNm | string | 고객사명 | "마켓노트 주식회사 테스트" |
                | cstShopCd | string | 고객사출고처코드 | null |
                | shopCd | string | 출고처코드 | "99999999" |
                | mapSlipNo | string | 매핑전표번호 | "" |
                | shopNm | string | 출고처명 | "미지정 출고" |
                | sku | number | SKU 수량 | 1 |
                | ordQty | number | 지시수량(요청수량) | 1 |
                | addGodOrdQty | number | 딸린상품 지시수량 | 0 |
                | outDiv | string | 출고구분(1:택배, 2:차량/기타) | null |
                | outDivNm | string | 출고구분명 | null |
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
                | goods | array | 출고 상품 목록 | [ ... ] |
                | remark | string | 비고 | "" |
                
                ---
                
                ### Response > content > deliveries > goods
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | ordDt | string | 출고요청일자 | "20260121" |
                | whCd | string | 창고코드 | "TEST" |
                | slipNo | string | 전표번호 | "TESTOO260121000042" |
                | cstCd | string | 고객사코드 | "00000" |
                | shopCd | string | 출고처코드 | "99999999" |
                | supCd | string | 공급사코드 | null |
                | godCd | string | 상품코드 | "00000IBA00001" |
                | cstGodCd | string | 고객사상품코드 | "IBA00001" |
                | godNm | string | 상품명 | "테스트상품" |
                | orgGodCd | string | 원상품코드 | "00000IBA00001" |
                | godType | string | 상품유형 | "1" |
                | godTypeNm | string | 상품유형명 | "단품" |
                | distTermDt | string | 유통기한 | "" |
                | stockQty | number | 재고수량 | 9998 |
                | ordQty | number | 지시수량 | 1 |
                | addGodOrdQty | number | 딸린상품 지시수량 | 0 |
                | ordQtySum | number | 지시수량 합계 | 1 |
                | giftDiv | string | 사은품구분 | "01" |
                | addType | string | 추가타입 | null |
                | emgrYn | string | 긴급여부 | "N" |
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
                        name = "slipNo",
                        description = "출고요청번호",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "TESTOO260121000042")
                ),
                @Parameter(
                        name = "ordNo",
                        description = "주문번호",
                        in = ParameterIn.QUERY,
                        required = false,
                        schema = @Schema(type = "string", example = "asdf")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "파스토 출고 상세 조회 성공",
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
                                                "cstShopCd": null,
                                                "shopCd": "99999999",
                                                "mapSlipNo": "",
                                                "shopNm": "미지정 출고",
                                                "sku": 1,
                                                "ordQty": 1,
                                                "addGodOrdQty": 0,
                                                "outDiv": null,
                                                "outDivNm": null,
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
                                                "goods": [
                                                  {
                                                    "ordDt": "20260121",
                                                    "whCd": "TEST",
                                                    "slipNo": "TESTOO260121000042",
                                                    "cstCd": "00000",
                                                    "shopCd": "99999999",
                                                    "supCd": null,
                                                    "godCd": "00000IBA00001",
                                                    "cstGodCd": "IBA00001",
                                                    "godNm": "테스트상품",
                                                    "orgGodCd": "00000IBA00001",
                                                    "godType": "1",
                                                    "godTypeNm": "단품",
                                                    "distTermDt": "",
                                                    "stockQty": 9998,
                                                    "ordQty": 1,
                                                    "addGodOrdQty": 0,
                                                    "ordQtySum": 1,
                                                    "giftDiv": "01",
                                                    "addType": null,
                                                    "emgrYn": "N"
                                                  }
                                                ],
                                                "remark": ""
                                              }
                                            ]
                                          },
                                          "message": "파스토 출고 상세 조회 성공"
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
                                          "timestamp": "2026-02-12T12:12:30.013",
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
                                          "timestamp": "2026-02-12T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetFulfillmentDeliveryDetailApiDocs {
}
