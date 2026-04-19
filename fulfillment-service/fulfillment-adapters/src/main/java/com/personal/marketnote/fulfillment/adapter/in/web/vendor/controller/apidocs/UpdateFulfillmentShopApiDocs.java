package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs;

import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.UpdateFulfillmentShopRequest;
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
        summary = "(관리자) 파스토 출고처 수정 요청",
        description = """
                작성일자: 2026-01-26
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                파스토 출고처 정보를 수정합니다.
                
                ---
                
                ## Request
                
                | **키** | **위치** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- | --- |
                | accessToken | header | string | 파스토 액세스 토큰 | Y | your-access-token |
                | customerCode | path | string | 파스토 고객사 코드 | Y | 00000 |
                
                ---
                
                ## Request Body
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | shopCd | string | 출고처 코드(수정 시 필수) | Y | "00000001" |
                | shopNm | string | 출고처명 | Y | "테스트 출고처1" |
                | cstShopCd | string | 고객사 출고처 코드 | N | "" |
                | dealStrDt | string | 거래 시작일자 | N | "" |
                | dealEndDt | string | 거래 종료일자 | N | "" |
                | zipNo | string | 우편번호 | N | "12345" |
                | addr1 | string | 주소1 | N | "서울특별시 양천구 목동동로 123 132동 101호" |
                | addr2 | string | 주소2 | N | "서울특별시 양천구 목동동로 123 132동 102호" |
                | ceoNm | string | 대표자명 | N | "홍길동" |
                | busNo | string | 사업자번호 | N | "1234567" |
                | telNo | string | 전화번호 | N | "01012345678" |
                | unloadWay | string | 하차방식(01: 지게차, 02: 수작업) | N | "02" |
                | checkWay | string | 검수방식(01: 전수검수, 02: 샘플검수) | N | "02" |
                | standYn | string | 대기여부 | N | "N" |
                | formType | string | 거래명세서 양식(STDF001: 택배기본, STDF002: 차량기본) | N | "STDF001" |
                | empNm | string | 담당자명 | N | "서영락" |
                | empPosit | string | 담당자직위 | N | "대리" |
                | empTelNo | string | 담당자전화번호 | N | "01012345678" |
                | useYn | string | 사용여부 | N | "Y" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-26T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "파스토 출고처 수정 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | shopInfo | object | 출고처 수정 결과 | { ... } |
                
                ---
                
                ### Response > content > shopInfo
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | msg | string | 처리 결과 | "SUCCESS" |
                | code | string | 응답 코드 | "200" |
                | shopCd | string | 출고처 코드 | "00000001" |
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
                        schema = @Schema(implementation = UpdateFulfillmentShopRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "shopCd": "00000001",
                                  "shopNm": "테스트 출고처1",
                                  "cstShopCd": "",
                                  "dealStrDt": "",
                                  "dealEndDt": "",
                                  "zipNo": "12345",
                                  "addr1": "서울특별시 양천구 목동동로 123 132동 101호",
                                  "addr2": "서울특별시 양천구 목동동로 123 132동 102호",
                                  "ceoNm": "홍길동",
                                  "busNo": "1234567",
                                  "telNo": "01012345678",
                                  "unloadWay": "02",
                                  "checkWay": "02",
                                  "standYn": "N",
                                  "formType": "STDF001",
                                  "empNm": "서영락",
                                  "empPosit": "대리",
                                  "empTelNo": "01012345678",
                                  "useYn": "Y"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "파스토 출고처 수정 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-26T10:28:54.060835",
                                          "content": {
                                            "shopInfo": {
                                              "msg": "SUCCESS",
                                              "code": "200",
                                              "shopCd": "00000007"
                                            }
                                          },
                                          "message": "파스토 출고처 수정 성공"
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
                                          "timestamp": "2026-01-26T10:12:30.013",
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
                                          "timestamp": "2026-01-26T10:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        })
public @interface UpdateFulfillmentShopApiDocs {
}
