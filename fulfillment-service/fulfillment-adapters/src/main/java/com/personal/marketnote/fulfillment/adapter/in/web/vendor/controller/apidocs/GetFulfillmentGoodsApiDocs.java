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
        summary = "(관리자) 파스토 상품 목록 조회",
        description = """
                작성일자: 2026-01-30
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                파스토 상품 목록을 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **위치** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- | --- |
                | accessToken | header | string | 파스토 액세스 토큰 | Y | your-access-token |
                | customerCode | path | string | 파스토 고객사 코드 | Y | 00000 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-30T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "파스토 상품 목록 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | dataCount | number | 조회 건수 | 5 |
                | goods | array | 상품 목록 | [ ... ] |
                
                ---
                
                ### Response > content > goods
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | godCd | string | 상품코드 | "943881" |
                | godType | string | 상품유형 | "1" |
                | godNm | string | 상품명 | "테스트 상품1" |
                | godTypeNm | string | 상품유형명 | "단품" |
                | invGodNmUseYn | string | 송장출력용 상품명 사용여부 | "N" |
                | cstGodCd | string | 고객사상품코드 | "1" |
                | godOptCd1 | string | 상품옵션코드1 | "" |
                | godOptCd2 | string | 상품옵션코드2 | "" |
                | cstCd | string | 고객사코드 | "00000" |
                | cstNm | string | 고객사명 | "마켓노트 주식회사 테스트" |
                | supCd | string | 공급사코드 | "" |
                | supNm | string | 공급사명 | null |
                | cateCd | string | 카테고리코드 | "" |
                | cateNm | string | 카테고리명 | null |
                | seasonCd | string | 계절상품 코드 | "0" |
                | genderCd | string | 성별상품 코드 | "A" |
                | godPr | string | 단가 | "0" |
                | inPr | string | 공급가 | "0" |
                | salPr | string | 판매가 | "0" |
                | dealTemp | string | 취급온도 | "03" |
                | pickFac | string | 피킹설비 | "" |
                | giftDiv | string | 사은품구분 | "01" |
                | giftDivNm | string | 상품구분명 | "본품" |
                | godWidth | string | 상품가로길이 | "0.000" |
                | godLength | string | 상품세로길이 | "0.000" |
                | godHeight | string | 상품높이길이 | "0.000" |
                | makeYr | string | 연식 | "" |
                | godBulk | string | 상품체적 | "0" |
                | godWeight | string | 상품무게 | "0" |
                | godSideSum | string | 상품규격(3면의합) | "0.000" |
                | godVolume | string | 상품중량 | "" |
                | godBarcd | string | 상품바코드 | "" |
                | boxWidth | string | 내품BOX가로길이 | "0" |
                | boxLength | string | 내품BOX세로길이 | "0" |
                | boxHeight | string | 내품BOX높이길이 | "0" |
                | boxBulk | string | 내품BOX체적 | "0" |
                | boxWeight | string | 내품BOX무게 | "0" |
                | inBoxBarcd | string | 내품BOX바코드 | null |
                | inBoxLength | string | 입고BOX세로길이 | "0" |
                | inBoxHeight | string | 입고BOX높이길이 | "0" |
                | inBoxBulk | string | 입고BOX체적 | "0" |
                | inBoxWidth | string | 내품BOX가로길이 | "0" |
                | inBoxWeight | string | 입고BOX무게 | "0" |
                | inBoxSideSum | string | 입고BOX규격(3면의합) | "0" |
                | boxInCnt | string | 내품박스입수 | "0" |
                | inBoxInCnt | string | 입고박스입수 | "0" |
                | pltInCnt | string | 팔레트입수 | "0" |
                | origin | string | 원산지 | "" |
                | distTermMgtYn | string | 유통기한관리여부 | "N" |
                | useTermDay | string | 사용기한 | "0" |
                | outCanDay | string | 출고가능일수 | "0" |
                | inCanDay | string | 입고가능일수 | "0" |
                | boxDiv | string | 출고박스 | "1" |
                | bufGodYn | string | 완충상품여부 | "Y" |
                | loadingDirection | string | 출고박스 상품 적재 기준 | "NONE" |
                | firstInDt | string | 최초입고일자 | null |
                | useYn | string | 사용여부 | "Y" |
                | feeYn | string | 요금적용여부 | "" |
                | saleUnitQty | string | 판매단위수량 | "1" |
                | cstOneDayDeliveryYn | string | 원데이배송여부 | null |
                | safetyStock | string | 안전재고수량 | "1" |
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
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "파스토 상품 목록 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-30T12:12:30.013",
                                          "content": {
                                            "dataCount": 5,
                                            "goods": [
                                              {
                                                "godCd": "943881",
                                                "godType": "1",
                                                "godNm": "테스트 상품1",
                                                "godTypeNm": "단품",
                                                "invGodNmUseYn": "N",
                                                "cstGodCd": "1",
                                                "godOptCd1": "",
                                                "godOptCd2": "",
                                                "cstCd": "00000",
                                                "cstNm": "마켓노트 주식회사 테스트",
                                                "supCd": "",
                                                "supNm": null,
                                                "cateCd": "",
                                                "cateNm": null,
                                                "seasonCd": "0",
                                                "genderCd": "A",
                                                "godPr": "0",
                                                "inPr": "0",
                                                "salPr": "0",
                                                "dealTemp": "03",
                                                "pickFac": "",
                                                "giftDiv": "01",
                                                "giftDivNm": "본품",
                                                "godWidth": "0.000",
                                                "godLength": "0.000",
                                                "godHeight": "0.000",
                                                "makeYr": "",
                                                "godBulk": "0",
                                                "godWeight": "0",
                                                "godSideSum": "0.000",
                                                "godVolume": "",
                                                "godBarcd": "",
                                                "boxWidth": "0",
                                                "boxLength": "0",
                                                "boxHeight": "0",
                                                "boxBulk": "0",
                                                "boxWeight": "0",
                                                "inBoxBarcd": null,
                                                "inBoxLength": "0",
                                                "inBoxHeight": "0",
                                                "inBoxBulk": "0",
                                                "inBoxWidth": "0",
                                                "inBoxWeight": "0",
                                                "inBoxSideSum": "0",
                                                "boxInCnt": "0",
                                                "inBoxInCnt": "0",
                                                "pltInCnt": "0",
                                                "origin": "",
                                                "distTermMgtYn": "N",
                                                "useTermDay": "0",
                                                "outCanDay": "0",
                                                "inCanDay": "0",
                                                "boxDiv": "1",
                                                "bufGodYn": "Y",
                                                "loadingDirection": "NONE",
                                                "firstInDt": null,
                                                "useYn": "Y",
                                                "feeYn": "",
                                                "saleUnitQty": "1",
                                                "cstOneDayDeliveryYn": null,
                                                "safetyStock": "1"
                                              }
                                            ]
                                          },
                                          "message": "파스토 상품 목록 조회 성공"
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
                                          "timestamp": "2026-01-30T12:12:30.013",
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
                                          "timestamp": "2026-01-30T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        })
public @interface GetFulfillmentGoodsApiDocs {
}
