package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs;

import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFulfillmentWarehousingRequest;
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
        summary = "(관리자) 파스토 상품 입고 요청",
        description = """
                작성일자: 2026-01-31
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                파스토 상품 입고 요청을 등록합니다.
                
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
                | ordDt | string | 입고 요청일 | Y | "2026-01-31" |
                | ordNo | string | 주문번호 | N | "" |
                | inWay | string | 입고방법(01:택배,02:차량) | Y | "01" |
                | slipNo | string | 입고 요청번호(수정시 필수) | N | "" |
                | parcelComp | string | 택배사명 | N | "" |
                | parcelInvoiceNo | string | 송장번호 | N | "" |
                | remark | string | 입고시 참고사항 | N | "" |
                | cstSupCd | string | 고객사공급사코드 | N | "" |
                | distTermDt | string | 유통기한지정일 (YYYY-MM-DD) | N | "" |
                | makeDt | string | 제조일자 (YYYY-MM-DD) | N | "" |
                | preArv | string | 사전도착여부 | N | "" |
                | godCds | array | 입고 상품 코드 목록 | Y | [ ... ] |
                
                ---
                
                ### Request Body > godCds
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | cstGodCd | string | 고객사상품번호 | Y | "1" |
                | distTermDt | string | 유통기한 | N | "" |
                | ordQty | number | 주문수량(본품일경우) | Y | 1 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-31T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "파스토 상품 입고 요청 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | dataCount | number | 등록 건수 | 1 |
                | warehousing | array | 입고 요청 결과 | [ ... ] |
                
                ---
                
                ### Response > content > warehousing
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | msg | string | 처리 결과 | "SUCCESS" |
                | code | string | 응답 코드 | "200" |
                | slipNo | string | 입고 요청번호 | "IN2401120001" |
                | ordNo | string | 주문번호 | "ORD-20260112-01" |
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
                        schema = @Schema(implementation = RegisterFulfillmentWarehousingRequest.class),
                        examples = @ExampleObject("""
                                [
                                  {
                                    "ordDt": "2026-01-31",
                                    "ordNo": "",
                                    "inWay": "01",
                                    "slipNo": "",
                                    "parcelComp": "",
                                    "parcelInvoiceNo": "",
                                    "remark": "",
                                    "cstSupCd": "",
                                    "distTermDt": "",
                                    "makeDt": "",
                                    "preArv": "",
                                    "godCds": [
                                      {
                                        "cstGodCd": "1",
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
                        description = "파스토 상품 입고 요청 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 201,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-31T16:19:04.950116",
                                          "content": {
                                            "dataCount": 0,
                                            "warehousing": [
                                              {
                                                "msg": "입고 등록 성공",
                                                "code": "200",
                                                "slipNo": null,
                                                "ordNo": null
                                              }
                                            ]
                                          },
                                          "message": "파스토 상품 입고 요청 성공"
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
                                          "timestamp": "2026-01-31T12:12:30.013",
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
                                          "timestamp": "2026-01-31T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        })
public @interface RegisterFulfillmentWarehousingApiDocs {
}
