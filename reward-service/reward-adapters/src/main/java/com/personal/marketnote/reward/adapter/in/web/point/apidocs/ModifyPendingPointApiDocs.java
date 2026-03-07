package com.personal.marketnote.reward.adapter.in.web.point.apidocs;

import com.personal.marketnote.reward.adapter.in.web.point.request.ModifyPendingPointRequest;
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
        summary = "(관리자) 회원 적립 예정 포인트 추가/차감",
        description = """
                작성일자: 2026-03-04
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 회원의 적립 예정 포인트를 추가하거나 차감합니다.
                
                - 포인트 차감 시에도 양수를 전송합니다.
                
                    - 예) 300 적립 예정 포인트를 추가하는 경우 -> changeType: "ACCRUAL", amount: 300 전송
                
                    - 예) 300 적립 예정 포인트를 차감하는 경우 -> changeType: "DEDUCTION", amount: 300 전송
                
                - 적립 예정 포인트는 0 미만이 될 수 없습니다.
                
                - 이력은 isReflected: false로 저장됩니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | userId (path) | number | 회원 ID | Y | 100 |
                | changeType | string | ACCRUAL: 추가, DEDUCTION: 차감 | Y | "ACCRUAL" |
                | amount | number | 변경 포인트(양수, 1 이상) | Y | 500 |
                | sourceType | string | 출처 유형 | Y | "ORDER" |
                | sourceId | number | 출처 ID | Y | 123 |
                | reason | string | 사유 | N | "주문 결제 적립 예정" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 시간 | "2026-03-04T12:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "적립 예정 포인트 수정 성공" |
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
                        schema = @Schema(implementation = ModifyPendingPointRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "changeType": "ACCRUAL",
                                  "amount": 500,
                                  "sourceType": "ORDER",
                                  "sourceId": 123,
                                  "reason": "주문 결제 적립 예정"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "적립 예정 포인트 수정 성공",
                        content = @Content(
                                schema = @Schema(implementation = RegisterUserPointResponseSchema.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-03-04T12:00:00.000",
                                          "content": {
                                            "userId": 100,
                                            "amount": 1000,
                                            "addExpectedAmount": 500,
                                            "expireExpectedAmount": 0,
                                            "createdAt": "2026-03-04T11:00:00",
                                            "modifiedAt": "2026-03-04T12:00:00"
                                          },
                                          "message": "적립 예정 포인트 수정 성공"
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
                                          "timestamp": "2026-03-04T12:00:00.000",
                                          "content": null,
                                          "message": "적립 예정 포인트가 부족합니다. 현재: 200, 요청: 500"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "회원 포인트 정보 없음",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 404,
                                          "code": "NOT_FOUND",
                                          "timestamp": "2026-03-04T12:00:00.000",
                                          "content": null,
                                          "message": "회원 포인트 정보를 찾을 수 없습니다. 전송된 회원 ID: 101"
                                        }
                                        """)
                        )
                )
        }
)
public @interface ModifyPendingPointApiDocs {
}
