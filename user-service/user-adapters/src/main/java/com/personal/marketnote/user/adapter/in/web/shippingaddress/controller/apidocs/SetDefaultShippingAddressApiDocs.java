package com.personal.marketnote.user.adapter.in.web.shippingaddress.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "기본 배송지 설정",
        description = """
                작성일자: 2026-02-19
                
                작성자: 성효빈
                
                ---
                
                ## Description
                - 지정한 배송지를 기본 배송지로 설정합니다.
                
                - 기존 기본 배송지는 자동으로 해제됩니다.
                
                - 이미 기본 배송지인 경우 변경 없이 성공 응답을 반환합니다.
                
                - 본인의 배송지만 설정할 수 있습니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | id | number(path) | 배송지 ID | O | 1 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-02-19T12:00:00.000" |
                | content | null | 응답 본문 | null |
                | message | string | 처리 결과 | "기본 배송지 설정 성공" |
                """,
        parameters = {
                @Parameter(name = "id", description = "배송지 ID", required = true, example = "1")
        },
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "기본 배송지 설정 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-19T12:00:00.000000",
                                          "content": null,
                                          "message": "기본 배송지 설정 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "배송지를 찾을 수 없음",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 404,
                                          "code": "NOT_FOUND",
                                          "timestamp": "2026-02-19T12:00:00.000000",
                                          "message": "배송지를 찾을 수 없습니다. 전송된 배송지 ID: 999"
                                        }
                                        """)
                        )
                )
        }
)
public @interface SetDefaultShippingAddressApiDocs {
}
