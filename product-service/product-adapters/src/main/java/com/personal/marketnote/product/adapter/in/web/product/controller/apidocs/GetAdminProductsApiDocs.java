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
        summary = "(관리자) 상품 목록 조회",
        description = """
                작성일자: 2026-02-03
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 관리자 페이지에서 상품 목록을 조회합니다.
                
                - 오프셋 기반 페이지네이션을 사용합니다. (page: 0-based)
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | categoryId | number | 카테고리 ID | N | 1001 |
                | pricePolicyIds | array<number> | 가격 정책 ID 목록 | N | 1, 2 |
                | page | number | 페이지 번호 (0-based) | N | 0 |
                | page-size | number | 페이지 크기 | N | 10 |
                | sortDirection | string | 정렬 방향(ASC, DESC) | N | DESC |
                | sortProperty | string | 정렬 속성(ORDER_NUM, POPULARITY, DISCOUNT_PRICE, ACCUMULATED_POINT) | N | ORDER_NUM |
                | searchTarget | string | 검색 대상(NAME, BRAND_NAME) | N | NAME |
                | searchKeyword | string | 검색 키워드 | N | "노트왕" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-02-03T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "관리자 상품 목록 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | products | object | 상품 목록(OffsetResponse) | { ... } |
                
                ---
                
                ### Response > content > products
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | page | number | 현재 페이지 번호 (0-based) | 0 |
                | pageSize | number | 페이지 크기 | 10 |
                | totalElements | number | 총 아이템 수 | 30 |
                | totalPages | number | 총 페이지 수 | 3 |
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
                
                ### Response > content > products > items > pricePolicy
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 가격 정책 ID | 22 |
                | price | number | 기본 판매 가격(원) | 50000 |
                | discountPrice | number | 할인 가격(원) | 40000 |
                | accumulatedPoint | number | 적립 포인트 | 2000 |
                | discountRate | number | 할인율(%, 최대 소수점 1자리) | 20 |
                | optionIds | array<number> | 옵션 ID 목록 | [8, 11] |
                
                ---
                
                ### Response > content > products > items > productTags
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 상품 태그 ID | 1 |
                | productId | number | 상품 ID | 10 |
                | name | string | 상품 태그명 | "루테인" |
                | orderNum | number | 정렬 순서 | 1 |
                | status | string | 상태 | "ACTIVE" |
                
                ---
                
                ### Response > content > products > items > catalogImage
                
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
                
                ### Response > content > products > items > selectedOptions
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 옵션 ID | 1 |
                | content | string | 옵션 내용 | "1박스" |
                | status | string | 상태 | "ACTIVE" |
                
                """,
        security = {@SecurityRequirement(name = "bearer")},
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
                        name = "page",
                        in = ParameterIn.QUERY,
                        description = "페이지 번호 (0-based, 기본값: 0)",
                        schema = @Schema(
                                type = "number",
                                example = "0",
                                defaultValue = "0"
                        )
                ),
                @Parameter(
                        name = "page-size",
                        in = ParameterIn.QUERY,
                        description = "페이지 크기",
                        schema = @Schema(
                                type = "number",
                                example = "10",
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
                                allowableValues = {"ORDER_NUM", "POPULARITY", "DISCOUNT_PRICE", "ACCUMULATED_POINT"},
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
                        description = "관리자 상품 목록 조회 성공",
                        content = @Content(
                                schema = @Schema(implementation = StringResponseSchema.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-03T12:12:30.013",
                                          "content": {
                                            "products": {
                                              "page": 0,
                                              "pageSize": 10,
                                              "totalElements": 16,
                                              "totalPages": 2,
                                              "items": [
                                                {
                                                  "id": 1,
                                                  "sellerId": 1,
                                                  "name": "스프링노트1",
                                                  "brandName": "노트왕",
                                                  "pricePolicy": {
                                                    "id": 28,
                                                    "price": 45000,
                                                    "discountPrice": 37000,
                                                    "discountRate": 3.2,
                                                    "accumulatedPoint": 1200,
                                                    "optionIds": [8, 11]
                                                  },
                                                  "sales": 0,
                                                  "productTags": [
                                                    {
                                                      "id": 25,
                                                      "productId": 1,
                                                      "name": "루테인",
                                                      "orderNum": null,
                                                      "status": "ACTIVE"
                                                    }
                                                  ],
                                                  "catalogImage": null,
                                                  "selectedOptions": null,
                                                  "stock": 99990,
                                                  "averageRating": 4.5,
                                                  "totalCount": 8,
                                                  "orderNum": 1,
                                                  "status": "ACTIVE"
                                                }
                                              ]
                                            }
                                          },
                                          "message": "관리자 상품 목록 조회 성공"
                                        }
                                        """)
                        )
                )
        })
public @interface GetAdminProductsApiDocs {
}
