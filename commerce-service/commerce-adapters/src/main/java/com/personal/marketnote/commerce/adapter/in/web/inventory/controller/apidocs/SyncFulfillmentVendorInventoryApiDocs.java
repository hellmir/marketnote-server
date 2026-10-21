package com.personal.marketnote.commerce.adapter.in.web.inventory.controller.apidocs;

import com.personal.marketnote.commerce.adapter.in.web.inventory.request.SyncFulfillmentVendorInventoryRequest;
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
        summary = "(관리자) 풀필먼트 벤더 재고 동기화",
        description = """
                작성일자: 2026-02-07
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                풀필먼트 벤더 재고 정보를 동기화합니다.
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | inventories | array | 풀필먼트 벤더 재고 목록 | Y | [ ... ] |
                | inventories.productKey | string(uuid) | 상품 키 (UUID) | Y | "3fa85f64-5717-4562-b3fc-2c963f66afa6" |
                | inventories.stock | number | 재고 수량 | Y | 100 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-02-07T12:12:30.013" |
                | content | object | 응답 본문 | null |
                | message | string | 처리 결과 | "풀필먼트 벤더 재고 동기화 성공" |
                
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = SyncFulfillmentVendorInventoryRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "inventories": [
                                    {
                                      "productKey": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                      "stock": 100
                                    },
                                    {
                                      "productKey": "5c82e87f-f3d6-4e63-9d24-7a5b8c9a1d23",
                                      "stock": 0
                                    }
                                  ]
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "풀필먼트 벤더 재고 동기화 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-07T12:12:30.013",
                                          "content": null,
                                          "message": "풀필먼트 벤더 재고 동기화 성공"
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
                                          "timestamp": "2026-02-07T12:12:30.013",
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
                                          "timestamp": "2026-02-07T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        })
public @interface SyncFulfillmentVendorInventoryApiDocs {
}
