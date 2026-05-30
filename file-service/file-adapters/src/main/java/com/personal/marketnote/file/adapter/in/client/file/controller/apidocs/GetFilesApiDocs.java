package com.personal.marketnote.file.adapter.in.client.file.controller.apidocs;

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
        summary = "파일 목록 조회",
        description = """
                작성일자: 2026-01-03
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 소유 도메인(상품 / 리뷰)의 파일 목록을 조회합니다.
                
                - 파일 종류(sort)를 지정하여 파일 목록을 조회할 수 있습니다.
                
                    - 상품 이미지
                
                        - 상품 카탈로그 이미지: "PRODUCT_CATALOG_IMAGE"
                
                        - 상품 상세 정보 상단 대표 이미지: "PRODUCT_REPRESENTATIVE_IMAGE"
                
                        - 상품 상세 정보 본문 이미지: "PRODUCT_CONTENT_IMAGE"
                
                    - 환불 사유 이미지: "REFUND_REASON_IMAGE"
                
                    - 리뷰 이미지: "REVIEW_IMAGE"
                
                    - 아이콘: "ICON"
                
                    - 기타: "ETC"
                
                - 파일 종류(sort)를 지정하지 않으면 해당 도메인의 모든 파일 목록을 조회합니다.
                
                - 가장 최신의 5개까지만 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | ownerType | string | 소유 도메인 타입 | Y | "PRODUCT": 상품 / "ORDER": 주문 / "REVIEW": 리뷰 |
                | ownerId | number | 소유 도메인 ID | Y | 1 |
                | sort | string | 파일 종류 | N | "PRODUCT_CATALOG_IMAGE" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-03T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "파일 목록 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | files | array | 파일 목록 | [ ... ] |
                
                ---
                
                ### Response > content > files
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 파일 ID | 1 |
                | sort | string | 파일 종류 | "PRODUCT_CATALOG_IMAGE" |
                | extension | string | 파일 확장자 | "jpg" |
                | name | string | 파일명 | "스프링노트1" |
                | storageUrl | string | 스토리지 URL | "https://bucket.s3.amazonaws.com/product/1/original.jpg" |
                | resizedStorageUrls | array | 리사이즈 이미지 스토리지 URL 목록 | ["https://bucket.s3.amazonaws.com/product/1/300x300_original.jpg", "https://bucket.s3.amazonaws.com/product/1/500x500_original.jpg"] |
                | orderNum | number | 파일 정렬 순서 | 1 |
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
                @Parameter(name = "sort", in = ParameterIn.QUERY, required = false,
                        description = "파일 종류(선택)",
                        schema = @Schema(type = "string", example = "PRODUCT_CATALOG_IMAGE")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "파일 목록 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-03T18:07:29.561027",
                                          "content": {
                                            "files": [
                                              {
                                                "id": 8,
                                                "sort": "PRODUCT_CATALOG_IMAGE",
                                                "extension": "png",
                                                "name": "스프링노트1",
                                                "storageUrl": "https://marketnote.s3.amazonaws.com/product/1/1763456309061_gungisick1.png",
                                                "resizedStorageUrls": [
                                                  "https://marketnote.s3.amazonaws.com/product/1/1763456309061_gungisick1_300x300.png",
                                                  "https://marketnote.s3.amazonaws.com/product/1/1763456309061_gungisick1_500x500.png"
                                                ],
                                                "orderNum": 1
                                              },
                                              {
                                                "id": 10,
                                                "sort": "PRODUCT_CATALOG_IMAGE",
                                                "extension": "png",
                                                "name": "스프링노트2",
                                                "storageUrl": "https://marketnote.s3.amazonaws.com/product/1/1763456845957_gungisick2.png",
                                                "resizedStorageUrls": [
                                                  "https://marketnote.s3.amazonaws.com/product/1/1763456845957_gungisick2_300x300.png",
                                                  "https://marketnote.s3.amazonaws.com/product/1/1763456845957_gungisick2_500x500.png"
                                                ],
                                                "orderNum": 2
                                              }
                                            ]
                                          },
                                          "message": "파일 목록 조회 성공"
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
public @interface GetFilesApiDocs {
}
