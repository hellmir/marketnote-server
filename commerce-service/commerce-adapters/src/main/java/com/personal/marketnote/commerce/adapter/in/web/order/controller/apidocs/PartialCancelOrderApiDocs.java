package com.personal.marketnote.commerce.adapter.in.web.order.controller.apidocs;

import com.personal.marketnote.commerce.adapter.in.web.order.request.PartialCancelOrderRequest;
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
        summary = "주문 부분 취소 요청",
        description = """
                작성일자: 2026-10-11

                작성자: 성효빈

                ---

                ## Description

                - 구매자가 주문 내 일부 상품에 대한 부분 취소를 요청합니다.

                - 취소 대상 상품의 상태를 CANCELLED로, 주문 전체 상태를 PARTIALLY_CANCELLED로 변경합니다.

                - 모든 미취소 상품이 취소 대상에 포함되면 전체 취소로 전환됩니다.

                - 결제 대기, 결제 완료, 상품 준비중, 부분 취소 상태에서만 부분 취소 요청이 가능합니다.

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
                | cancelPricePolicyIds | number[] | 취소 대상 상품 가격 정책 ID 목록 | Y | [100, 200] |

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-10-11T12:00:00.000" |
                | content | object | 응답 본문 | null |
                | message | string | 처리 결과 | "주문 부분 취소 요청 성공" |
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
                        schema = @Schema(implementation = PartialCancelOrderRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "reasonCategory": "CANCEL_ORDER",
                                  "reason": "일부 상품 구매 의사 취소",
                                  "cancelPricePolicyIds": [100, 200]
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "주문 부분 취소 요청 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-10-11T12:00:00.000",
                                          "content": null,
                                          "message": "주문 부분 취소 요청 성공"
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
                                          "timestamp": "2026-10-11T12:00:00.000",
                                          "content": null,
                                          "message": "Invalid token"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "부분 취소 불가 (상태 전이 불가 또는 상품 검증 실패)",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 400,
                                          "code": "BAD_REQUEST",
                                          "timestamp": "2026-10-11T12:00:00.000",
                                          "content": null,
                                          "message": "현재 주문 상태에서 부분 취소가 불가능합니다. orderStatus=SHIPPING"
                                        }
                                        """)
                        )
                )
        })
public @interface PartialCancelOrderApiDocs {
}
