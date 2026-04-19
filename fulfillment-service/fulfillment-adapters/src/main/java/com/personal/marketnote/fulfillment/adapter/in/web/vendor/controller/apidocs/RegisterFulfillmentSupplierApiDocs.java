package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller.apidocs;

import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFulfillmentSupplierRequest;
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
        summary = "(관리자) 파스토 공급사 등록 요청",
        description = """
                작성일자: 2026-01-26
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                파스토 공급사 등록을 요청합니다. 공급사 코드는 등록 시 자동 생성됩니다.
                
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
                | supNm | string | 공급사명 | Y | "테스트 공급사1" |
                | cstSupCd | string | 고객사 공급사 코드 | N | "" |
                | useYn | string | 사용여부 | N | "" |
                | dealStrDt | string | 거래시작일자 | N | "" |
                | dealEndDt | string | 거래종료일자 | N | "" |
                | zipNo | string | 우편번호 | N | "12345" |
                | addr1 | string | 주소1 | N | "서울특별시 양천구 목동동로 123 132동 101호" |
                | addr2 | string | 주소2 | N | "서울특별시 양천구 목동동로 123 132동 102호" |
                | ceoNm | string | 대표자명 | N | "고길동" |
                | busNo | string | 사업자번호 | N | "7654321" |
                | busSp | string | 업태 | N | "" |
                | busTp | string | 업종 | N | "" |
                | telNo | string | 전화번호 | N | "01012345678" |
                | faxNo | string | 팩스번호 | N | "0212345678" |
                | empNm1 | string | 담당자명1 | N | "서영락" |
                | empPosit1 | string | 담당자직위1 | N | "대리" |
                | empTelNo1 | string | 담당자전화번호1 | N | "0212345678" |
                | empEmail1 | string | 담당자이메일1 | N | "abc@abc.com" |
                | empNm2 | string | 담당자명2 | N | "오상식" |
                | empPosit2 | string | 담당자직위2 | N | "차장" |
                | empTelNo2 | string | 담당자전화번호2 | N | "0212345678" |
                | empEmail2 | string | 담당자이메일2 | N | "abc@abc.com" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-26T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "파스토 공급사 등록 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | supplierInfo | object | 공급사 등록 결과 | { ... } |
                
                ---
                
                ### Response > content > supplierInfo
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | msg | string | 처리 결과 | "SUCCESS" |
                | code | string | 응답 코드 | "200" |
                | supCd | string | 공급사 코드 | "00000001" |
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
                        schema = @Schema(implementation = RegisterFulfillmentSupplierRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "cstSupCd": "",
                                  "supNm": "테스트 공급사1",
                                  "useYn": "",
                                  "dealStrDt": "",
                                  "dealEndDt": "",
                                  "zipNo": "12345",
                                  "addr1": "서울특별시 양천구 목동동로 123 132동 101호",
                                  "addr2": "서울특별시 양천구 목동동로 123 132동 102호",
                                  "ceoNm": "고길동",
                                  "busNo": "7654321",
                                  "busSp": "",
                                  "busTp": "",
                                  "telNo": "01012345678",
                                  "faxNo": "0212345678",
                                  "empNm1": "서영락",
                                  "empPosit1": "대리",
                                  "empTelNo1": "0212345678",
                                  "empEmail1": "abc@abc.com",
                                  "empNm2": "오상식",
                                  "empPosit2": "차장",
                                  "empTelNo2": "0212345678",
                                  "empEmail2": "abc@abc.com"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "파스토 공급사 등록 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 201,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-26T09:07:08.013",
                                          "content": {
                                            "supplierInfo": {
                                              "msg": "SUCCESS",
                                              "code": "200",
                                              "supCd": "00000001"
                                            }
                                          },
                                          "message": "파스토 공급사 등록 성공"
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
                                          "timestamp": "2026-01-26T12:12:30.013",
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
                                          "timestamp": "2026-01-26T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        })
public @interface RegisterFulfillmentSupplierApiDocs {
}
