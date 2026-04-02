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
        summary = "(비회원) 상품 리뷰 목록 조회",
        description = """
                작성일자: 2026-01-10
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 상품 리뷰 목록을 조회합니다.
                
                - 포토 리뷰 목록 첫 페이지에 Redis Cache 적용되어 있습니다. TTL: 10분
                
                - 페이로드에 cursor 값이 없는 경우(첫 페이지): 총 리뷰 개수 반환 O
                
                - 페이로드에 cursor 값이 있는 경우(더 보기): 총 리뷰 개수 반환 X
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | productId | number | 상품 ID | Y | 1 |
                | isPhoto | boolean | 포토 리뷰 여부(포토 리뷰만 조회할지 여부) | Y | false |
                | cursor | number | 이전 페이지의 nextCursor 값, 전송하지 않는 경우 첫 데이터부터 조회 | N | 1 |
                | pageSize | number | 페이지 크기 | N | 4 |
                | sortDirection | string | 정렬 방향(ASC, DESC) | N | DESC |
                | sortProperty | string | 정렬 속성(ID, ORDER_NUM, LIKE, RATING) | N | ID |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 201 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-09T16:32:18.828188" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "리뷰 등록 성공" |
                
                ---
                
                ### Response > content
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | reviews | array | 리뷰 목록 | [ ... ] |
                
                ---
                
                ### Response > content > reviews
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | totalElements | number | 총 리뷰 개수 | 100 |
                | hasNext | boolean | 다음 페이지 존재 여부 | true |
                | nextCursor | number | 현재 페이지에서 조회한 마지막 리소스의 식별자 | 18 |
                | items | array | 리뷰 목록 | [ ... ] |
                
                ---
                
                ### Response > content > reviews > items
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 리뷰 ID | 3 |
                | reviewerId | number | 리뷰어 ID | 17 |
                | orderId | number | 주문 ID | 1 |
                | productId | number | 상품 ID | 1 |
                | pricePolicyId | number | 가격 정책 ID | 11 |
                | productImageUrl | string | 상품 이미지 URL | "https://example.com/image.jpg" |
                | selectedOptions | string | 선택된 옵션 목록 | "30개입, 5박스" |
                | quantity | number | 주문 수량 | 2 |
                | maskedReviewerName | string | 리뷰 작성자 이름(마스킹) | "홍*동" |
                | rating | number | 평점 | 4 |
                | content | string | 리뷰 내용 | "배송이 빠르고 포장 상태도 좋았습니다." |
                | isPhoto | boolean | 포토 리뷰 여부 | false |
                | images | array | 리뷰 이미지 목록 | [ ... ] |
                | isEdited | boolean | 수정 여부 | false |
                | likeCount | number | 좋아요 개수 | 0 |
                | isUserLiked | boolean | 로그인 사용자가 좋아요를 눌렀는지 여부 | false |
                | status | string | 상태 | "ACTIVE" |
                | createdAt | string(datetime) | 생성 일시 | "2026-01-10T16:57:59.792312" |
                | modifiedAt | string(datetime) | 수정 일시 | "2026-01-10T16:57:59.805601" |
                | orderNum | number | 정렬 순서 | 1 |
                
                ---
                
                ### Response > content > reviews > items > images
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 이미지 ID | 79 |
                | sort | string | 이미지 정렬 순서 | "REVIEW_IMAGE" |
                | extension | string | 이미지 확장자 | "png" |
                | name | string | 이미지 이름 | "리뷰2" |
                | storageUrl | string | 스토리지 URL | "https://marketnote.s3.amazonaws.com/review/35/1765528094927_image.png" |
                | resizedStorageUrls | array | 리사이즈 이미지 스토리지 URL 목록 | ["https://bucket.s3.amazonaws.com/product/1/300x300_original.jpg", "https://bucket.s3.amazonaws.com/product/1/500x500_original.jpg"] |
                | orderNum | number | 정렬 순서 | 79 |
                """,
        parameters = {
                @Parameter(
                        name = "productId",
                        in = ParameterIn.PATH,
                        description = "상품 ID",
                        schema = @Schema(type = "number")
                ),
                @Parameter(
                        name = "isPhoto",
                        in = ParameterIn.QUERY,
                        description = "포토 리뷰 여부",
                        schema = @Schema(type = "boolean")
                ),
                @Parameter(
                        name = "cursor",
                        in = ParameterIn.QUERY,
                        description = "이전 페이지의 nextCursor 값, 전송하지 않는 경우 첫 데이터부터 조회",
                        schema = @Schema(type = "number")
                ),
                @Parameter(
                        name = "pageSize",
                        in = ParameterIn.QUERY,
                        description = "페이지 크기",
                        schema = @Schema(
                                type = "number",
                                example = "4",
                                defaultValue = "4"
                        )
                ),
                @Parameter(
                        name = "sortDirection",
                        in = ParameterIn.QUERY,
                        description = "정렬 방향",
                        schema = @Schema(
                                type = "string",
                                example = "DESC",
                                allowableValues = {"ASC", "DESC"},
                                defaultValue = "DESC"
                        )
                ),
                @Parameter(
                        name = "sortProperty",
                        in = ParameterIn.QUERY,
                        description = "정렬 속성",
                        schema = @Schema(
                                type = "string",
                                example = "ID",
                                allowableValues = {"ID", "ORDER_NUM", "LIKE", "RATING"},
                                defaultValue = "ID"
                        )
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "상품 리뷰 목록 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-10T07:06:09.309111888",
                                          "content": {
                                            "reviews": {
                                              "totalElements": 7,
                                              "hasNext": true,
                                              "nextCursor": 20,
                                              "items": [
                                                {
                                                  "id": 35,
                                                  "reviewerId": 17,
                                                  "orderId": 1,
                                                  "productId": 55,
                                                  "pricePolicyId": 11222111,
                                                  "productImageUrl": "https://example.com/image.jpg",
                                                  "selectedOptions": "30개입, 5박스",
                                                  "quantity": 2,
                                                  "maskedReviewerName": "홍*동",
                                                  "rating": 5,
                                                  "content": "배송이 빠르고 포장 상태도 좋았습니다.",
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
                                                  "likeCount": 0,
                                                  "isUserLiked": false,
                                                  "status": "ACTIVE",
                                                  "createdAt": "2026-01-16T14:38:03.084745",
                                                  "modifiedAt": "2026-01-16T14:38:03.410822",
                                                  "orderNum": 35
                                                },
                                                {
                                                  "id": 34,
                                                  "reviewerId": 17,
                                                  "orderId": 1,
                                                  "productId": 55,
                                                  "pricePolicyId": 11222,
                                                  "productImageUrl": "https://example.com/image.jpg",
                                                  "selectedOptions": "30개입, 5박스",
                                                  "quantity": 2,
                                                  "maskedReviewerName": "홍*동",
                                                  "rating": 5,
                                                  "content": "배송이 빠르고 포장 상태도 좋았습니다.",
                                                  "isPhoto": false,
                                                  "images": null,
                                                  "isEdited": false,
                                                  "likeCount": 0,
                                                  "isUserLiked": false,
                                                  "status": "ACTIVE",
                                                  "createdAt": "2026-01-16T14:07:32.571571",
                                                  "modifiedAt": "2026-01-16T14:07:47.737044",
                                                  "orderNum": 34
                                                },
                                                {
                                                  "id": 21,
                                                  "reviewerId": 17,
                                                  "orderId": 1111,
                                                  "productId": 55,
                                                  "pricePolicyId": 66,
                                                  "productImageUrl": "https://example.com/image.jpg",
                                                  "selectedOptions": "30개입, 5박스",
                                                  "quantity": 2,
                                                  "maskedReviewerName": "홍*동",
                                                  "rating": 1,
                                                  "content": "배송이 느리고 포장 상태도 나빴습니다.",
                                                  "isPhoto": false,
                                                  "images": null,
                                                  "isEdited": true,
                                                  "likeCount": 0,
                                                  "isUserLiked": false,
                                                  "status": "ACTIVE",
                                                  "createdAt": "2026-01-10T15:29:33.861599",
                                                  "modifiedAt": "2026-01-16T14:37:49.032131",
                                                  "orderNum": 21
                                                },
                                                {
                                                  "id": 20,
                                                  "reviewerId": 17,
                                                  "orderId": 111,
                                                  "productId": 55,
                                                  "pricePolicyId": 66,
                                                  "productImageUrl": "https://example.com/image.jpg",
                                                  "selectedOptions": "30개입, 5박스",
                                                  "quantity": 2,
                                                  "maskedReviewerName": "홍*동",
                                                  "rating": 3,
                                                  "content": "배송이 빠르고 포장 상태도 좋았습니다.",
                                                  "isPhoto": false,
                                                  "images": null,
                                                  "isEdited": false,
                                                  "likeCount": 0,
                                                  "isUserLiked": false,
                                                  "status": "ACTIVE",
                                                  "createdAt": "2026-01-10T15:28:56.10613",
                                                  "modifiedAt": "2026-01-10T15:28:56.15211",
                                                  "orderNum": 20
                                                }
                                              ]
                                            }
                                          },
                                          "message": "상품 리뷰 목록 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetProductReviewsApiDocs {
}
