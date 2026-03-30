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
        summary = "(관리자) 파스토 출고 배송 조회",
        description = """
                작성일자: 2026-02-13
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                파스토 출고 배송 상태를 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **위치** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- | --- |
                | accessToken | header | string | 파스토 액세스 토큰 | Y | 7cd04d84f74911f0be620ab49498ff55 |
                | customerCode | path | string | 파스토 고객사 코드 | Y | 94388 |
                | startDate | path | string | 검색 시작일(YYYY-MM-DD) | Y | 2026-02-11 |
                | endDate | path | string | 검색 종료일(YYYY-MM-DD) | Y | 2026-02-13 |
                | outDiv | path | string | 출고 구분(ALL:전체, 1:택배, 2:차량배송, COUPANG:쿠팡쉽먼트, ONE_DAY:원데이배송) | Y | ALL |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-02-13T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "파스토 출고 배송 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | dataCount | number | 조회 건수 | 1 |
                | deliveryStatuses | array | 출고 배송 상태 목록 | [ ... ] |
                
                ---
                
                ### Response > content > deliveryStatuses
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | boxDiv | string | 박스타입 | "" |
                | boxDivNm | string | 박스명 | "" |
                | boxNm | string | 박스 규격명 | "" |
                | boxNo | string | 박스 고유번호 | "" |
                | boxTp | string | 박스 규격 사이즈 | "" |
                | crgSt | string | 배송상태코드 | "" |
                | crgStNm | string | 배송상태명 | "" |
                | cstCd | string | 고객사코드 | "94388" |
                | cstNm | string | 고객사명 | "마켓노트 주식회사 테스트" |
                | custAddr | string | 배송 주소 | "" |
                | custNm | string | 배송 고객명 | "" |
                | custTelNo | string | 배송 전화번호 | "" |
                | dlvMisYn | string | 오배송 여부(Y/N) | "N" |
                | godCd | string | 상품코드 | "" |
                | godNm | string | 상품명 | "" |
                | invoiceNo | string | 운송장번호 | "" |
                | ordNo | string | 주문번호 | "" |
                | ordSeq | string | 주문순번 | "" |
                | outDiv | string | 출고방법 코드 | "" |
                | outDivNm | string | 출고방법 명 | "" |
                | outOrdSlipNo | string | 출고요청번호 | "" |
                | packDt | string | 검수패킹일 | "" |
                | packQty | string | 패킹 수량 | "" |
                | packSeq | string | 패킹 순번 | "" |
                | parcelCd | string | 택배사 코드 | "" |
                | parcelLinkYn | string | 택배사 사용여부 | "" |
                | parcelNm | string | 택배사명 | "" |
                | pickSeq | string | 피킹순번 | "" |
                | postYn | string | 주문순번 | "N" |
                | printCnt | string | 리얼패킹영상 출력횟수 | "" |
                | rtnAddr1 | string | 반품주소 | "" |
                | rtnAddr2 | string | 반품주소2 | "" |
                | rtnCheck | string | 반품여부 | "" |
                | rtnEmpNm | string | 반품 담당자명 | "" |
                | rtnOrdDt | string | 반품일 | "" |
                | rtnTelNo | string | 반품주소지 전화번호 | "" |
                | rtnZipCd | string | 반품 우편번호 | "" |
                | salChanel | string | 판매채널 | "" |
                | shipReqTerm | string | 배송요청사항 | "" |
                | shopCd | string | 공급사코드 | "" |
                | shopNm | string | 공급사명 | "" |
                | sku | string | 수량 | "" |
                | whCd | string | 창고코드 | "" |
                | goodsSerialNo | array | 상품 시리얼 목록 | [] |
                | supCd | string | 공급사코드 | "" |
                | supNm | string | 공급사명 | "" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "accessToken",
                        description = "파스토 액세스 토큰",
                        in = ParameterIn.HEADER,
                        required = true,
                        schema = @Schema(type = "string", example = "7cd04d84f74911f0be620ab49498ff55")
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
                        description = "검색 시작일(YYYY-MM-DD)",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "2026-02-11")
                ),
                @Parameter(
                        name = "endDate",
                        description = "검색 종료일(YYYY-MM-DD)",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "2026-02-13")
                ),
                @Parameter(
                        name = "outDiv",
                        description = "출고 구분(ALL:전체, 1:택배, 2:차량배송, COUPANG:쿠팡쉽먼트, ONE_DAY:원데이배송)",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "ALL")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "파스토 출고 배송 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-13T12:12:30.013",
                                          "content": {
                                            "dataCount": 0,
                                            "deliveryStatuses": []
                                          },
                                          "message": "파스토 출고 배송 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetFulfillmentDeliveryStatusesApiDocs {
}
