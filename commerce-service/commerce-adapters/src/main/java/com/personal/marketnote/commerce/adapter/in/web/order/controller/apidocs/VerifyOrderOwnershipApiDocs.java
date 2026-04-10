package com.personal.marketnote.commerce.adapter.in.web.order.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "주문 소유권 확인 (서비스 간 통신용)",
        description = """
                작성일자: 2026-04-05
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 주문 ID와 구매자 ID로 주문 소유권을 확인합니다.
                - HMAC 인증 기반 서비스 간 통신 전용 API입니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | orderId | number | 주문 ID | Y | 1 |
                | buyerId | number | 구매자 ID | Y | 100 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 소유권 확인 성공 / 403: 소유권 불일치 / 404: 주문 미존재 |
                | code | string | 응답 코드 | "SUC01" / "FORBIDDEN" / "NOT_FOUND" |
                | timestamp | string(datetime) | 응답 일시 | "2026-04-05T12:00:00.000" |
                | content | null | 응답 본문 | null |
                | message | string | 처리 결과 | "주문 소유권 확인 성공" |
                """,
        parameters = {
                @Parameter(
                        name = "orderId",
                        description = "주문 ID",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "number")
                ),
                @Parameter(
                        name = "buyerId",
                        description = "구매자 ID",
                        in = ParameterIn.QUERY,
                        required = true,
                        schema = @Schema(type = "number")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "주문 소유권 확인 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-05T12:00:00.000",
                                          "content": null,
                                          "message": "주문 소유권 확인 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "403",
                        description = "주문 소유자 불일치",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 403,
                                          "code": "FORBIDDEN",
                                          "timestamp": "2026-04-05T12:00:00.000",
                                          "content": null,
                                          "message": "해당 주문에 대한 접근 권한이 없습니다."
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "주문 미존재",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 404,
                                          "code": "NOT_FOUND",
                                          "timestamp": "2026-04-05T12:00:00.000",
                                          "content": null,
                                          "message": "주문을 찾을 수 없습니다."
                                        }
                                        """)
                        )
                )
        })
public @interface VerifyOrderOwnershipApiDocs {
}
