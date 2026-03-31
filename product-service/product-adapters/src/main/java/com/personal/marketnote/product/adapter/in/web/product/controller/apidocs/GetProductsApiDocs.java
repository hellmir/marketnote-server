package com.personal.marketnote.product.adapter.in.web.product.controller.apidocs;

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
        summary = "(비회원) 상품 목록 조회",
        description = """
                작성일자: 2026-01-01
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 상품 목록을 조회합니다.
                
                - 각 카테고리, 정렬 기준별 첫 페이지에 Redis Cache 적용되어 있습니다. TTL: 2분
                
                - categoryId 미 전송 시 전체 상품 목록을 반환합니다.
                
                - categoryId 전송 시 해당 카테고리의 상품 목록을 반환합니다.
                
                - 페이로드에 cursor 값이 없는 경우(첫 페이지): 총 상품 개수 반환 O
                
                - 페이로드에 cursor 값이 있는 경우(더 보기): 총 상품 개수 반환 X
                
                - 옵션별 상품 목록 조회 설정된 상품은 모든 옵션 카테고리 조합에 대해 개별 상품으로 조회되며, selectedOptions 배열이 추가로 제공됩니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | categoryId | number | 카테고리 ID | N | 1001 |
                | pricePolicyIds | array<number> | 가격 정책 ID 목록 | N | 1, 2 |
                | cursor | number | 이전 페이지의 nextCursor 값, 전송하지 않는 경우 첫 데이터부터 조회 | N | 1 |
                | page-size | number | 페이지 크기 | N | 4 |
                | sortDirection | string | 정렬 방향(ASC, DESC) | N | DESC |
                | sortProperty | string | 정렬 속성(ORDER_NUM, POPULARITY, DISCOUNT_PRICE, ACCUMULATED_POINT, ACCUMULATED_POINT_RATE) | N | ORDER_NUM |
                | searchTarget | string | 검색 대상(NAME, BRAND_NAME) | N | NAME |
                | searchKeyword | string | 검색 키워드 | N | "노트왕" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-01T10:37:32.320824" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "상품 목록 조회 성공" |
                
                ---
                
                ### Response > content > products
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | totalElements | number | 총 아이템 수 | 30 |
                | nextCursor | number | 현재 페이지에서 조회한 마지막 리소스의 식별자 | 18 |
                | hasNext | boolean | 다음 페이지 존재 여부 | true |
                | items | array | 상품 목록 | [ ... ] |
                
                ---
                
                ### Response > content > products > items
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 상품 ID | 1 |
                | sellerId | number | 판매자 ID | 10 |
                | name | string | 상품명 | "스프링노트1" |
                | brandName | string | 브랜드명 | "노트왕" |
                | pricePolicy | object | 가격 정책 | { ... } |
                | sales | number | 판매량 | 0 |
                | productTags | array | 상품 태그 목록 | [ ... ] |
                | catalogImage | object | 상품 카탈로그 이미지 | { ... } |
                | selectedOptions | array | 선택된 옵션 목록 | [ ... ] |
                | stock | number | 재고 수량(재고 서버와 통신 실패한 경우 -1 반환) | 10 |
                | averageRating | number | 평점 평균 | 4.5 |
                | totalCount | number | 리뷰 총 개수 | 8 |
                | status | string | 상태 | "ACTIVE" |
                | orderNum | number | 정렬 순서 | 1 |
                
                ---
                
                ### Response > content > products > pricePolicy
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 가격 정책 ID | 22 |
                | price | number | 기본 판매 가격(원) | 50000 |
                | discountPrice | number | 할인 가격(원) | 40000 |
                | accumulatedPoint | number | 적립 포인트 | 2000 |
                | discountRate | number | 할인율(%, 최대 소수점 1자리) | 20 |
                | optionIds | array<number> | 옵션 ID 목록 | [8, 11] |
                
                ---
                
                ### Response > content > products > productTags
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 상품 태그 ID | 1 |
                | productId | number | 상품 ID | 10 |
                | name | string | 상품 태그명 | "루테인" |
                | orderNum | number | 정렬 순서 | 1 |
                | status | string | 상태 | "ACTIVE" |
                
                ---
                
                ### Response > content > products > catalogImage
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 이미지 ID | 1 |
                | sort | string | 이미지 종류 | "PRODUCT_CATALOG_IMAGE" |
                | extension | string | 이미지 확장자 | "jpg" |
                | name | string | 이미지명 | "스프링노트1" |
                | storageUrl | string | 이미지 스토리지 URL | "https://marketnote.s3.amazonaws.com/product/30/1763517082648_grafana-icon.png" |
                | resizedStorageUrls | array | 리사이즈 이미지 스토리지 URL 목록 | [ "https://marketnote.s3.amazonaws.com/product/30/1763517083251_grafana-icon_300x300.png" ] |
                | orderNum | number | 정렬 순서 | 1 |
                
                ---
                
                ### Response > content > products > selectedOptions
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 옵션 ID | 1 |
                | content | string | 옵션 내용 | "1박스" |
                | status | string | 상태 | "ACTIVE" |
                """, security = {
        @SecurityRequirement(name = "bearer")
},
        parameters = {
                @Parameter(
                        name = "categoryId",
                        in = ParameterIn.QUERY,
                        description = "카테고리 ID(없는 경우 전체 조회)",
                        schema = @Schema(type = "number")
                ),
                @Parameter(
                        name = "pricePolicyIds",
                        description = "가격 정책 ID 목록",
                        in = ParameterIn.QUERY,
                        schema = @Schema(type = "array")
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
                                example = "ORDER_NUM",
                                allowableValues = {"ORDER_NUM", "POPULARITY", "DISCOUNT_PRICE", "ACCUMULATED_POINT", "ACCUMULATED_POINT_RATE"},
                                defaultValue = "ORDER_NUM"
                        )
                ),
                @Parameter(
                        name = "searchTarget",
                        in = ParameterIn.QUERY,
                        description = "검색 대상",
                        schema = @Schema(
                                type = "string",
                                allowableValues = {"NAME", "BRAND_NAME"},
                                defaultValue = "NAME"
                        )
                ),
                @Parameter(
                        name = "searchKeyword",
                        in = ParameterIn.QUERY,
                        description = "검색 키워드(없는 경우 전체 조회)",
                        schema = @Schema(type = "string")
                )
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
                                          "timestamp": "2026-01-04T23:35:03.051692",
                                          "content": {
                                            "products": {
                                              "totalElements": 16,
                                              "hasNext": true,
                                              "nextCursor": 20,
                                              "items": [
                                                {
                                                  "id": 36,
                                                  "sellerId": 1,
                                                  "name": "스프링노트1",
                                                  "brandName": "노트왕",
                                                  "pricePolicy": {
                                                    "id": 28,
                                                    "price": 45000,
                                                    "discountPrice": 37000,
                                                    "discountRate": 3.2,
                                                    "accumulatedPoint": 1200
                                                  },
                                                  "sales": 0,
                                                  "productTags": [
                                                    {
                                                      "id": 25,
                                                      "productId": 36,
                                                      "name": "루테인",
                                                      "orderNum": null,
                                                      "status": "ACTIVE"
                                                    },
                                                    {
                                                      "id": 26,
                                                      "productId": 36,
                                                      "name": "아스타잔틴",
                                                      "orderNum": null,
                                                      "status": "ACTIVE"
                                                    }
                                                  ],
                                                  "catalogImage": null,
                                                  "selectedOptions": null,
                                                  "stock": 99990,
                                                  "averageRating": 4.5,
                                                  "totalCount": 8,
                                                  "orderNum": 36,
                                                  "status": "ACTIVE"
                                                },
                                                {
                                                  "id": 35,
                                                  "sellerId": 1,
                                                  "name": "스프링노트1",
                                                  "brandName": "노트왕",
                                                  "pricePolicy": {
                                                    "id": 26,
                                                    "price": 10000,
                                                    "discountPrice": 9000,
                                                    "discountRate": 11.1,
                                                    "accumulatedPoint": 1000
                                                  },
                                                  "sales": 0,
                                                  "productTags": [
                                                    {
                                                      "id": 23,
                                                      "productId": 35,
                                                      "name": "루테인",
                                                      "orderNum": null,
                                                      "status": "ACTIVE"
                                                    },
                                                    {
                                                      "id": 24,
                                                      "productId": 35,
                                                      "name": "아스타잔틴",
                                                      "orderNum": null,
                                                      "status": "ACTIVE"
                                                    }
                                                  ],
                                                  "catalogImage": null,
                                                  "selectedOptions": null,
                                                  "stock": null,
                                                  "averageRating": 0,
                                                  "totalCount": 0,
                                                  "orderNum": 35,
                                                  "status": "ACTIVE"
                                                },
                                                {
                                                  "id": 30,
                                                  "sellerId": 1,
                                                  "name": "건기식테스트1",
                                                  "brandName": "노트왕",
                                                  "pricePolicy": {
                                                    "id": 21,
                                                    "price": 40000,
                                                    "discountPrice": 32000,
                                                    "discountRate": 2.5,
                                                    "accumulatedPoint": 800
                                                  },
                                                  "sales": 0,
                                                  "productTags": [
                                                    {
                                                      "id": 13,
                                                      "productId": 30,
                                                      "name": "루테인",
                                                      "orderNum": null,
                                                      "status": "ACTIVE"
                                                    },
                                                    {
                                                      "id": 14,
                                                      "productId": 30,
                                                      "name": "아스타잔틴",
                                                      "orderNum": null,
                                                      "status": "ACTIVE"
                                                    }
                                                  ],
                                                  "catalogImage": {
                                                    "id": 28,
                                                    "sort": "PRODUCT_CATALOG_IMAGE",
                                                    "extension": "jpg",
                                                    "name": "스프링노트1",
                                                    "storageUrl": "https://marketnote.s3.amazonaws.com/product/30/1763521042462_grafana-icon.png",
                                                    "resizedStorageUrls": [
                                                      "https://marketnote.s3.amazonaws.com/product/30/1763521042802_grafana-icon_300x300.png"
                                                    ],
                                                    "orderNum": 28
                                                  },
                                                  "selectedOptions": [
                                                    {
                                                      "id": 8,
                                                      "content": "1박스",
                                                      "status": "ACTIVE"
                                                    },
                                                    {
                                                      "id": 10,
                                                      "content": "30개입",
                                                      "status": "ACTIVE"
                                                    }
                                                  ],
                                                  "stock": 2100,
                                                  "averageRating": 4.3,
                                                  "totalCount": 12,
                                                  "orderNum": 30,
                                                  "status": "ACTIVE"
                                                },
                                                {
                                                  "id": 30,
                                                  "sellerId": 1,
                                                  "name": "건기식테스트1",
                                                  "brandName": "노트왕",
                                                  "pricePolicy": {
                                                    "id": 20,
                                                    "price": 45000,
                                                    "discountPrice": 37000,
                                                    "discountRate": 3.2,
                                                    "accumulatedPoint": 1200
                                                  },
                                                  "sales": 0,
                                                  "productTags": [
                                                    {
                                                      "id": 13,
                                                      "productId": 30,
                                                      "name": "루테인",
                                                      "orderNum": null,
                                                      "status": "ACTIVE"
                                                    },
                                                    {
                                                      "id": 14,
                                                      "productId": 30,
                                                      "name": "아스타잔틴",
                                                      "orderNum": null,
                                                      "status": "ACTIVE"
                                                    }
                                                  ],
                                                  "catalogImage": {
                                                    "id": 28,
                                                    "sort": "PRODUCT_CATALOG_IMAGE",
                                                    "extension": "jpg",
                                                    "name": "스프링노트1",
                                                    "storageUrl": "https://marketnote.s3.amazonaws.com/product/30/1763521042462_grafana-icon.png",
                                                    "resizedStorageUrls": [
                                                      "https://marketnote.s3.amazonaws.com/product/30/1763521042802_grafana-icon_300x300.png"
                                                    ],
                                                    "orderNum": 28
                                                  },
                                                  "selectedOptions": [
                                                    {
                                                      "id": 8,
                                                      "content": "1박스",
                                                      "status": "ACTIVE"
                                                    },
                                                    {
                                                      "id": 11,
                                                      "content": "60개입",
                                                      "status": "ACTIVE"
                                                    }
                                                  ],
                                                  "stock": 2000,
                                                  "averageRating": 4.3,
                                                  "totalCount": 12,
                                                  "orderNum": 30,
                                                  "status": "ACTIVE"
                                                }
                                              ]
                                            }
                                          },
                                          "message": "상품 목록 조회 성공"
                                        }
                                        """)
                        )
                )
        })
public @interface GetProductsApiDocs {
}
