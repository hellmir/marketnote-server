package com.personal.marketnote.commerce.adapter.in.web.order.controller.apidocs;

import com.personal.marketnote.commerce.adapter.in.web.order.request.CancelOrderRequest;
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
        summary = "주문 취소 요청",
        description = """
                작성일자: 2026-04-05
                
                작성자: 성효빈
                
                ---
                
                ## Description

                - 구매자가 주문 취소를 요청합니다.

                - pricePolicyIds를 지정하지 않거나 빈 배열이면 주문 전체를 취소합니다.

                - pricePolicyIds가 있으면 지정한 상품만 부분 취소합니다. 남은 미취소 상품을 모두 포함하면 전체 취소로 처리합니다.

                - 주문 전체 취소 시 상태를 CANCELLED(주문 취소) 또는 CANCEL_REQUESTED(취소 요청)로 변경합니다.

                - 부분 취소 시 일부 상품만 취소되며 주문 전체 상태는 PARTIALLY_CANCEL_REQUESTED(부분 취소 요청) 또는 PARTIALLY_CANCELLED(부분 취소)로 변경됩니다.

                - 결제 대기, 결제 완료, 상품 준비중 상태에서만 취소 요청이 가능합니다. 부분 취소는 PARTIALLY_CANCELLED 상태에서도 가능합니다.

                - 취소 사유 카테고리 목록

                    - "CANCEL_ORDER": 구매 의사 취소

                    - "CHANGE_OPTION": 색상, 사이즈 등 변경

                    - "MISTAKE": 주문 실수

                    - "ETC": 직접 입력

                ---

                ## Request

                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | reasonCategory | string | 취소 사유 카테고리 | N | "CANCEL_ORDER" |
                | reason | string | 취소 사유 | N | "단순 변심" |
                | pricePolicyIds | array(number) | 취소 대상 상품의 가격 정책 ID 목록 (미지정/빈 배열이면 전체 취소) | N | [100, 200] |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-04-05T12:00:00.000" |
                | content | object | 응답 본문 | null |
                | message | string | 처리 결과 | "주문 취소 요청 성공" |
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
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = CancelOrderRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "reasonCategory": "CANCEL_ORDER",
                                  "reason": "단순 변심",
                                  "pricePolicyIds": []
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "주문 취소 요청 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-05T12:00:00.000",
                                          "content": null,
                                          "message": "주문 취소 요청 성공"
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
                        description = "이미 주문 취소",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 409,
                                          "code": "CONFLICT",
                                          "timestamp": "2026-04-05T12:00:00.000",
                                          "content": null,
                                          "message": "이미 해당 주문 상태(주문 취소)로 변경되었습니다."
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
                                          "message": "주문 상태를 배송중에서 주문 취소(으)로 변경할 수 없습니다."
                                        }
                                        """)
                        )
                ),
        })
public @interface CancelOrderApiDocs {
}
