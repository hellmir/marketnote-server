package com.personal.marketnote.product.adapter.in.web.pricepolicy.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
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
        summary = "(비회원) 상품 가격 정책 목록 조회",
        description = """
                작성일자: 2026-01-02
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                상품의 가격 정책 목록(기본/조합)을 조회합니다.
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-02T10:37:32.320824" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "상품 가격 정책 목록 조회 성공" |
                
                ### Response > content
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | policies | array | 가격 정책 목록 | [ ... ] |
                
                ### Response > content > policies
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 가격 정책 ID | 12 |
                | price | number | 정가 | 10000 |
                | discountPrice | number | 할인 판매가 | 8000 |
                | accumulatedPoint | number | 적립 포인트 | 100 |
                | discountRate | number | 할인율 | 20 |
                | basePolicy | boolean | 기본 정책 여부(조합 미연결) | true |
                | optionIds | array<number> | 연결된 옵션 ID 목록(조합 정책일 때) | [3,7] |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "OK",
                        content = @Content(
                                schema = @Schema(implementation = com.personal.marketnote.common.adapter.in.api.schema.StringResponseSchema.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-02T10:37:32.320824",
                                          "content": {
                                            "policies": [
                                              {
                                                "id": 115,
                                                "price": 100000,
                                                "discountPrice": 50000,
                                                "accumulatedPoint": 30000,
                                                "discountRate": 60,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  34,
                                                  40
                                                ]
                                              },
                                              {
                                                "id": 114,
                                                "price": 100000,
                                                "discountPrice": 50000,
                                                "accumulatedPoint": 30000,
                                                "discountRate": 60,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  34,
                                                  40
                                                ]
                                              },
                                              {
                                                "id": 112,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  34,
                                                  39
                                                ]
                                              },
                                              {
                                                "id": 111,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  34,
                                                  38
                                                ]
                                              },
                                              {
                                                "id": 110,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  33,
                                                  40
                                                ]
                                              },
                                              {
                                                "id": 109,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  33,
                                                  39
                                                ]
                                              },
                                              {
                                                "id": 108,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  33,
                                                  38
                                                ]
                                              },
                                              {
                                                "id": 107,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  32,
                                                  40
                                                ]
                                              },
                                              {
                                                "id": 106,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  32,
                                                  39
                                                ]
                                              },
                                              {
                                                "id": 105,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  32,
                                                  38
                                                ]
                                              },
                                              {
                                                "id": 104,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  40
                                                ]
                                              },
                                              {
                                                "id": 103,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  39
                                                ]
                                              },
                                              {
                                                "id": 102,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  38
                                                ]
                                              },
                                              {
                                                "id": 101,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  34
                                                ]
                                              },
                                              {
                                                "id": 100,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  34
                                                ]
                                              },
                                              {
                                                "id": 99,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  34
                                                ]
                                              },
                                              {
                                                "id": 98,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  33
                                                ]
                                              },
                                              {
                                                "id": 97,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  33
                                                ]
                                              },
                                              {
                                                "id": 96,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  33
                                                ]
                                              },
                                              {
                                                "id": 95,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  32
                                                ]
                                              },
                                              {
                                                "id": 94,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  32
                                                ]
                                              },
                                              {
                                                "id": 93,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  32
                                                ]
                                              },
                                              {
                                                "id": 92,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": []
                                              },
                                              {
                                                "id": 91,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": []
                                              },
                                              {
                                                "id": 90,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": []
                                              },
                                              {
                                                "id": 89,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  34
                                                ]
                                              },
                                              {
                                                "id": 88,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  33
                                                ]
                                              },
                                              {
                                                "id": 87,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": [
                                                  32
                                                ]
                                              },
                                              {
                                                "id": 86,
                                                "price": 10000,
                                                "discountPrice": 9000,
                                                "accumulatedPoint": 1000,
                                                "discountRate": 11.1,
                                                "basePolicy": false,
                                                "optionIds": []
                                              }
                                            ]
                                          },
                                          "message": "상품 가격 정책 목록 조회 성공"
                                        }
                                        """)
                        )
                )
        })
public @interface GetPricePoliciesApiDocs {
}


