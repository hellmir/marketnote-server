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
        summary = "(관리자) 파스토 입고 검수 상세 조회",
        description = """
                작성일자: 2026-02-17
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                파스토 입고 검수 상세 정보를 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **위치** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- | --- |
                | accessToken | header | string | 파스토 액세스 토큰 | Y | eefae1befa4c11f0be620ab49498ff55 |
                | customerCode | path | string | 파스토 고객사 코드 | Y | 94388 |
                | slipNo | path | string | 입고요청번호 | Y | TESTIO260130000001 |
                | whCd | path | string | 센터 | Y | TEST |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-02-17T10:20:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "파스토 입고 검수 상세 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | dataCount | number | 조회 건수 | 1 |
                | details | array | 입고 검수 상세 목록 | [ ... ] |
                
                ---
                
                ### Response > content > details
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | ordDt | string | 입고일자 | "20260130" |
                | whCd | string | 창고코드 | "TEST" |
                | whNm | string | 창고명 | "테스트" |
                | slipNo | string | 전표번호(입고요청번호) | "TESTIO260130000001" |
                | cstCd | string | 고객사코드 | "94388" |
                | cstNm | string | 고객사명 | "마켓노트 주식회사 테스트" |
                | supCd | string | 파스토공급사코드 | "99999999" |
                | supNm | string | 공급사명 | "미지정 공급사" |
                | inWay | string | 입고방식(01:택배,02:차량) | "01" |
                | inWayNm | string | 입고방식명 | "택배" |
                | godCd | string | 상품코드 | "943881" |
                | ordQty | number | 입고 요청 수량 | 1 |
                | totInQty | number | 검수 수량 | 1 |
                | parcelComp | string | 택배사명 | "" |
                | parcelInvoiceNo | string | 입고시 송장번호 | "" |
                | wrkStat | string | 작업상태코드(1:입고 반품 예정(지연),2:입고 검수 진행,3:입고 확정,4:입고 완료,5:입고 취소) | "2" |
                | wrkStatNm | string | 작업상태명 | "입고 검수 진행" |
                | remark | string | 입고요청내용 | "" |
                | goods | array | 상품 정보 | ["943881"] |
                | goodsSerialNo | array | 상품일련번호 | ["SN-001"] |
                | externalGodImgUrl | string | 상품이미지url | "https://cdn.personal.marketnote/images/sample.png" |
                | distTermDt | string | 유통기한 | "2026-12-31" |
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
                        schema = @Schema(type = "string", example = "TESTIO260130000001")
                ),
                @Parameter(
                        name = "whCd",
                        description = "센터",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "TEST")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "파스토 입고 검수 상세 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-17T10:20:30.013",
                                          "content": {
                                            "dataCount": 1,
                                            "details": [
                                              {
                                                "ordDt": "20260130",
                                                "whCd": "TEST",
                                                "whNm": "테스트",
                                                "slipNo": "TESTIO260130000001",
                                                "cstCd": "94388",
                                                "cstNm": "마켓노트 주식회사 테스트",
                                                "supCd": "99999999",
                                                "supNm": "미지정 공급사",
                                                "inWay": "01",
                                                "inWayNm": "택배",
                                                "godCd": "943881",
                                                "ordQty": 1,
                                                "totInQty": 1,
                                                "parcelComp": "",
                                                "parcelInvoiceNo": "",
                                                "wrkStat": "2",
                                                "wrkStatNm": "입고 검수 진행",
                                                "remark": "",
                                                "goods": ["943881"],
                                                "goodsSerialNo": ["SN-001"],
                                                "externalGodImgUrl": "https://cdn.personal.marketnote/images/sample.png",
                                                "distTermDt": "2026-12-31"
                                              }
                                            ]
                                          },
                                          "message": "파스토 입고 검수 상세 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetFulfillmentWarehousingInspecDetailApiDocs {
}
