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
        summary = "배송지 삭제",
        description = """
                작성일자: 2026-02-19
                
                작성자: 성효빈
                
                ---
                
                ## Description
                - 배송지를 삭제합니다. (Soft Delete)
                
                - **집(HOME) 배송지는 삭제할 수 없습니다.**
                
                - **기본 배송지는 삭제할 수 없습니다.** 다른 배송지를 기본으로 설정한 후 삭제해야 합니다.
                
                - 회사(COMPANY), 기타(OTHER) 배송지만 삭제 가능합니다.
                
                - 본인의 배송지만 삭제할 수 있습니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | id | number(path) | 배송지 ID | O | 1 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 삭제 불가 / 404: 배송지 없음 |
                | code | string | 응답 코드 | "SUC01" / "ERR01" / "ERR02" |
                | timestamp | string(datetime) | 응답 일시 | "2026-02-19T12:00:00.000" |
                | content | null | 응답 본문 | null |
                | message | string | 처리 결과 | "배송지 삭제 성공" |
                """,
        parameters = {
                @Parameter(name = "id", description = "배송지 ID", required = true, example = "1")
        },
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "배송지 삭제 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-19T12:00:00.000000",
                                          "content": null,
                                          "message": "배송지 삭제 성공"
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
                                          "code": "ERR01",
                                          "timestamp": "2026-02-19T12:00:00.000000",
                                          "content": null,
                                          "message": "ERR01:: 배송지를 찾을 수 없습니다. 전송된 배송지 ID: 999"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "삭제 불가 (집 배송지 또는 기본 배송지)",
                        content = @Content(
                                examples = {
                                        @ExampleObject(
                                                name = "집 배송지 삭제 불가",
                                                value = """
                                                        {
                                                          "statusCode": 400,
                                                          "code": "ERR02",
                                                          "timestamp": "2026-02-19T12:00:00.000000",
                                                          "content": null,
                                                          "message": "ERR02:: 집 배송지는 삭제할 수 없습니다."
                                                        }
                                                        """
                                        ),
                                        @ExampleObject(
                                                name = "기본 배송지 삭제 불가",
                                                value = """
                                                        {
                                                          "statusCode": 400,
                                                          "code": "ERR02",
                                                          "timestamp": "2026-02-19T12:00:00.000000",
                                                          "content": null,
                                                          "message": "ERR02:: 기본 배송지는 삭제할 수 없습니다. 다른 배송지를 기본으로 설정한 후 삭제해주세요."
                                                        }
                                                        """
                                        )
                                }
                        )
                )
        }
)
public @interface DeleteShippingAddressApiDocs {
}
