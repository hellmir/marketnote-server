package com.personal.marketnote.community.adapter.in.web.post.controller.apidocs;

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
        summary = "게시글 목록 조회",
        description = """
                작성일자: 2026-01-13
                
                작성자: 성효빈
                
                - 게시글 목록을 조회합니다.
                
                - 페이로드에 cursor 값이 없는 경우(첫 페이지): 총 게시글 개수 반환 O
                
                - 페이로드에 cursor 값이 있는 경우(더 보기): 총 게시글 개수 반환 X
                
                - 게시판
                
                    - **비회원**
                
                        - **"NOTICE": 공지**
                
                        - **"FAQ": FAQ**
                
                        - **"PRODUCT_INQUERY": 상품 상세 정보 페이지의 상품 문의**
                
                            - targetGroupType: PRODUCT
                
                            - targetGroupId: productId
                
                            - targetType: PRICE_POLICY
                
                            - targetId: pricePolicyId
                
                    - **회원(인증 필요)**
                
                        - **"PRODUCT_INQUERY": 나의 상품 문의**
                
                            - targetGroupType: 미전송
                
                            - targetGroupId: 미전송
                
                            - targetType: 미전송
                
                            - targetId: 미전송
                
                        - **"ONE_ON_ONE_INQUERY": 1:1 문의**
                
                            - 필터: IS_ANSWERED (답변 완료만 보기)
                
                - 정렬 기준
                
                   - ORDER_NUM: 지정된 정렬 순서순(기본)
                
                   - ID: 최신 등록순
                
                   - IS_ANSWERED: 답변 여부
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | board | string | 게시판 | Y | "PRODUCT_INQUERY" |
                | category | string | 카테고리 | N | "PRODUCT_QUESTION" |
                | targetGroupType | string | 대상 그룹 유형 | N | "PRODUCT" |
                | targetGroupId | number | 대상 ID(PRODUCT ID) | N | 10 |
                | targetType | string | 대상 유형 | N | "PRICE_POLICY" |
                | targetId | number | 대상 ID(PRICE_POLICY ID) | N | 10 |
                | cursor | number | 커서(무한 스크롤) | N | 30 |
                | pageSize | number | 페이지 크기 | N | 10 |
                | sortDirection | string | 정렬 방향(DESC/ASC) | N | "DESC" |
                | sortProperty | string | 정렬 기준 | N | "ORDER_NUM" |
                | searchTarget | string | 검색 대상 | N | "CONTENT" |
                | searchKeyword | string | 검색 키워드 | N | "배송" |
                    | filterCategory | string | 필터 유형 | N | "IS_PUBLIC" |
                | filterValue | string | 필터 값 | N | "TRUE" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-13T16:32:18.828188" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "게시글 목록 조회 성공" |
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | totalElements | number | 총 게시글 수(첫 페이지에서만 반환) | 120 |
                | hasNext | boolean | 다음 페이지 존재 여부 | true |
                | nextCursor | number | 다음 페이지 커서 | 20 |
                | items | array | 게시글 목록 | [ ... ] |
                
                ---
                
                ### Response > content > items
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 게시글 ID | 10 |
                | userId | number | 작성자 ID | 5 |
                | parentId | number | 부모 게시글 ID | null |
                | board | string | 게시판 | "PRODUCT_INQUERY" |
                | category | string | 카테고리 | "PRODUCT_QUESTION" |
                | targetType | string | 대상 유형 | "PRICE_POLICY" |
                | targetId | number | 대상 ID | 120 |
                | maskedWriterName | string | 마스킹된 작성자명 | "홍*동" |
                | title | string | 제목 | "배송 문의" |
                | content | string | 내용 | "배송 언제 오나요?" |
                | isPrivate | boolean | 비밀글 여부 | true |
                | isPhoto | boolean | 이미지 첨부 여부 | true |
                | images | array | 게시글 이미지 목록 | [ ... ] |
                | isMasked | boolean | 비밀글 숨김 처리 여부 | true |
                | isAnswered | boolean | 답변 여부 | true |
                | createdAt | string(datetime) | 생성 일시 | "2026-01-13T13:12:40.921092" |
                | modifiedAt | string(datetime) | 수정 일시 | "2026-01-13T13:12:40.921092" |
                | product | object | 상품 정보(상품 문의글이 아닌 경우 null) | { ... } |
                | replies | array | 답글 목록(재귀) | [ ... ] |
                
                ---
                
                ### Response > content > items > images
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 이미지 ID | 1 |
                | sort | string | 이미지 정렬 순서 | "POST_IMAGE" |
                | extension | string | 이미지 확장자 | "jpg" |
                | name | string | 이미지 이름 | "게시글 이미지" |
                | storageUrl | string | 스토리지 URL | "https://marketnote.s3.amazonaws.com/post/1/1765528094927_image.png" |
                | resizedStorageUrls | array | 리사이즈 이미지 스토리지 URL 목록 | [] |
                | orderNum | number | 정렬 순서 | 1 |
                """,
        parameters = {
                @Parameter(
                        name = "board",
                        in = ParameterIn.QUERY,
                        required = true,
                        description = "게시판",
                        schema = @Schema(type = "string", example = "PRODUCT_INQUERY")
                ),
                @Parameter(
                        name = "category",
                        in = ParameterIn.QUERY,
                        description = "게시글 카테고리",
                        schema = @Schema(type = "string", example = "PRODUCT_QUESTION")
                ),
                @Parameter(
                        name = "targetGroupType",
                        in = ParameterIn.QUERY,
                        description = "대상 그룹 유형",
                        schema = @Schema(type = "string", example = "PRODUCT")
                ),
                @Parameter(
                        name = "targetGroupId",
                        in = ParameterIn.QUERY,
                        description = "대상 그룹 ID",
                        schema = @Schema(type = "number", example = "10")
                ),
                @Parameter(
                        name = "targetType",
                        in = ParameterIn.QUERY,
                        description = "대상 유형",
                        schema = @Schema(type = "string", example = "PRICE_POLICY")
                ),
                @Parameter(
                        name = "targetId",
                        in = ParameterIn.QUERY,
                        description = "대상 ID",
                        schema = @Schema(type = "number", example = "10")
                ),
                @Parameter(
                        name = "cursor",
                        in = ParameterIn.QUERY,
                        description = "커서",
                        schema = @Schema(type = "number")
                ),
                @Parameter(
                        name = "pageSize",
                        in = ParameterIn.QUERY,
                        description = "페이지 크기",
                        schema = @Schema(type = "number", example = "10")
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
                                allowableValues = {"ORDER_NUM", "ID", "IS_ANSWERED"},
                                defaultValue = "ORDER_NUM"
                        )
                ),
                @Parameter(
                        name = "searchTarget",
                        in = ParameterIn.QUERY,
                        description = "검색 대상",
                        schema = @Schema(
                                type = "string",
                                allowableValues = {"TITLE", "CONTENT"}
                        )
                ),
                @Parameter(
                        name = "searchKeyword",
                        in = ParameterIn.QUERY,
                        description = "검색 키워드",
                        schema = @Schema(type = "string")
                ),
                @Parameter(
                        name = "filterCategory",
                        in = ParameterIn.QUERY,
                        description = "필터 유형",
                        schema = @Schema(
                                type = "string",
                                allowableValues = {"IS_PUBLIC", "IS_MINE", "IS_ANSWERED"}
                        )
                ),
                @Parameter(
                        name = "filterValue",
                        in = ParameterIn.QUERY,
                        description = "필터 값",
                        schema = @Schema(
                                type = "string",
                                allowableValues = {"TRUE"}
                        )
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "게시글 목록 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-13T16:32:18.828188",
                                          "content": {
                                            "posts": {
                                              "totalElements": 0,
                                              "hasNext": false,
                                              "nextCursor": 5,
                                              "items": [
                                                {
                                                  "id": 22,
                                                  "userId": 17,
                                                  "parentId": null,
                                                  "board": "PRODUCT_INQUERY",
                                                  "category": "PRODUCT_QUESTION",
                                                  "targetType": "PRICE_POLICY",
                                                  "targetId": 180,
                                                  "productImageUrl": null,
                                                  "maskedWriterName": "홍*동",
                                                  "title": "게시글 제목",
                                                  "content": "게시글 내용",
                                                  "isPrivate": false,
                                                  "isPhoto": true,
                                                  "images": [
                                                    {
                                                      "id": 1,
                                                      "sort": "POST_IMAGE",
                                                      "extension": "jpg",
                                                      "name": "게시글 이미지",
                                                      "storageUrl": "https://marketnote.s3.amazonaws.com/post/1/1765528094927_image.png",
                                                      "resizedStorageUrls": [],
                                                      "orderNum": 1
                                                    }
                                                  ],
                                                  "isMasked": false,
                                                  "isAnswered": false,
                                                  "createdAt": "2026-01-16T04:20:11.769461",
                                                  "modifiedAt": "2026-01-16T04:20:11.777291",
                                                  "product": {
                                                    "name": "스프링노트1234",
                                                    "brandName": "노트왕",
                                                    "selectedOptions": [
                                                      {
                                                        "id": 60,
                                                        "content": "3박스",
                                                        "status": "ACTIVE"
                                                      }
                                                    ]
                                                  },
                                                  "replies": []
                                                },
                                                {
                                                  "id": 8,
                                                  "userId": 12,
                                                  "parentId": null,
                                                  "board": "PRODUCT_INQUERY",
                                                  "category": "PRODUCT_QUESTION",
                                                  "targetType": "PRICE_POLICY",
                                                  "targetId": 180,
                                                  "productImageUrl": null,
                                                  "maskedWriterName": "김*",
                                                  "title": null,
                                                  "content": null,
                                                  "isPrivate": true,
                                                  "isPhoto": false,
                                                  "images": null,
                                                  "isMasked": true,
                                                  "isAnswered": true,
                                                  "createdAt": "2026-01-13T17:16:05.34468",
                                                  "modifiedAt": "2026-01-13T17:16:05.380594",
                                                  "product": {
                                                    "name": "스프링노트1234",
                                                    "brandName": "노트왕",
                                                    "selectedOptions": [
                                                      {
                                                        "id": 60,
                                                        "content": "3박스",
                                                        "status": "ACTIVE"
                                                      }
                                                    ]
                                                  },
                                                  "replies": []
                                                },
                                                {
                                                  "id": 6,
                                                  "userId": 17,
                                                  "parentId": null,
                                                  "board": "PRODUCT_INQUERY",
                                                  "category": "PRODUCT_QUESTION",
                                                  "targetType": "PRICE_POLICY",
                                                  "targetId": 180,
                                                  "productImageUrl": null,
                                                  "maskedWriterName": "김*",
                                                  "title": null,
                                                  "content": null,
                                                  "isPrivate": true,
                                                  "isPhoto": false,
                                                  "images": null,
                                                  "isMasked": true,
                                                  "isAnswered": false,
                                                  "createdAt": "2026-01-13T16:45:26.694612",
                                                  "modifiedAt": "2026-01-13T16:45:26.726141",
                                                  "product": {
                                                    "name": "스프링노트1234",
                                                    "brandName": "노트왕",
                                                    "selectedOptions": [
                                                      {
                                                        "id": 60,
                                                        "content": "3박스",
                                                        "status": "ACTIVE"
                                                      }
                                                    ]
                                                  },
                                                  "replies": []
                                                },
                                                {
                                                  "id": 5,
                                                  "userId": 17,
                                                  "parentId": null,
                                                  "board": "PRODUCT_INQUERY",
                                                  "category": "PRODUCT_QUESTION",
                                                  "targetType": "PRICE_POLICY",
                                                  "targetId": 180,
                                                  "productImageUrl": null,
                                                  "maskedWriterName": "홍*동",
                                                  "title": "게시글 제목",
                                                  "content": "게시글 내용",
                                                  "isPrivate": false,
                                                  "isPhoto": false,
                                                  "images": null,
                                                  "isMasked": false,
                                                  "isAnswered": true,
                                                  "createdAt": "2026-01-13T16:44:28.287364",
                                                  "modifiedAt": "2026-01-16T01:42:20.116426",
                                                  "product": {
                                                    "name": "스프링노트1234",
                                                    "brandName": "노트왕",
                                                    "selectedOptions": [
                                                      {
                                                        "id": 60,
                                                        "content": "3박스",
                                                        "status": "ACTIVE"
                                                      }
                                                    ]
                                                  },
                                                  "replies": [
                                                    {
                                                      "id": 16,
                                                      "userId": 4,
                                                      "parentId": 5,
                                                      "board": "PRODUCT_INQUERY",
                                                      "category": "PRODUCT_QUESTION",
                                                      "targetType": "PRICE_POLICY",
                                                      "targetId": 180,
                                                      "productImageUrl": null,
                                                      "maskedWriterName": "홍*동",
                                                      "title": "게시글 답변 제목",
                                                      "content": "게시글 답변 내용",
                                                      "isPrivate": false,
                                                      "isPhoto": false,
                                                      "images": null,
                                                      "isMasked": false,
                                                      "isAnswered": false,
                                                      "createdAt": "2026-01-15T14:08:02.735176",
                                                      "modifiedAt": "2026-01-15T16:30:26.869231",
                                                      "product": null,
                                                      "replies": []
                                                    }
                                                  ]
                                                }
                                              ]
                                            }
                                          },
                                          "message": "게시글 목록 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetPostsApiDocs {
}
