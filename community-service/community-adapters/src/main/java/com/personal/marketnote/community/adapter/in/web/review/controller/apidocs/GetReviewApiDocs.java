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
                | reviewerId | number | 작성자 ID | 5 |
                | orderId | number | 주문 ID | 100 |
                | productId | number | 상품 ID | 30 |
                | pricePolicyId | number | 가격 정책 ID | 15 |
                | productImageUrl | string | 상품 이미지 URL | "https://example.com/image.jpg" |
                | selectedOptions | string | 선택 옵션 | "색상: 블랙 / 사이즈: M" |
                | quantity | number | 수량 | 2 |
                | maskedReviewerName | string | 마스킹된 작성자명 | "홍*동" |
                | rating | number | 리뷰 평점 | 4.5 |
                | content | string | 내용 | "배송 언제 오나요?" |
                | isPhoto | boolean | 이미지 첨부 여부 | true |
                | images | array | 리뷰 이미지 목록 | [ ... ] |
                | isEdited | boolean | 수정 여부 | false |
                | likeCount | number | 좋아요 수 | 3 |
                | isUserLiked | boolean | 현재 사용자 좋아요 여부 | false |
                | status | string | 리뷰 상태 | "ACTIVE" |
                | createdAt | string(datetime) | 생성 일시 | "2026-01-27T09:30:00.000000" |
                | modifiedAt | string(datetime) | 수정 일시 | "2026-01-27T09:30:00.000000" |
                | orderNum | number | 정렬 순서 | 10 |
                | product | object | 상품 정보 | { ... } |

                ---

                ### Response > content > product

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | name | string | 상품명 | "비타민C 1000mg" |
                | brandName | string | 브랜드명 | "마켓노트" |
                | pricePolicy | object | 가격 정책 | { ... } |
                | catalogImage | object | 카탈로그 이미지 | { ... } |
                | unitAmount | number | 단위 수량 | 60 |

                ---

                ### Response > content > product > pricePolicy

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 가격 정책 ID | 15 |
                | price | number | 정가 | 30000 |
                | discountPrice | number | 할인가 | 25000 |
                | discountRate | number | 할인율 | 16.67 |
                | accumulatedPoint | number | 적립 포인트 | 250 |

                ---

                ### Response > content > product > catalogImage

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 파일 ID | 50 |
                | sort | string | 파일 분류 | "CATALOG_IMAGE" |
                | extension | string | 확장자 | "jpg" |
                | name | string | 파일명 | "비타민C" |
                | storageUrl | string | 저장 URL | "https://marketnote.s3.amazonaws.com/product/30/catalog.jpg" |
                | resizedStorageUrls | array | 리사이즈 URL 목록 | [] |
                | orderNum | number | 정렬 순서 | 50 |
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
                                            "reviewerId": 5,
                                            "orderId": 100,
                                            "productId": 30,
                                            "pricePolicyId": 15,
                                            "productImageUrl": "https://example.com/image.jpg",
                                            "selectedOptions": "색상: 블랙 / 사이즈: M",
                                            "quantity": 2,
                                            "maskedReviewerName": "홍*동",
                                            "rating": 4.5,
                                            "content": "배송 언제 오나요?",
                                            "isPhoto": true,
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
                                            "isEdited": false,
                                            "likeCount": 3,
                                            "isUserLiked": false,
                                            "status": "ACTIVE",
                                            "createdAt": "2026-01-27T09:30:00.000000",
                                            "modifiedAt": "2026-01-27T09:30:00.000000",
                                            "orderNum": 10,
                                            "product": {
                                              "name": "비타민C 1000mg",
                                              "brandName": "마켓노트",
                                              "pricePolicy": {
                                                "id": 15,
                                                "price": 30000,
                                                "discountPrice": 25000,
                                                "discountRate": 16.67,
                                                "accumulatedPoint": 250
                                              },
                                              "catalogImage": {
                                                "id": 50,
                                                "sort": "CATALOG_IMAGE",
                                                "extension": "jpg",
                                                "name": "비타민C",
                                                "storageUrl": "https://marketnote.s3.amazonaws.com/product/30/catalog.jpg",
                                                "resizedStorageUrls": [],
                                                "orderNum": 50
                                              },
                                              "unitAmount": 60
                                            }
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
