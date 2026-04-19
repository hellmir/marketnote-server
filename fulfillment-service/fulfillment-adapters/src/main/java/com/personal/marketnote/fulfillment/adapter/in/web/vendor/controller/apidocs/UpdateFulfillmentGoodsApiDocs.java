package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs;

import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.UpdateFulfillmentGoodsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "(관리자) 파스토 상품 수정 요청",
        description = """
                작성일자: 2026-01-30
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                파스토 상품 정보를 수정합니다.
                
                ---
                
                ## Request
                
                | **키** | **위치** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- | --- |
                | accessToken | header | string | 파스토 액세스 토큰 | Y | your-access-token |
                | customerCode | path | string | 파스토 고객사 코드 | Y | 00000 |
                
                ---
                
                ## Request Body (Array)
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | cstGodCd | string | 고객사상품코드 | Y | "1" |
                | godNm | string | 상품명 | Y | "테스트 상품2" |
                | godType | string | 상품유형(1:단일, 2:모음, 3:세트, 4:대표상품) | Y | "1" |
                | giftDiv | string | 사은품구분(01:본품, 02:사은품, 03:부자재) | Y | "01" |
                | godOptCd1 | string | 상품옵션코드1 | N | "" |
                | godOptCd2 | string | 상품옵션코드2 | N | "" |
                | invGodNmUseYn | string | 송장출력용 상품명 사용여부 (Y/N) | N | "" |
                | invGodNm | string | 송장출력용 상품명 | N | "" |
                | supCd | string | 공급사코드 | N | "" |
                | cateCd | string | 카테고리코드 | N | "" |
                | seasonCd | string | 계절상품 코드(0:공용, 1:S/S, 2:F/W) | N | "" |
                | genderCd | string | 성별상품 코드(A:Unisex,M:menm,W:woman,K:kids) | N | "" |
                | makeYr | string | 연식 | N | "" |
                | godPr | string | 단가 | N | "" |
                | inPr | string | 공급가 | N | "" |
                | salPr | string | 판매가 | N | "" |
                | dealTemp | string | 취급온도 | N | "" |
                | pickFac | string | 피킹설비 | N | "" |
                | godBarcd | string | 상품바코드 | N | "" |
                | boxWeight | string | 내품BOX무게 | N | "" |
                | origin | string | 원산지 | N | "" |
                | distTermMgtYn | string | 유통기한관리여부 | N | "" |
                | useTermDay | string | 사용기한 | N | "" |
                | outCanDay | string | 출고가능일수 | N | "" |
                | inCanDay | string | 입고가능일수 | N | "" |
                | boxDiv | string | 출고박스 | N | "" |
                | bufGodYn | string | 완충상품여부(Y:사용,N:미사용,A:추가완충제) | N | "" |
                | loadingDirection | string | 출고박스 상품 적재 기준(NONE:관계없음, UP:세워서 적재) | N | "" |
                | subMate | string | 부자재코드(01:홍보물, 02:출력물, 03:쇼핑백, 04:포장지, 05:고객사테이프, 06:케이스, 07:단상자, 08:세트제작용 부자재, 09:출고용 패킹박스, 10:습자지, 11:고객사인박스) | N | "" |
                | useYn | string | 사용여부(Y/N) | N | "" |
                | safetyStock | string | 안전재고수량 | N | "" |
                | feeYn | string | 요금적용여부 | N | "" |
                | saleUnitQty | string | 상품판매단위 | N | "" |
                | cstGodImgUrl | string | 고객상품이미지URL | N | "" |
                | externalGodImgUrl | string | 상품이미지 URL | N | "" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-30T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "파스토 상품 수정 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | dataCount | number | 수정 건수 | 1 |
                | goods | array | 상품 정보 수정 결과 | [ ... ] |
                
                ---
                
                ### Response > content > goods
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | fmsSlipNo | string | 운송장 번호 | null |
                | orderNo | string | 주문 번호 | null |
                | msg | string | 처리 결과 | "상품 정보 수정 성공" |
                | code | string | 응답 코드 | "200" |
                | outOfStockGoodsDetail | object | 품절 상품 상세 | null |
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
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = UpdateFulfillmentGoodsRequest.class),
                        examples = @ExampleObject("""
                                [
                                  {
                                    "cstGodCd": "1",
                                    "godNm": "테스트 상품2",
                                    "godType": "1",
                                    "giftDiv": "01",
                                    "godOptCd1": "",
                                    "godOptCd2": "",
                                    "invGodNmUseYn": "",
                                    "invGodNm": "",
                                    "supCd": "",
                                    "cateCd": "",
                                    "seasonCd": "",
                                    "genderCd": "",
                                    "makeYr": "",
                                    "godPr": "",
                                    "inPr": "",
                                    "salPr": "",
                                    "dealTemp": "",
                                    "pickFac": "",
                                    "godBarcd": "",
                                    "boxWeight": "",
                                    "origin": "",
                                    "distTermMgtYn": "",
                                    "useTermDay": "",
                                    "outCanDay": "",
                                    "inCanDay": "",
                                    "boxDiv": "",
                                    "bufGodYn": "",
                                    "loadingDirection": "",
                                    "subMate": "",
                                    "useYn": "",
                                    "safetyStock": "",
                                    "feeYn": "",
                                    "saleUnitQty": "",
                                    "cstGodImgUrl": "",
                                    "externalGodImgUrl": ""
                                  }
                                ]
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "파스토 상품 수정 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-30T04:53:08.013",
                                          "content": {
                                            "dataCount": 1,
                                            "goods": [
                                              {
                                                "fmsSlipNo": null,
                                                "orderNo": null,
                                                "msg": "상품 정보 수정 성공",
                                                "code": "200",
                                                "outOfStockGoodsDetail": null
                                              }
                                            ]
                                          },
                                          "message": "파스토 상품 수정 성공"
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
public @interface UpdateFulfillmentGoodsApiDocs {
}
