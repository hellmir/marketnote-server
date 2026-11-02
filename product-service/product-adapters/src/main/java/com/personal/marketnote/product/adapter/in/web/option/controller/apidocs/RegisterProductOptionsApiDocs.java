package com.personal.marketnote.product.adapter.in.web.option.controller.apidocs;

import com.personal.marketnote.common.adapter.in.api.schema.StringResponseSchema;
import com.personal.marketnote.product.adapter.in.web.option.request.UpdateProductOptionsRequest;
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
        summary = "(판매자/관리자) 상품 옵션 카테고리 및 하위 옵션 목록 생성",
        description = """
                작성일자: 2026-01-01
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 요청 상품에 새로운 옵션 카테고리와 하위 옵션들을 생성합니다.
                
                - 각 카테고리는 최소 1개 이상의 옵션을 포함해야 합니다.
                
                - 최초 등록 시 해당 상품의 기본 가격 정책으로 생성되며, 가격 정책 등록 API를 통해 새로운 가격 정책을 생성할 수 있습니다.
                
                - 상품 판매자 본인 또는 관리자만 가능합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | productId (path) | number | 상품 ID | Y | 1 |
                | categoryName | string | 생성할 옵션 카테고리명 | Y | "수량" |
                | options | array<object> | 카테고리 내 옵션 목록 | Y | [ ... ] |
                
                ### Request > options
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | content | string | 옵션 내용 | Y | "1박스" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 201: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "ERR01" / "ERR02" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-01T10:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "상품 카테고리 및 옵션 목록 등록 성공" |
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | optionCategoryId | number | 옵션 카테고리 ID | 10 |
                | optionIds | array<number> | 옵션 ID 목록 | [100,101] |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "productId",
                        in = ParameterIn.PATH,
                        required = true,
                        description = "상품 ID",
                        schema = @Schema(type = "number", example = "1")
                )
        },
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = UpdateProductOptionsRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "categoryName": "개당 수량",
                                  "options": [
                                    { "content": "30개입" },
                                    { "content": "60개입" }
                                  ]
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "등록 성공",
                        content = @Content(
                                schema = @Schema(implementation = StringResponseSchema.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 201,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-01T17:32:49.097185",
                                          "content": {
                                            "optionCategoryId": 2,
                                            "optionIds": [
                                              2,
                                              3
                                            ]
                                          },
                                          "message": "상품 카테고리 및 옵션 목록 등록 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "중복된 회원 등록",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 409,
                                          "code": "ERR01",
                                          "timestamp": "2025-12-28T11:50:53.656526",
                                          "content": null,
                                          "message": "이미 가입된 회원입니다. 가입된 OIDC ID: 1234"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "인증 코드 검증 실패",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 401,
                                          "code": "ERR05",
                                          "timestamp": "2025-12-282T09:23:22.091551",
                                          "content": null,
                                          "message": "이메일 인증 코드가 유효하지 않거나 만료되었습니다. 전송된 이메일 주소: example@example.com"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "403",
                        description = "비활성화된 계정",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 403,
                                          "code": "ERR06",
                                          "timestamp": "2025-12-28T16:13:25.045291",
                                          "content": null,
                                          "message": "비활성화된 계정입니다. 전송된 이메일 주소: example@example.com"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "상품 소유자가 아님",
                        content = @Content(
                                schema = @Schema(implementation = StringResponseSchema.class),
                                examples = @ExampleObject("""
                                        {
                                            "statusCode": 400,
                                            "code": "ERR01",
                                            "timestamp": "2025-12-31T14:51:42.27111",
                                            "content": null,
                                            "message": "관리자 또는 상품 판매자가 아닙니다. 전송된 상품 ID: 11"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "존재하지 않는 상품",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 404,
                                          "code": "NOT_FOUND",
                                          "timestamp": "2026-01-01T17:32:49.097185",
                                          "content": null,
                                          "message": "상품을 찾을 수 없습니다. 전송된 상품 ID: 55"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "옵션 없음",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                            "statusCode": 400,
                                            "code": "ERR02",
                                            "timestamp": "2026-01-01T17:32:49.097185",
                                            "content": null,
                                            "message": "각 옵션 카테고리는 최소 1개 이상의 옵션을 포함해야 합니다."
                                        }
                                        """)
                        )
                )
        }
)
public @interface RegisterProductOptionsApiDocs {
}
