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
        summary = "(관리자) 파스토 비정상 입고 상품 이미지 조회",
        description = """
                작성일자: 2026-02-17
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                파스토 비정상 입고 상품의 이미지 정보를 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **위치** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- | --- |
                | accessToken | header | string | 파스토 액세스 토큰 | Y | your-access-token |
                | slipNo | path | string | 입고요청번호 | Y | TESTIO260119000005 |
                | godCd | path | string | 상품코드 | Y | 943881 |
                | goodsSerialNo | path | string | 상품일련번호 | Y | SERIAL-0001 |
                | fileSeq | path | string | 파일seq | Y | 1 |
                | imgNo | path | string | 이미지순서 | Y | 1 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-02-17T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "파스토 비정상 입고 상품 이미지 조회 성공" |
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
                        name = "slipNo",
                        description = "입고요청번호",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "TESTIO260119000005")
                ),
                @Parameter(
                        name = "godCd",
                        description = "상품코드",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "943881")
                ),
                @Parameter(
                        name = "goodsSerialNo",
                        description = "상품일련번호",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "SERIAL-0001")
                ),
                @Parameter(
                        name = "fileSeq",
                        description = "파일seq",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "1")
                ),
                @Parameter(
                        name = "imgNo",
                        description = "이미지순서",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "string", example = "1")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "파스토 비정상 입고 상품 이미지 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-17T12:12:30.013",
                                          "content": {
                                            "dataCount": 1,
                                            "data": {}
                                          },
                                          "message": "파스토 비정상 입고 상품 이미지 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetFulfillmentWarehousingAbnormalImageApiDocs {
}
