package com.personal.marketnote.product.adapter.in.web.category.controller.apidocs;

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
        summary = "(관리자) 카테고리 삭제",
        description = """
                작성일자: 2025-12-31
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 카테고리를 삭제합니다.
                
                - 하위 카테고리 또는 등록된 상품이 하나 이상 존재하는 경우 삭제할 수 없습니다.
                
                - 관리자만 가능합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | categoryId | number | 삭제 대상 카테고리 ID | Y | 10004 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "ERR01" / "ERR02" |
                | timestamp | string(datetime) | 응답 일시 | "2025-12-31T16:30:14.004058" |
                | content | object | 응답 본문 | null |
                | message | string | 처리 결과 | "카테고리 삭제 성공" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "categoryId",
                        in = ParameterIn.PATH,
                        required = true,
                        description = "삭제 대상 카테고리 ID",
                        schema = @Schema(type = "number", example = "10004")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "OK",
                        content = @Content(
                                schema = @Schema(implementation = StringResponseSchema.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2025-12-31T12:00:00.000",
                                          "content": null,
                                          "message": "카테고리 삭제 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "카테고리가 존재하지 않음",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 404,
                                          "code": "NOT_FOUND",
                                          "timestamp": "2025-12-31T12:06:00.000",
                                          "content": null,
                                          "message": "존재하지 않는 카테고리 ID입니다. 전송된 카테고리 ID: 999"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "하위 카테고리 존재 또는 이미 사용 중인 카테고리",
                        content = @Content(
                                examples = {
                                        @ExampleObject(
                                                name = "하위 카테고리 존재",
                                                summary = "삭제 불가 - 하위 카테고리 존재",
                                                value = """
                                                        {
                                                          "statusCode": 409,
                                                          "code": "ERR01",
                                                          "timestamp": "2025-12-31T12:05:00.000",
                                                          "content": null,
                                                          "message": "하위 카테고리가 존재합니다. 전송된 카테고리 ID: 1"
                                                        }
                                                        """
                                        ),
                                        @ExampleObject(
                                                name = "카테고리 사용 중",
                                                summary = "삭제 불가 - 상품이 연결된 카테고리",
                                                value = """
                                                        {
                                                          "statusCode": 409,
                                                          "code": "ERR02",
                                                          "timestamp": "2025-12-31T16:59:42.911819",
                                                          "content": null,
                                                          "message": "해당 카테고리에 등록된 상품이 존재합니다. 전송된 카테고리 ID: 10004"
                                                        }
                                                        """
                                        )
                                }
                        )
                )
        }
)
public @interface DeleteCategoryApiDocs {
}


