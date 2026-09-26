package com.personal.marketnote.file.adapter.in.client.file.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
        summary = "파일 목록 업데이트",
        description = """
                작성일자: 2026-01-03
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 요청 리소스의 파일 목록을 업데이트합니다. 기존 파일 목록은 비활성화됩니다.
                
                - 각 파일에 대해 파일 종류(sort)를 지정해야 합니다.
                
                    - 상품 이미지
                
                        - 상품 카탈로그 이미지: "PRODUCT_CATALOG_IMAGE"
                
                        - 상품 상세 정보 상단 대표 이미지: "PRODUCT_REPRESENTATIVE_IMAGE"
                
                        - 상품 상세 정보 본문 이미지: "PRODUCT_CONTENT_IMAGE"
                
                    - 리뷰 이미지: "REVIEW_IMAGE"
                
                    - 취소 사유 이미지: "CANCEL_REASON_IMAGE"
                
                    - 교환 사유 이미지: "EXCHANGE_REASON_IMAGE"
                
                    - 환불 사유 이미지: "REFUND_REASON_IMAGE"
                
                    - 게시글 이미지: "POST_IMAGE"
                
                    - 아이콘: "ICON"
                
                    - 기타: "ETC"
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | ownerType | string | 소유 도메인 타입 | Y | "PRODUCT": 상품 / "ORDER": 주문 / "POST": 게시글 / "REVIEW": 리뷰 |
                | ownerId | number | 소유 도메인 ID | Y | 1 |
                | ownerKey | string | 소유자 키 (최대 64자) | Y | "01HZXQ2K6E5X7F9Y3Z1M8N4Q0P" |
                | file | file[] | 업로드할 파일 목록 | Y | [file1, file2] |
                | sort | string[] | 파일 종류 목록 | Y | ["PRODUCT_CATALOG_IMAGE", "PRODUCT_REPRESENTATIVE_IMAGE", "REVIEW_IMAGE"] |
                | extension | string[] | 파일 확장자 목록 | N | ["jpg", "png"] |
                | name | string[] | 파일명 목록 | N | ["스프링노트1", "스프링노트2"] |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 201: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-03T12:12:30.013" |
                | content | object | 응답 본문 | null |
                | message | string | 처리 결과 | "파일 추가 성공" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(name = "ownerType", in = ParameterIn.QUERY, required = true,
                        description = "소유 도메인 타입",
                        schema = @Schema(type = "string", example = "PRODUCT")
                ),
                @Parameter(name = "ownerId", in = ParameterIn.QUERY, required = true,
                        description = "소유 도메인 ID",
                        schema = @Schema(type = "number", example = "1")
                ),
                @Parameter(name = "ownerKey", in = ParameterIn.QUERY, required = true,
                        description = "소유자 키 (최대 64자)",
                        schema = @Schema(type = "string", example = "01HZXQ2K6E5X7F9Y3Z1M8N4Q0P")
                ),
                @Parameter(name = "file", in = ParameterIn.QUERY, required = true,
                        description = "업로드할 파일 목록",
                        content = @Content(
                                mediaType = "multipart/form-data",
                                array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))
                        )
                ),
                @Parameter(name = "sort", in = ParameterIn.QUERY, required = true,
                        description = "파일 종류 목록",
                        array = @ArraySchema(schema = @Schema(type = "string", example = "PRODUCT_CATALOG_IMAGE"))
                ),
                @Parameter(name = "extension", in = ParameterIn.QUERY,
                        description = "파일 확장자 목록",
                        array = @ArraySchema(schema = @Schema(type = "string", example = "jpg"))
                ),
                @Parameter(name = "name", in = ParameterIn.QUERY,
                        description = "파일명 목록",
                        array = @ArraySchema(schema = @Schema(type = "string", example = "스프링노트1"))
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "파일 추가 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 201,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-03T12:12:30.013",
                                          "content": null,
                                          "message": "파일 추가 성공"
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
                                          "timestamp": "2026-01-03T12:12:30.013",
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
                                          "timestamp": "2026-01-03T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        })
public @interface UpdateFilesApiDocs {
}
