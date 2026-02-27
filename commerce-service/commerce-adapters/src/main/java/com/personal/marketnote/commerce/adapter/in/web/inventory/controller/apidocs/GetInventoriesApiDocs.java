package com.personal.marketnote.commerce.adapter.in.web.inventory.controller.apidocs;

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
        summary = "상품 재고 목록 조회",
        description = """
                작성일자: 2026-01-07
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                상품 재고 목록을 조회합니다. productIds를 함께 전달하면 존재하지 않는 재고를 자동 생성합니다.
                productIds를 전달하지 않으면 존재하는 재고만 조회합니다.
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | pricePolicyIds | array<number> | 가격 정책 ID 목록 | Y | [1, 2, 3] |
                | productIds | array<number> | 상품 ID 목록 (pricePolicyIds와 동일 순서로 매핑) | N | [100, 200, 300] |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 201: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-07T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "상품 재고 목록 조회 성공" |
                
                ---
                
                ### Response > content
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | inventories | array | 상품 재고 목록 | [ ... ] |
                
                ---
                
                ### Response > content > inventories
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | pricePolicyId | number | 가격 정책 ID | 188 |
                | stock | number | 재고 | 980 |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "pricePolicyIds",
                        description = "가격 정책 ID 목록",
                        in = ParameterIn.QUERY,
                        required = true,
                        schema = @Schema(type = "array", example = "[1, 2, 3]")
                ),
                @Parameter(
                        name = "productIds",
                        description = "상품 ID 목록 (pricePolicyIds와 동일 순서로 매핑, 전달 시 존재하지 않는 재고 자동 생성)",
                        in = ParameterIn.QUERY,
                        required = false,
                        schema = @Schema(type = "array", example = "[100, 200, 300]")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "상품 재고 목록 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-24T13:42:54.309143665",
                                          "content": {
                                            "inventories": [
                                              {
                                                "pricePolicyId": 188,
                                                "stock": 980
                                              },
                                              {
                                                "pricePolicyId": 184,
                                                "stock": 0
                                              },
                                              {
                                                "pricePolicyId": 200,
                                                "stock": 0
                                              }
                                            ]
                                          },
                                          "message": "상품 재고 목록 조회 성공"
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
                                          "timestamp": "2026-01-07T12:12:30.013",
                                          "content": null,
                                          "message": "Invalid token"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "403",
                        description = "토큰 인가 실패",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 403,
                                          "code": "FORBIDDEN",
                                          "timestamp": "2026-01-07T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        })
public @interface GetInventoriesApiDocs {
}

