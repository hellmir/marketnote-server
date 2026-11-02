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
        summary = "(관리자) 파스토 모음상품 상세 정보 조회",
        description = """
                작성일자: 2026-01-31
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                파스토 모음상품 상세 정보를 조회합니다.
                
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
                | timestamp | string(datetime) | 응답 일시 | "2026-01-31T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "파스토 모음상품 상세 정보 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | dataCount | number | 조회 건수 | 0 |
                | elements | array | 모음상품 목록 | [ ... ] |
                
                ---
                
                ### Response > content > elements
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | godCd | string | 상품코드 | "943881" |
                | cstGodCd | string | 고객사상품코드 | "1" |
                | godNm | string | 상품명 | "테스트 모음상품" |
                | useYn | string | 사용여부 | "Y" |
                | elementList | array | 구성상품 목록 | [ ... ] |
                
                ---
                
                ### Response > content > elements > elementList
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | godCd | string | 상품코드 | "00000001" |
                | cstGodCd | string | 고객사상품코드 | "1" |
                | godBarcd | string | 상품바코드 | "ABC123" |
                | godNm | string | 상품명 | "구성 상품1" |
                | godType | string | 상품유형 | "1" |
                | godTypeNm | string | 상품유형명 | "단품" |
                | qty | number | 수량 | 1 |
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
                        description = "파스토 모음상품 상세 정보 조회 성공",
                        content = @Content(
                                examples = {
                                        @ExampleObject(
                                                name = "empty",
                                                value = """
                                                        {
                                                          "statusCode": 200,
                                                          "code": "SUC01",
                                                          "timestamp": "2026-01-31T12:12:30.013",
                                                          "content": {
                                                            "dataCount": 0,
                                                            "elements": []
                                                          },
                                                          "message": "파스토 모음상품 상세 정보 조회 성공"
                                                        }
                                                        """
                                        ),
                                        @ExampleObject(
                                                name = "sample",
                                                value = """
                                                        {
                                                          "statusCode": 200,
                                                          "code": "SUC01",
                                                          "timestamp": "2026-01-31T12:12:30.013",
                                                          "content": {
                                                            "dataCount": 1,
                                                            "elements": [
                                                              {
                                                                "godCd": "943881",
                                                                "cstGodCd": "1",
                                                                "godNm": "테스트 모음상품",
                                                                "useYn": "Y",
                                                                "elementList": [
                                                                  {
                                                                    "godCd": "00000001",
                                                                    "cstGodCd": "1",
                                                                    "godBarcd": "ABC123",
                                                                    "godNm": "구성 상품1",
                                                                    "godType": "1",
                                                                    "godTypeNm": "단품",
                                                                    "qty": 1
                                                                  }
                                                                ]
                                                              }
                                                            ]
                                                          },
                                                          "message": "파스토 모음상품 상세 정보 조회 성공"
                                                        }
                                                        """
                                        )
                                }
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
public @interface GetFulfillmentGoodsElementsApiDocs {
}
