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
        summary = "(관리자) 파스토 상품 입고 상세 조회",
        description = """
                작성일자: 2026-02-14
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                파스토 상품 입고 상세 정보를 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **위치** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- | --- |
                | accessToken | header | string | 파스토 액세스 토큰 | Y | eefae1befa4c11f0be620ab49498ff55 |
                | customerCode | path | string | 파스토 고객사 코드 | Y | 94388 |
                | slipNo | path | string | 입고요청번호 | Y | TESTIO260119000005 |
                | ordNo | query | string | 주문번호 | N | 12345 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-02-14T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "파스토 상품 입고 상세 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | dataCount | number | 조회 건수 | 1 |
                | details | array | 입고 상세 목록 | [ ... ] |
                
                ---
                
                ### Response > content > details
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | ordDt | string | 입고일자 | "20260119" |
                | whCd | string | 창고코드 | "TEST" |
                | whNm | string | 창고명 | "테스트" |
                | slipNo | string | 전표번호(입고요청번호) | "TESTIO260119000005" |
                | ordNo | string | 주문번호 | "" |
                | cstCd | string | 고객사코드 | "94388" |
                | cstNm | string | 고객사명 | "마켓노트 주식회사 테스트" |
                | supCd | string | FSS공급사코드 | "99999999" |
                | supNm | string | 공급사명 | "미지정 공급사" |
                | cstSupCd | string | 고객사공급사코드 | null |
                | sku | number | sku(상품 종류수) | 1 |
                | ordQty | number | 입고 요청 수량 | 1 |
                | inQty | number | 입고 완료 수량 | 0 |
                | tarQty | number | 대상 수량 | 1 |
                | inWay | string | 입고방식(01:택배,02:차량) | "01" |
                | inWayNm | string | 입고방식명 | "택배" |
                | parcelComp | string | 택배사명 | "" |
                | parcelInvoiceNo | string | 입고시 송장번호 | "" |
                | wrkStat | string | 작업상태코드(1:입고요청,2:검수중,3:검수완료,4:입고완료,5:입고취소) | "1" |
                | wrkStatNm | string | 작업상태명 | "입고요청" |
                | emgrYn | string | 긴급입고여부 | null |
                | remark | string | 입고요청내용 | "" |
                | goods | array | 입고 상품 목록 | [ ... ] |
                
                ---
                
                ### Response > content > details > goods
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | ordDt | string | 입고일자 | "20260119" |
                | whCd | string | 창고코드 | "TEST" |
                | slipNo | string | 전표번호(입고요청번호) | "TESTIO260119000005" |
                | cstCd | string | 고객사코드 | "94388" |
                | shopCd | string | 공급사코드 | null |
                | supCd | string | FSS공급사코드 | "99999999" |
                | godCd | string | 상품코드 | "943881" |
                | cstGodCd | string | 고객사상품코드 | "1" |
                | godNm | string | 상품명 | "테스트 상품1" |
                | orgGodCd | string | 원상품코드 | null |
                | godType | string | 상품유형 | null |
                | godTypeNm | string | 상품유형명 | null |
                | distTermDt | string | 유통기한 | "" |
                | stockQty | number | 재고수량 | 0 |
                | ordQty | number | 지시수량 | 1 |
                | addGodOrdQty | number | 딸린상품 지시수량 | 0 |
                | ordQtySum | number | 지시수량 합계 | 1 |
                | giftDiv | string | 사은품구분 | "01" |
                | addType | string | 추가타입 | null |
                | emgrYn | string | 긴급여부 | null |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "accessToken",
                        description = "파스토 액세스 토큰",
                        in = ParameterIn.HEADER,
                        required = true,
                        schema = @Schema(type = "string", example = "eefae1befa4c11f0be620ab49498ff55")
                ),
                @Parameter(
                        name = "customerCode",
                        description = "파스토 고객사 코드",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "94388")
                ),
                @Parameter(
                        name = "slipNo",
                        description = "입고요청번호",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "TESTIO260119000005")
                ),
                @Parameter(
                        name = "ordNo",
                        description = "주문번호",
                        in = ParameterIn.QUERY,
                        required = false,
                        schema = @Schema(type = "string", example = "12345")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "파스토 상품 입고 상세 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-14T12:12:30.013",
                                          "content": {
                                            "dataCount": 1,
                                            "details": [
                                              {
                                                "ordDt": "20260119",
                                                "whCd": "TEST",
                                                "whNm": "테스트",
                                                "slipNo": "TESTIO260119000005",
                                                "ordNo": "",
                                                "cstCd": "94388",
                                                "cstNm": "마켓노트 주식회사 테스트",
                                                "supCd": "99999999",
                                                "supNm": "미지정 공급사",
                                                "cstSupCd": null,
                                                "sku": 1,
                                                "ordQty": 1,
                                                "inQty": 0,
                                                "tarQty": 1,
                                                "inWay": "01",
                                                "inWayNm": "택배",
                                                "parcelComp": "",
                                                "parcelInvoiceNo": "",
                                                "wrkStat": "1",
                                                "wrkStatNm": "입고요청",
                                                "emgrYn": null,
                                                "remark": "",
                                                "goods": [
                                                  {
                                                    "ordDt": "20260119",
                                                    "whCd": "TEST",
                                                    "slipNo": "TESTIO260119000005",
                                                    "cstCd": "94388",
                                                    "shopCd": null,
                                                    "supCd": "99999999",
                                                    "godCd": "943881",
                                                    "cstGodCd": "1",
                                                    "godNm": "테스트 상품1",
                                                    "orgGodCd": null,
                                                    "godType": null,
                                                    "godTypeNm": null,
                                                    "distTermDt": "",
                                                    "stockQty": 0,
                                                    "ordQty": 1,
                                                    "addGodOrdQty": 0,
                                                    "ordQtySum": 1,
                                                    "giftDiv": "01",
                                                    "addType": null,
                                                    "emgrYn": null
                                                  }
                                                ]
                                              }
                                            ]
                                          },
                                          "message": "파스토 상품 입고 상세 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetFulfillmentWarehousingDetailApiDocs {
}
