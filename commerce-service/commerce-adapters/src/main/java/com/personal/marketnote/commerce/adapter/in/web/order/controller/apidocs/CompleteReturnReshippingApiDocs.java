package com.personal.marketnote.commerce.adapter.in.web.order.controller.apidocs;

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
        summary = "CS 회송 완료 처리",
        description = """
                작성일자: 2026-06-03

                작성자: 성효빈

                ---

                ## Description

                - CS 담당자가 회송 중(RETURN_RESHIPPING) 상태의 주문에 대해 회송 완료 처리합니다.

                - 주문 상태를 회송 완료(RETURN_RESHIPPED)로 전이합니다.

                - 이미 회송 완료(RETURN_RESHIPPED) 상태인 주문에 대해 호출하면 멱등하게 처리됩니다.

                - RETURN_RESHIPPING 상태가 아닌 주문에 대해 호출하면 400 에러가 반환됩니다.

                ---

                ## Request

                - Request Body 없음 (Path Variable만 사용)

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 상태 전이 불가 / 401: 인증 실패 / 403: 권한 없음 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" |
                | timestamp | string(datetime) | 응답 일시 | "2026-06-03T12:00:00.000" |
                | content | object | 응답 본문 | null |
                | message | string | 처리 결과 | "회송 완료 처리 성공" |
                """, security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "id",
                        description = "주문 ID",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "number")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "회송 완료 처리 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-06-03T12:00:00.000",
                                          "content": null,
                                          "message": "회송 완료 처리 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "상태 전이 불가",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 400,
                                          "code": "BAD_REQUEST",
                                          "timestamp": "2026-06-03T12:00:00.000",
                                          "content": null,
                                          "message": "주문 상태를 결제 완료에서 회송 완료(으)로 변경할 수 없습니다."
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
                                          "timestamp": "2026-06-03T12:00:00.000",
                                          "content": null,
                                          "message": "Invalid token"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "403",
                        description = "관리자 권한 필요",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 403,
                                          "code": "FORBIDDEN",
                                          "timestamp": "2026-06-03T12:00:00.000",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                ),
        })
public @interface CompleteReturnReshippingApiDocs {
}
