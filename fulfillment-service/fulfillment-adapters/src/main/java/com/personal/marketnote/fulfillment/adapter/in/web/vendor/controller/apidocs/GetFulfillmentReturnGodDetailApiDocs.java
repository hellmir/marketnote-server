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
        summary = "(관리자) 파스토 반품 완료 상품 상세 목록 조회",
        description = """
                작성일자: 2026-02-20
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                파스토 반품이 완료된 상품의 상세 목록을 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **위치** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- | --- |
                | accessToken | header | string | 파스토 액세스 토큰 | Y | 23ea2ab4f0e111f0be620ab49498ff55 |
                | customerCode | path | string | 파스토 고객사 코드 | Y | 94388 |
                | strDt | query | string | 반품예정일 검색 시작일(YYYY-MM-DD), rtnSlipNoList가 없으면 필수 | 조건부 | 2026-02-01 |
                | endDt | query | string | 반품예정일 검색 종료일(YYYY-MM-DD), rtnSlipNoList가 없으면 필수 | 조건부 | 2026-02-20 |
                | rtnSlipNoList | query | string | 반품요청번호(목록), 여러 개일 경우 ','로 연결, strDt/endDt가 없으면 필수 | 조건부 | TESTRI260204000001,TESTRI260204000002 |
                | whCd | query | string | 창고 코드 | N | WH001 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-02-20T12:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "파스토 반품 완료 상품 상세 목록 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | dataCount | number | 조회 건수 | 1 |
                | returnGodInfos | array | 반품 상품 상세 정보 | [ ... ] |
                
                ---
                
                ### Response > content > returnGodInfos
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | ordNo | string | 주문번호 | "ORD20260204001" |
                | supCd | string | 공급처코드 | "SUP001" |
                | inSlipNo | string | 반품입고일 | "IN20260204001" |
                | cstCd | string | 고객사코드 | "94388" |
                | whCd | string | 창고코드 | "WH001" |
                | inOrdSlipNo | string | 반품요청번호 | "TESTRI260204000001" |
                | inOrdDt | string | 반품요청일 | "20260204" |
                | outOrdSlipNo | string | 출고요청번호 | "OUT20260204001" |
                | supNm | string | 공급처명 | "테스트공급처" |
                | custNm | string | 고객명 | "홍길동" |
                | outInvoiceNo | string | 출고송장번호 | "1234567890" |
                | rtnInvoiceNo | string | 반품송장번호 | "9876543210" |
                | inRtnPayCd | string | 운임비용지불구분코드 | "01" |
                | inRtnPayNm | string | 운임비용지불구분설명 | "고객사/구매고객이 착불로 발송" |
                | inRtnPay | string | 착불비용/현금동봉금액 | "3000" |
                | rtnMisYn | string | 오반품여부 | "N" |
                | rtnType | string | 반품 요청 구분 코드 | "1" |
                | rtnTypeNm | string | 반품 요청 구분명 | "고객반품신청" |
                | custTelNo | string | 고객 전화번호 | "01012345678" |
                | cstMemo | string | 고객사 메모 | "취급주의" |
                | rtnReason | string | 반품사유코드 | "07" |
                | rtnReasonNm | string | 반품사유설명 | "상품 파손" |
                | rtnDetailReason | string | 반품사유상세설명 | "배송 중 파손됨" |
                | rtnGubun | string | 반품신청구분코드(01:반품, 02:교환, 03:환불) | "01" |
                | rtnGubunNm | string | 반품신청구분설명 | "반품" |
                | godList | array | 반품 상품 상세 리스트 | [ ... ] |
                
                ---
                
                ### Response > content > returnGodInfos > godList
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | cstGodCd | string | 고객사상품코드 | "PROD001" |
                | godNm | string | 상품명 | "테스트상품" |
                | makeDt | string | 제조일자 | "20260101" |
                | distTermDt | string | 유통기한 | "20270101" |
                | inQty | string | 반품수량 | "1" |
                | remark | string | 반품비고 | "파손" |
                | rtnGodCheckStat | string | 반품상품검수상태코드(01:정상, 02:불량, 03:보류) | "02" |
                | rtnGodCheckStatNm | string | 반품검수상태명 | "불량" |
                
                ---
                
                ## 운임비용지불구분코드 참조
                
                | **코드** | **설명** |
                | --- | --- |
                | 01 | 고객사/구매고객이 착불로 발송 |
                | 02 | 파스토 택배사가 수거 |
                | 03 | 고객사/구매고객이 현금 동봉하여 발송 |
                | 04 | 고객사/구매고객이 자사 차량으로 직접 반송 |
                
                ---
                
                ## 반품사유코드 참조
                
                | **코드** | **설명** |
                | --- | --- |
                | 01 | 구매 의사 취소 |
                | 02 | 색상 및 사이즈 변경 |
                | 03 | 다른 상품 잘못 주문 |
                | 04 | 서비스 불만족 |
                | 05 | 배송 지연 |
                | 06 | 배송 누락 |
                | 07 | 상품 파손 |
                | 08 | 상품 정보 상이 |
                | 09 | 배송 주소 상이 |
                | 10 | 색상 등 다른상품 잘못 배송 |
                | 99 | 기타 |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "accessToken",
                        description = "파스토 액세스 토큰",
                        in = ParameterIn.HEADER,
                        required = true,
                        schema = @Schema(type = "string", example = "23ea2ab4f0e111f0be620ab49498ff55")
                ),
                @Parameter(
                        name = "customerCode",
                        description = "파스토 고객사 코드",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "94388")
                ),
                @Parameter(
                        name = "strDt",
                        description = "반품예정일 검색 시작일(YYYY-MM-DD), rtnSlipNoList가 없으면 필수",
                        in = ParameterIn.QUERY,
                        required = false,
                        schema = @Schema(type = "string", example = "2026-02-01")
                ),
                @Parameter(
                        name = "endDt",
                        description = "반품예정일 검색 종료일(YYYY-MM-DD), rtnSlipNoList가 없으면 필수",
                        in = ParameterIn.QUERY,
                        required = false,
                        schema = @Schema(type = "string", example = "2026-02-20")
                ),
                @Parameter(
                        name = "rtnSlipNoList",
                        description = "반품요청번호(목록), 여러 개일 경우 ','로 연결, strDt/endDt가 없으면 필수",
                        in = ParameterIn.QUERY,
                        required = false,
                        schema = @Schema(type = "string", example = "TESTRI260204000001,TESTRI260204000002")
                ),
                @Parameter(
                        name = "whCd",
                        description = "창고 코드",
                        in = ParameterIn.QUERY,
                        required = false,
                        schema = @Schema(type = "string", example = "WH001")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "파스토 반품 완료 상품 상세 목록 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-20T12:00:00.000",
                                          "content": {
                                            "dataCount": 1,
                                            "returnGodInfos": [
                                              {
                                                "ordNo": "ORD20260204001",
                                                "supCd": "SUP001",
                                                "inSlipNo": "IN20260204001",
                                                "cstCd": "94388",
                                                "whCd": "WH001",
                                                "inOrdSlipNo": "TESTRI260204000001",
                                                "inOrdDt": "20260204",
                                                "outOrdSlipNo": "OUT20260204001",
                                                "supNm": "테스트공급처",
                                                "custNm": "홍길동",
                                                "outInvoiceNo": "1234567890",
                                                "rtnInvoiceNo": "9876543210",
                                                "inRtnPayCd": "01",
                                                "inRtnPayNm": "고객사/구매고객이 착불로 발송",
                                                "inRtnPay": "3000",
                                                "rtnMisYn": "N",
                                                "rtnType": "1",
                                                "rtnTypeNm": "고객반품신청",
                                                "custTelNo": "01012345678",
                                                "cstMemo": "취급주의",
                                                "rtnReason": "07",
                                                "rtnReasonNm": "상품 파손",
                                                "rtnDetailReason": "배송 중 파손됨",
                                                "rtnGubun": "01",
                                                "rtnGubunNm": "반품",
                                                "godList": [
                                                  {
                                                    "cstGodCd": "PROD001",
                                                    "godNm": "테스트상품",
                                                    "makeDt": "20260101",
                                                    "distTermDt": "20270101",
                                                    "inQty": "1",
                                                    "remark": "파손",
                                                    "rtnGodCheckStat": "02",
                                                    "rtnGodCheckStatNm": "불량"
                                                  }
                                                ]
                                              }
                                            ]
                                          },
                                          "message": "파스토 반품 완료 상품 상세 목록 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetFulfillmentReturnGodDetailApiDocs {
}
