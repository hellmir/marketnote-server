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
        summary = "구매 확정",
        description = """
                작성일자: 2026-04-05

                작성자: 성효빈

                ---

                ## Description

                - 구매자가 구매 확정을 요청합니다.

                - 주문 상태를 CONFIRMED(구매 확정)로 변경합니다.

                - 배송 완료, 부분 구매 확정 상태에서만 구매 확정이 가능합니다.

                - 구매 확정 시 구매 확정 이벤트가 발행됩니다.

                ---

                ## Request

                - Request Body 없음 (Path Variable만 사용)

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 401: 인증 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "UNAUTHORIZED" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-04-05T12:00:00.000" |
                | content | object | 응답 본문 | null |
                | message | string | 처리 결과 | "구매 확정 성공" |
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
                        description = "구매 확정 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-05T12:00:00.000",
                                          "content": null,
                                          "message": "구매 확정 성공"
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
                                          "timestamp": "2026-04-05T12:00:00.000",
                                          "content": null,
                                          "message": "Invalid token"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "이미 구매 확정됨",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 409,
                                          "code": "CONFLICT",
                                          "timestamp": "2026-04-05T12:00:00.000",
                                          "content": null,
                                          "message": "이미 해당 주문 상태(구매 확정)로 변경되었습니다."
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
                                          "timestamp": "2026-04-05T12:00:00.000",
                                          "content": null,
                                          "message": "주문 상태를 배송중에서 구매 확정(으)로 변경할 수 없습니다."
                                        }
                                        """)
                        )
                ),
        })
public @interface ConfirmOrderApiDocs {
}
