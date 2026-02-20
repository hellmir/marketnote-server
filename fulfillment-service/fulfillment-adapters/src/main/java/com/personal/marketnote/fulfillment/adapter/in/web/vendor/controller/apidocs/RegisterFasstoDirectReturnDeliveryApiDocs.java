package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs;

import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFasstoDirectReturnDeliveryRequest;
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
        summary = "(관리자) 파스토 반품 택배사 미지정 등록 요청",
        description = """
                작성일자: 2026-02-20
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                파스토 반품 택배사 미지정 등록(택배사 예약 없거나 다른 택배사 사용 시)을 요청합니다.
                
                ---
                
                ## Request
                
                | **키** | **위치** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- | --- |
                | accessToken | header | string | 파스토 액세스 토큰 | Y | 23ea2ab4f0e111f0be620ab49498ff55 |
                | customerCode | path | string | 파스토 고객사 코드 | Y | 94388 |
                
                ---
                
                ## Request Body (Array)
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | ordDt | string | 반품예정일 | Y | "20260204" |
                | supCd | string | 공급처코드 | N | "SUP001" |
                | orgParcelCd | string | 원택배사코드 | Y | "04" |
                | orgInvoiceNo | string | 원송장번호 | Y | "1234567890" |
                | inWay | string | 반품방식(01:택배, 02:차량) | Y | "01" |
                | custNm | string | 고객명 | Y | "홍길동" |
                | rtnParcelComp | string | 반품택배사명(택배사코드 사용하지 않음) | N | "CJ대한통운" |
                | rtnInvoiceNo | string | 반품송장번호 | N | "9876543210" |
                | rtnGubun | string | 반품 구분코드(01:반품, 02:교환, 03:환불) | Y | "01" |
                | rtnReason | string | 반품 사유 코드(01~10, 99) | Y | "07" |
                | rtnDetailReason | string | 반품 사유 상세내용 | N | "상품 파손" |
                | remark | string | 비고 | N | "취급주의" |
                | godCds | array | 반품 대상 상품 목록 | N | [ ... ] |
                
                ---
                
                ### Request Body > godCds
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | cstGodCd | string | 고객사상품번호 | Y | "PROD001" |
                | distTermDt | string | 유통기한 | N | "" |
                | ordQty | number | 주문수량 | Y | 1 |
                
                ---
                
                ## 반품 사유 코드 참조
                
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
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 201: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-02-20T12:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "파스토 반품 택배사 미지정 등록 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | dataCount | number | 등록 건수 | 1 |
                | deliveries | array | 반품 등록 결과 | [ ... ] |
                
                ---
                
                ### Response > content > deliveries
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | fmsSlipNo | string | FMS 요청번호 | "TESTRI260204000001" |
                | orderNo | string | 주문번호 | "" |
                | msg | string | 처리 메시지 | "반품요청 등록 성공" |
                | code | string | 처리 코드 | "200" |
                | outOfStockGoodsDetail | object | 재고부족 상품 상세 | null |
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
                )
        },
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = RegisterFasstoDirectReturnDeliveryRequest.class),
                        examples = @ExampleObject("""
                                [
                                  {
                                    "ordDt": "20260204",
                                    "supCd": "SUP001",
                                    "orgParcelCd": "04",
                                    "orgInvoiceNo": "1234567890",
                                    "inWay": "01",
                                    "custNm": "홍길동",
                                    "rtnParcelComp": "CJ대한통운",
                                    "rtnInvoiceNo": "9876543210",
                                    "rtnGubun": "01",
                                    "rtnReason": "07",
                                    "rtnDetailReason": "상품 파손",
                                    "remark": "취급주의",
                                    "godCds": [
                                      {
                                        "cstGodCd": "PROD001",
                                        "distTermDt": "",
                                        "ordQty": 1
                                      }
                                    ]
                                  }
                                ]
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "파스토 반품 택배사 미지정 등록 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 201,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-20T12:00:00.000",
                                          "content": {
                                            "dataCount": 1,
                                            "deliveries": [
                                              {
                                                "fmsSlipNo": "TESTRI260204000001",
                                                "orderNo": "",
                                                "msg": "반품요청 등록 성공",
                                                "code": "200",
                                                "outOfStockGoodsDetail": null
                                              }
                                            ]
                                          },
                                          "message": "파스토 반품 택배사 미지정 등록 성공"
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
                                          "timestamp": "2026-02-20T12:00:00.000",
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
                                          "timestamp": "2026-02-20T12:00:00.000",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        })
public @interface RegisterFasstoDirectReturnDeliveryApiDocs {
}
