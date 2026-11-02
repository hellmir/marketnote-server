package com.personal.marketnote.product.adapter.in.web.product.controller.apidocs;

import com.personal.marketnote.common.adapter.in.api.schema.StringResponseSchema;
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
        summary = "(비회원) 상품 검색 대상 목록 조회",
        description = """
                작성일자: 2026-01-02
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                상품 검색 대상 목록을 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-02T10:37:32.320824" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "상품 검색 대상 목록 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | targets | array | 상품 검색 대상 목록 | [ ... ] |
                
                ---
                
                ### Response > content > targets
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | name | string | 검색 대상 이름 | "NAME" |
                | description | string | 검색 대상 설명 | "상품명" |
                """, security = {
        @SecurityRequirement(name = "bearer")
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
                                          "timestamp": "2026-01-02T10:54:07.722551",
                                          "content": {
                                            "targets": [
                                              {
                                                "name": "NAME",
                                                "description": "상품명"
                                              },
                                              {
                                                "name": "BRAND_NAME",
                                                "description": "브랜드명"
                                              }
                                            ]
                                          },
                                          "message": "상품 검색 대상 목록 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetProductSearchTargetsApiDocs {
}


