package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs;

import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFulfillmentDeliveryIcsRequest;
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
        summary = "(관리자) 파스토 출고 등록(해외) 요청",
        description = """
                작성일자: 2026-02-18
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                파스토 출고 등록(해외)을 요청합니다.
                
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
                | ordDt | string | 요청일자(등록/수정시 필수) | Y | "20260202" |
                | ordNo | string | 주문번호(등록/수정시 필수) | Y | "ICS-001" |
                | platform | string | 쇼핑몰명(QOO10, SHOPEE …) | Y | "QOO10" |
                | logiCenter | string | 배송센터(DRA, ICS …) | Y | "ICS" |
                | invoiceNo | string | 운송장번호 | Y | "INV-123" |
                | custNm | string | 배송 고객명 | N | "홍길동" |
                | custTelNo | string | 배송 고객번호 | N | "01012345678" |
                | custAddr | string | 배송주소 | N | "서울" |
                | sendNm | string | 보내는분 | N | "" |
                | sendTelNo | string | 보내는분전화번호 | N | "" |
                | salChanel | string | 판매채널(큐텐, 쇼피…) | N | "QOO10" |
                | shipReqTerm | string | 배송요청사항 | N | "" |
                | remark | string | 비고(파스토에 요청하거나 공유할 내용) | N | "" |
                | godCds | array | 출고 상품 코드 목록 | Y | [ ... ] |
                
                ---
                
                ### Request Body > godCds
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | cstGodCd | string | 고객사상품번호 | Y | "1" |
                | ordQty | number | 주문수량 | Y | 2 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-02-18T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "파스토 출고 등록(해외) 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | dataCount | number | 등록 건수 | 0 |
                | deliveries | array | 출고 등록 결과 | [ ... ] |
                
                ---
                
                ### Response > content > deliveries
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | fmsSlipNo | string | 전표번호 | "ICSOO260202000001" |
                | orderNo | string | 주문번호 | "ICS-001" |
                | msg | string | 처리 메시지 | "출고요청 등록 성공" |
                | code | string | 처리 코드 | "200" |
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
                        schema = @Schema(type = "array", implementation = RegisterFulfillmentDeliveryIcsRequest.class),
                        examples = @ExampleObject("""
                                [
                                  {
                                    "ordDt": "20260202",
                                    "ordNo": "ICS-001",
                                    "platform": "QOO10",
                                    "logiCenter": "ICS",
                                    "invoiceNo": "INV-123",
                                    "custNm": "홍길동",
                                    "custTelNo": "01012345678",
                                    "custAddr": "서울",
                                    "sendNm": "",
                                    "sendTelNo": "",
                                    "salChanel": "QOO10",
                                    "shipReqTerm": "",
                                    "remark": "",
                                    "godCds": [
                                      {
                                        "cstGodCd": "1",
                                        "ordQty": 2
                                      }
                                    ]
                                  }
                                ]
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "파스토 출고 등록(해외) 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-18T12:12:30.013",
                                          "content": {
                                            "dataCount": 0,
                                            "deliveries": [
                                              {
                                                "fmsSlipNo": "ICSOO260202000001",
                                                "orderNo": "ICS-001",
                                                "msg": "출고요청 등록 성공",
                                                "code": "200",
                                                "outOfStockGoodsDetail": null
                                              }
                                            ]
                                          },
                                          "message": "파스토 출고 등록(해외) 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface RegisterFulfillmentDeliveryIcsApiDocs {
}
