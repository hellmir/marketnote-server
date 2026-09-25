package com.personal.marketnote.product.adapter.in.web.product.controller.apidocs;

import com.personal.marketnote.product.adapter.in.web.product.request.UpdateProductRequest;
import io.swagger.v3.oas.annotations.Operation;
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
        summary = "(판매자/관리자) 상품 정보 수정",
        description = """
                작성일자: 2026-02-03
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 상품 정보를 수정합니다.
                
                - 판매자 또는 관리자만 가능합니다.
                
                - 파스토 상품 정보 수정 파라미터가 전달되면 파스토 상품 정보도 함께 수정합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | name | string | 상품명 | Y | "스프링노트1" |
                | brandName | string | 브랜드명 | Y | "노트왕" |
                | detail | string | 상품 설명 | Y | "스프링노트1 설명" |
                | isFindAllOptions | boolean | 상품 목록 조회 시 옵션마다 개별 상품으로 조회 여부 | Y | true |
                | tags | array<string> | 상품 태그 목록(없는 경우 빈 배열) | Y | ["루테인", "아스타잔틴"] |
                | fulfillmentVendorGoods | object | 파스토 상품 수정 파라미터(요청 시 godType/giftDiv 필수) | N | { ... } |
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-01T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "상품 정보 수정 성공" |

                ---

                ### Response > content

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 상품 ID | 1 |
                | productKey | string(uuid) | 상품 productKey | "01890d0a-1234-7000-89ab-0123456789ab" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = UpdateProductRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "name": "스프링노트1",
                                  "brandName": "노트왕",
                                  "detail": "스프링노트1 설명",
                                  "isFindAllOptions": true,
                                  "tags": ["루테인", "아스타잔틴"],
                                  "fulfillmentVendorGoods": {
                                    "godType": "1",
                                    "giftDiv": "01",
                                    "supCd": "94388001",
                                    "cateCd": "A001",
                                    "seasonCd": "0",
                                    "genderCd": "A"
                                  }
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "상품 정보 수정 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-01T12:12:30.013",
                                          "content": {
                                            "id": 1,
                                            "productKey": "01890d0a-1234-7000-89ab-0123456789ab"
                                          },
                                          "message": "상품 정보 수정 성공"
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
public @interface UpdateProductApiDocs {
}
