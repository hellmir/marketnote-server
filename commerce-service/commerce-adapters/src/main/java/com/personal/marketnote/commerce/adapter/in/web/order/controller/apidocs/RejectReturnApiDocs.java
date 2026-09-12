package com.personal.marketnote.commerce.adapter.in.web.order.controller.apidocs;

import com.personal.marketnote.commerce.adapter.in.web.order.request.RejectReturnRequest;
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
        summary = "반품 불가 판정 (관리자/판매자 전용)",
        description = """
                작성일자: 2026-09-03

                작성자: 성효빈

                ---

                ## Description

                - 관리자/판매자가 반품 요청된 주문을 반품 불가로 판정합니다.

                - 주문 상태를 RETURN_REJECTED(반품 불가)로 변경합니다.

                - 반품 요청(RETURN_REQUESTED) 상태에서만 반품 불가 판정이 가능합니다.

                - 반품 불가 판정 시 ReturnRejectedEvent를 Kafka로 발행합니다.

                - 반품 불가 사유 카테고리 목록

                    - "SIMPLE_CHANGE_OF_MIND": 단순 변심

                    - "PRODUCT_DAMAGE": 상품 파손/변질

                    - "PRODUCT_MISMATCH": 상품이 설명과 다름

                    - "WRONG_DELIVERY": 다른 상품이 배송됨

                    - "MISSING_COMPONENTS": 상품/구성품 누락

                    - "MISTAKE": 주문 실수

                    - "ETC": 직접 입력

                ---

                ## Request

                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | reasonCategory | string | 반품 불가 사유 카테고리 | N | "ETC" |
                | reason | string | 반품 불가 사유 | N | "검수 결과 상품 하자 없음" |

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-09-03T12:00:00.000" |
                | content | object | 응답 본문 | null |
                | message | string | 처리 결과 | "반품 불가 판정 성공" |
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
                        schema = @Schema(implementation = RejectReturnRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "reasonCategory": "ETC",
                                  "reason": "검수 결과 상품 하자 없음"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "반품 불가 판정 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-09-03T12:00:00.000",
                                          "content": null,
                                          "message": "반품 불가 판정 성공"
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
                                          "timestamp": "2026-09-03T12:00:00.000",
                                          "content": null,
                                          "message": "Invalid token"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "403",
                        description = "토큰 인가 실패 (관리자/판매자 권한 필요)",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 403,
                                          "code": "FORBIDDEN",
                                          "timestamp": "2026-09-03T12:00:00.000",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "이미 반품 불가 판정됨",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 409,
                                          "code": "CONFLICT",
                                          "timestamp": "2026-09-03T12:00:00.000",
                                          "content": null,
                                          "message": "이미 해당 주문 상태(반품 불가)로 변경되었습니다."
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
                                          "timestamp": "2026-09-03T12:00:00.000",
                                          "content": null,
                                          "message": "주문 상태를 배송 완료에서 반품 불가(으)로 변경할 수 없습니다."
                                        }
                                        """)
                        )
                ),
        })
public @interface RejectReturnApiDocs {
}
