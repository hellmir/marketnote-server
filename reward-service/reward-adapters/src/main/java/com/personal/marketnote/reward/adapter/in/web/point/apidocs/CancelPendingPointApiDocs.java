package com.personal.marketnote.reward.adapter.in.web.point.apidocs;

import com.personal.marketnote.reward.adapter.in.web.point.request.CancelPendingPointRequest;
import com.personal.marketnote.reward.adapter.in.web.point.response.RegisterUserPointResponseSchema;
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
        summary = "(관리자) 적립 예정 포인트 취소",
        description = """
                작성일자: 2026-03-07

                작성자: 성효빈

                ---

                ## Description

                - 적립 예정 포인트를 취소(회수)합니다.

                - sourceType과 sourceId로 취소 대상 적립 예정 포인트 이력을 식별합니다.

                - 취소 시 적립 예정 포인트에서 차감되고, 실제 포인트는 변경되지 않습니다.

                - 기존 pending 이력은 isReflected: true로 업데이트됩니다.

                - 취소 대상 이력이 없으면 현재 포인트를 그대로 반환합니다 (멱등성 보장).

                ---

                ## Request

                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | userId (path) | number | 회원 ID | Y | 100 |
                | sourceType | string | 출처 유형 | Y | "ORDER" |
                | sourceId | number | 출처 ID (주문 ID) | Y | 123 |
                | reason | string | 사유 | N | "결제 취소 적립 예정 포인트 회수" |

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 시간 | "2026-03-07T12:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "적립 예정 포인트 취소 성공" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "userId",
                        in = ParameterIn.PATH,
                        required = true,
                        description = "회원 ID",
                        schema = @Schema(type = "number", example = "100")
                )
        },
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = CancelPendingPointRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "sourceType": "ORDER",
                                  "sourceId": 123,
                                  "reason": "결제 취소 적립 예정 포인트 회수"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "적립 예정 포인트 취소 성공",
                        content = @Content(
                                schema = @Schema(implementation = RegisterUserPointResponseSchema.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-03-07T12:00:00.000",
                                          "content": {
                                            "userId": 100,
                                            "amount": 1000,
                                            "addExpectedAmount": 0,
                                            "expireExpectedAmount": 0,
                                            "createdAt": "2026-03-07T11:00:00",
                                            "modifiedAt": "2026-03-07T12:00:00"
                                          },
                                          "message": "적립 예정 포인트 취소 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "적립 예정 포인트 부족",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 400,
                                          "code": "BAD_REQUEST",
                                          "timestamp": "2026-03-07T12:00:00.000",
                                          "content": null,
                                          "message": "적립 예정 포인트가 부족합니다. 현재: 200, 요청: 500"
                                        }
                                        """)
                        )
                )
        }
)
public @interface CancelPendingPointApiDocs {
}
