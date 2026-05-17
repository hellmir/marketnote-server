package com.personal.marketnote.product.adapter.in.web.option.controller.apidocs;

import com.personal.marketnote.common.adapter.in.api.schema.StringResponseSchema;
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
        summary = "상품 옵션 카테고리 및 하위 옵션 목록 조회",
        description = """
                작성일자: 2026-01-01
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                특정 상품의 옵션 카테고리 및 하위 옵션 목록을 조회합니다.
                
                ---
                
                ## Path Variables
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | productId | number | 상품 ID | Y | 1001 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-01T10:37:32.320824" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "상품 옵션 카테고리 및 하위 옵션 목록 조회 성공"" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | categories | array | 옵션 카테고리 목록 | [ ... ] |
                
                ---
                
                ### Response > content > categories
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 옵션 카테고리 ID | 10 |
                | name | string | 옵션 카테고리명 | "수량" |
                | orderNum | number | 정렬 순서 | 1 |
                | status | string | 상태 | "ACTIVE" |
                | options | array | 하위 옵션 목록 | [ ... ] |
                
                ---
                
                ### Response > content > categories > options
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 옵션 ID | 101 |
                | content | string | 옵션 내용 | "1박스" |
                | price | number | 옵션 가격(원) | 37000 |
                | accumulatedPoint | number | 적립 포인트 | 1200 |
                | status | string | 상태 | "ACTIVE" |
                """, security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "productId",
                        in = ParameterIn.PATH,
                        description = "상품 ID",
                        schema = @Schema(type = "number")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "조회 성공",
                        content = @Content(
                                schema = @Schema(implementation = StringResponseSchema.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-01T14:08:35.975356",
                                          "content": {
                                            "categories": [
                                              {
                                                "id": 3,
                                                "name": "수량",
                                                "orderNum": 3,
                                                "status": "ACTIVE",
                                                "options": [
                                                  {
                                                    "id": 4,
                                                    "content": "1박스",
                                                    "price": 37000,
                                                    "accumulatedPoint": 1200,
                                                    "status": "ACTIVE"
                                                  },
                                                  {
                                                    "id": 5,
                                                    "content": "3박스",
                                                    "price": 99000,
                                                    "accumulatedPoint": 1200,
                                                    "status": "ACTIVE"
                                                  }
                                                ]
                                              },
                                              {
                                                "id": 4,
                                                "name": "등급",
                                                "orderNum": 4,
                                                "status": "ACTIVE",
                                                "options": [
                                                  {
                                                    "id": 6,
                                                    "content": "최상",
                                                    "price": 37000,
                                                    "accumulatedPoint": 1200,
                                                    "status": "ACTIVE"
                                                  },
                                                  {
                                                    "id": 7,
                                                    "content": "상",
                                                    "price": 22000,
                                                    "accumulatedPoint": 800,
                                                    "status": "ACTIVE"
                                                  },
                                                  {
                                                    "id": 8,
                                                    "content": "중",
                                                    "price": 11000,
                                                    "accumulatedPoint": 400,
                                                    "status": "ACTIVE"
                                                  }
                                                ]
                                              },
                                              {
                                                "id": 5,
                                                "name": "등급2",
                                                "orderNum": 5,
                                                "status": "ACTIVE",
                                                "options": [
                                                  {
                                                    "id": 9,
                                                    "content": "최상",
                                                    "price": 37000,
                                                    "accumulatedPoint": 1200,
                                                    "status": "ACTIVE"
                                                  },
                                                  {
                                                    "id": 10,
                                                    "content": "상",
                                                    "price": 22000,
                                                    "accumulatedPoint": 800,
                                                    "status": "ACTIVE"
                                                  },
                                                  {
                                                    "id": 11,
                                                    "content": "중",
                                                    "price": 11000,
                                                    "accumulatedPoint": 400,
                                                    "status": "ACTIVE"
                                                  }
                                                ]
                                              }
                                            ]
                                          },
                                          "message": "상품 옵션 카테고리 및 옵션 목록 조회 성공"
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
                                          "timestamp": "2025-12-30T12:12:30.013",
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
                                          "timestamp": "2025-12-30T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        })
public @interface GetProductOptionsApiDocs {
}


