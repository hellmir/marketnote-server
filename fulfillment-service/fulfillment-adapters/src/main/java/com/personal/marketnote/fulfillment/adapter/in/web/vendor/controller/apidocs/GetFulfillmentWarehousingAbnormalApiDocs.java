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
        summary = "(관리자) 파스토 비정상 입고 상품 정보 조회",
        description = """
                작성일자: 2026-02-14
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                파스토 비정상 입고 상품 정보를 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **위치** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- | --- |
                | accessToken | header | string | 파스토 액세스 토큰 | Y | your-access-token |
                | customerCode | path | string | 파스토 고객사 코드 | Y | 00000 |
                | whCd | path | string | 센터 | Y | TEST |
                | slipNo | path | string | 입고요청번호 | Y | TESTIO260119000005 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-02-14T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "파스토 비정상 입고 상품 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | dataCount | number | 조회 건수 | 0 |
                | abnormals | array | 비정상 입고 목록 | [ ... ] |
                
                ---
                
                ### Response > content > abnormals
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | slipNo | string | 입고요청번호 | "TESTIO260119000005" |
                | goodsSerialNo | string | 상품일련번호 | "" |
                | goodsSerialStatus | string | 상품일련번호상태 | "" |
                | whCd | string | 센터 | "TEST" |
                | cstCd | string | 고객사코드 | "00000" |
                | cstNm | string | 고객사명 | "마켓노트 주식회사 테스트" |
                | godCd | string | 상품코드 | "943881" |
                | description | string | 설명 | "" |
                | remark | string | 기타메모 | "" |
                | fileSeq | string | 파일seq | "" |
                | lastFileSeqNo | number | 마지막파일no | 0 |
                | regDate | string | 비정상입고등록일자 | "" |
                | regNM | string | 비정상입고등록자 | "" |
                | updDate | string | 비정상입고수정일자 | "" |
                | updNm | string | 비정상입고수정자 | "" |
                | fileNo | string | 파일하위seq | "" |
                | imageUrl | array | 이미지 URL | [] |
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
                ),
                @Parameter(
                        name = "whCd",
                        description = "센터",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "TEST")
                ),
                @Parameter(
                        name = "slipNo",
                        description = "입고요청번호",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "TESTIO260119000005")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "파스토 비정상 입고 상품 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-14T12:12:30.013",
                                          "content": {
                                            "dataCount": 0,
                                            "abnormals": []
                                          },
                                          "message": "파스토 비정상 입고 상품 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetFulfillmentWarehousingAbnormalApiDocs {
}
