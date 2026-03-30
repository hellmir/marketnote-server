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
        summary = "(관리자) 파스토 공급사 목록 조회",
        description = """
                작성일자: 2026-01-26
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                파스토 공급사 목록을 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **위치** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- | --- |
                | accessToken | header | string | 파스토 액세스 토큰 | Y | 73c1195ae79411f0be620ab49498ff55 |
                | customerCode | path | string | 파스토 고객사 코드 | Y | 94388 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-26T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "파스토 공급사 목록 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | dataCount | number | 조회 건수 | 1 |
                | suppliers | array | 공급사 목록 | [ ... ] |
                
                ---
                
                ### Response > content > suppliers
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | supCd | string | 공급사 코드 | "94388001" |
                | supNm | string | 공급사명 | "테스트 공급사1" |
                | cstSupCd | string | 고객사 공급사 코드 | "" |
                | cstCd | string | 고객사 코드 | "94388" |
                | cstNm | string | 고객사명 | "마켓노트 주식회사 테스트" |
                | dealStrDt | string | 거래 시작일자 | "" |
                | dealEndDt | string | 거래 종료일자 | "" |
                | zipNo | string | 우편번호 | "12345" |
                | addr1 | string | 주소1 | "서울특별시 양천구 목동동로 123 132동 101호" |
                | addr2 | string | 주소2 | "서울특별시 양천구 목동동로 123 132동 102호" |
                | ceoNm | string | 대표자명 | "고길동" |
                | busNo | string | 사업자번호 | "7654321" |
                | busSp | string | 업태 | "" |
                | busTp | string | 업종 | "" |
                | telNo | string | 전화번호 | "01012345678" |
                | faxNo | string | 팩스번호 | "0212345678" |
                | empNm1 | string | 담당자명1 | "서영락" |
                | empPosit1 | string | 담당자직위1 | "대리" |
                | empTelNo1 | string | 담당자전화번호1 | "0212345678" |
                | empEmail1 | string | 담당자이메일1 | "abc@abc.com" |
                | empNm2 | string | 담당자명2 | "오상식" |
                | empPosit2 | string | 담당자직위2 | "차장" |
                | empTelNo2 | string | 담당자전화번호2 | "0212345678" |
                | empEmail2 | string | 담당자이메일2 | "abc@abc.com" |
                | useYn | string | 사용여부 | "Y" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "accessToken",
                        description = "파스토 액세스 토큰",
                        in = ParameterIn.HEADER,
                        required = true,
                        schema = @Schema(type = "string", example = "73c1195ae79411f0be620ab49498ff55")
                ),
                @Parameter(
                        name = "customerCode",
                        description = "파스토 고객사 코드",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "94388")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "파스토 공급사 목록 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-26T14:28:11.83625",
                                          "content": {
                                            "dataCount": 3,
                                            "suppliers": [
                                              {
                                                "supCd": "94388001",
                                                "supNm": "테스트 공급사1",
                                                "cstSupCd": "",
                                                "cstCd": "94388",
                                                "cstNm": "마켓노트 주식회사 테스트",
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
                                                "empEmail2": "abc@abc.com",
                                                "useYn": "Y"
                                              },
                                              {
                                                "supCd": "94388002",
                                                "supNm": "테스트 공급사2",
                                                "cstSupCd": "",
                                                "cstCd": "94388",
                                                "cstNm": "마켓노트 주식회사 테스트",
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
                                                "empEmail2": "abc@abc.com",
                                                "useYn": "Y"
                                              },
                                              {
                                                "supCd": "94388003",
                                                "supNm": "테스트 공급사3",
                                                "cstSupCd": "",
                                                "cstCd": "94388",
                                                "cstNm": "마켓노트 주식회사 테스트",
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
                                                "empEmail2": "abc@abc.com",
                                                "useYn": "Y"
                                              }
                                            ]
                                          },
                                          "message": "파스토 공급사 목록 조회 성공"
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
public @interface GetFulfillmentSuppliersApiDocs {
}
