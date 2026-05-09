package com.personal.marketnote.community.adapter.in.web.review.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "리뷰 상세 정보 조회",
        description = """
                작성일자: 2026-01-27
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                리뷰 상세 정보를 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | id(path) | number | 리뷰 ID | Y | 10 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-27T10:05:12.123456" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "리뷰 상세 정보 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 리뷰 ID | 10 |
                | userId | number | 작성자 ID | 5 |
                | writerName | string | 작성자명 | "홍길동" |
                | content | string | 내용 | "배송 언제 오나요?" |
                | rating | number | 리뷰 평점 | 4.5 |
                | productImageUrl | string | 상품 이미지 URL | "https://example.com/image.jpg" |
                | isPhoto | boolean | 이미지 첨부 여부 | true |
                | images | array | 리뷰 이미지 목록 | [ ... ] |
                | createdAt | string(datetime) | 생성 일시 | "2026-01-27T09:30:00.000000" |
                | modifiedAt | string(datetime) | 수정 일시 | "2026-01-27T09:30:00.000000" |
                """,
        parameters = {
                @Parameter(
                        name = "id",
                        in = ParameterIn.PATH,
                        required = true,
                        description = "리뷰 ID",
                        schema = @Schema(type = "number", example = "10")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "리뷰 상세 정보 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-27T10:05:12.123456",
                                          "content": {
                                            "id": 10,
                                            "userId": 5,
                                            "writerName": "홍길동",
                                            "content": "배송 언제 오나요?",
                                            "rating": 4.5,
                                            "productImageUrl": "https://example.com/image.jpg",
                                            "isPhoto": false,
                                            "images": [
                                              {
                                                "id": 79,
                                                "sort": "REVIEW_IMAGE",
                                                "extension": "png",
                                                "name": "리뷰2",
                                                "storageUrl": "https://marketnote.s3.amazonaws.com/review/35/1765528094927_image.png",
                                                "resizedStorageUrls": [],
                                                "orderNum": 79
                                              },
                                              {
                                                "id": 78,
                                                "sort": "REVIEW_IMAGE",
                                                "extension": "jpg",
                                                "name": "리뷰1",
                                                "storageUrl": "https://marketnote.s3.amazonaws.com/review/35/1765528092213_grafana-icon.png",
                                                "resizedStorageUrls": [],
                                                "orderNum": 78
                                              }
                                            ],
                                            "createdAt": "2026-01-27T09:30:00.000000",
                                            "modifiedAt": "2026-01-27T09:30:00.000000"
                                          },
                                          "message": "리뷰 상세 정보 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetReviewApiDocs {
}
