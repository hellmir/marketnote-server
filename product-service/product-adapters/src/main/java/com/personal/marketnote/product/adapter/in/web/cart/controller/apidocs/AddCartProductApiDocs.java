package com.personal.marketnote.product.adapter.in.web.cart.controller.apidocs;

import com.personal.marketnote.product.adapter.in.web.cart.request.AddCartProductRequest;
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
        summary = "장바구니 상품 추가",
        description = """
                작성일자: 2026-01-04
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 회원 장바구니에 상품을 추가합니다.
                
                - 회원이 선택한 옵션 ID 조합에 맞는 가격 정책 ID를 전송해야 합니다.
                
                - 옵션 ID 조합에 맞는 가격 정책 ID가 없는 경우, 기본 가격 정책 ID를 전송합니다.
                
                - 링크 공유를 통해 구매하는 경우, 링크를 공유한 회원 식별키를 전송합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | pricePolicyId | number | 가격 정책 ID | Y | 1 |
                | sharerKey | string(uuid) | 링크 공유 회원 식별키 | N | "550e8400-e29b-41d4-a716-446655440000" |
                | imageUrl | string | 상품 이미지 URL | Y | "https://marketnote.s3.amazonaws.com/product/30/1763534195922_image_600.png" |
                | quantity | number | 상품 수량 | Y | 1 |
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 201: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-04T12:12:30.013" |
                | content | object | 응답 본문 | null |
                | message | string | 처리 결과 | "장바구니 상품 추가 성공" |
                
                ---
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = AddCartProductRequest.class),
                        examples = @ExampleObject("""
                                {
                                    "pricePolicyId": 1,
                                    "sharerKey": "550e8400-e29b-41d4-a716-446655440000",
                                    "imageUrl": "https://marketnote.s3.amazonaws.com/product/30/1763534195922_image_600.png",
                                    "quantity": 1
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "장바구니 상품 추가 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 201,
                                          "code": "SUC01",
                                          "timestamp": "2025-12-30T12:12:30.013",
                                          "content": null,
                                          "message": "장바구니 상품 추가 성공"
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
public @interface AddCartProductApiDocs {
}
