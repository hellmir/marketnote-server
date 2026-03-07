package com.personal.marketnote.reward.adapter.in.web.point.apidocs;

import com.personal.marketnote.reward.adapter.in.web.point.request.ConfirmPendingPointRequest;
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
        summary = "(관리자) 적립 예정 포인트 확정",
        description = """
                작성일자: 2026-03-07
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 적립 예정 포인트를 실제 포인트로 확정(전환)합니다.
                
                - sourceType과 sourceId로 확정 대상 적립 예정 포인트 이력을 식별합니다.
                
                - 확정 시 적립 예정 포인트에서 차감되고, 동일 금액이 실제 포인트에 적립됩니다.
                
                - 기존 pending 이력은 isReflected: true로 업데이트되고, 확정 이력이 새로 생성됩니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | userId (path) | number | 회원 ID | Y | 100 |
                | sourceType | string | 출처 유형 | Y | "ORDER" |
                | sourceId | number | 출처 ID (주문 ID) | Y | 123 |
                | reason | string | 사유 | N | "구매 확정 포인트 적립" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 시간 | "2026-03-07T12:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "적립 예정 포인트 확정 성공" |
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
                        schema = @Schema(implementation = ConfirmPendingPointRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "sourceType": "ORDER",
                                  "sourceId": 123,
                                  "reason": "구매 확정 포인트 적립"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "적립 예정 포인트 확정 성공",
                        content = @Content(
                                schema = @Schema(implementation = RegisterUserPointResponseSchema.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-03-07T12:00:00.000",
                                          "content": {
                                            "userId": 100,
                                            "amount": 1500,
                                            "addExpectedAmount": 0,
                                            "expireExpectedAmount": 0,
                                            "createdAt": "2026-03-07T11:00:00",
                                            "modifiedAt": "2026-03-07T12:00:00"
                                          },
                                          "message": "적립 예정 포인트 확정 성공"
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
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "확정 대상 적립 예정 포인트 이력 없음",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 404,
                                          "code": "NOT_FOUND",
                                          "timestamp": "2026-03-07T12:00:00.000",
                                          "content": null,
                                          "message": "확정 대상 적립 예정 포인트 이력을 찾을 수 없습니다. userId=100, sourceType=ORDER, sourceId=123"
                                        }
                                        """)
                        )
                )
        }
)
public @interface ConfirmPendingPointApiDocs {
}
