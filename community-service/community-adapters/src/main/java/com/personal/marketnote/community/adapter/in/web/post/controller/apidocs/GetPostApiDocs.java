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
        summary = "게시글 상세 정보 조회",
        description = """
                작성일자: 2026-01-27
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 게시글 상세 정보를 조회합니다.
                
                - 게시판
                
                    - **비회원**
                
                        - **"NOTICE": 공지**
                
                        - **"FAQ": FAQ**
                
                        - **"PRODUCT_INQUERY": 상품 상세 정보 페이지의 상품 문의**
                
                            - targetType: PRICE_POLICY
                
                            - targetId: pricePolicyId
                
                    - **회원(인증 필요)**
                
                        - **"PRODUCT_INQUERY": 나의 상품 문의**
                
                            - targetType: 미전송
                
                            - targetId: 미전송
                
                        - **"ONE_ON_ONE_INQUERY": 1:1 문의**
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | id(path) | number | 게시글 ID | Y | 10 |
                | board | string | 게시판 | Y | "PRODUCT_INQUERY" |
                | targetType | string | 대상 유형 | N | "PRICE_POLICY" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-27T10:05:12.123456" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "게시글 상세 정보 조회 성공" |
                
                ---
                
                ### Response > content
                
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
                | createdAt | string(datetime) | 생성 일시 | "2026-01-27T09:30:00.000000" |
                | modifiedAt | string(datetime) | 수정 일시 | "2026-01-27T09:30:00.000000" |
                | product | object | 상품 정보(상품 문의글이 아닌 경우 null) | { ... } |
                | replies | array | 답글 목록(재귀) | [ ... ] |
                
                ### Response > content > replies
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 답글 ID | 10 |
                | userId | number | 작성자 ID | 5 |
                | parentId | number | 부모 게시글 ID | null |
                | board | string | 게시판 | "ONE_ON_ONE_INQUERY" |
                | category | string | 카테고리 | "DELIVERY" |
                | targetType | string | 대상 유형 | null |
                | targetId | number | 대상 ID | null |
                | productImageUrl | string | 상품 이미지 URL | "https://example.com/image.jpg" |
                | maskedWriterName | string | 마스킹된 작성자명 | "홍*동" |
                | title | string | 제목 | "게시글 제목" |
                | content | string | 내용 | "게시글 내용" |
                | isPrivate | boolean | 비밀글 여부 | false |
                | isPhoto | boolean | 이미지 첨부 여부 | false |
                | images | array | 답글 이미지 목록 | [ ... ] |
                | isMasked | boolean | 비밀글 숨김 처리 여부 | false |
                | isAnswered | boolean | 답변 여부 | false |
                | createdAt | string(datetime) | 생성 일시 | "2026-01-29T15:19:58.335327" |
                | modifiedAt | string(datetime) | 수정 일시 | "2026-01-29T15:19:58.348655" |
                | product | object | 상품 정보(상품 문의글이 아닌 경우 null) | { ... } |
                | replies | array | 답글 목록(재귀) | [ ... ] |
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
                        name = "targetType",
                        in = ParameterIn.QUERY,
                        description = "대상 유형",
                        schema = @Schema(type = "string", example = "PRICE_POLICY")
                ),
                @Parameter(
                        name = "id",
                        in = ParameterIn.PATH,
                        required = true,
                        description = "게시글 ID",
                        schema = @Schema(type = "number", example = "10")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "게시글 상세 정보 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-27T10:05:12.123456",
                                          "content": {
                                            "id": 10,
                                            "userId": 5,
                                            "parentId": null,
                                            "board": "PRODUCT_INQUERY",
                                            "category": "PRODUCT_QUESTION",
                                            "targetType": "PRICE_POLICY",
                                            "targetId": 120,
                                            "maskedWriterName": "홍*동",
                                            "title": "배송 문의",
                                            "content": "배송 언제 오나요?",
                                            "isPrivate": true,
                                            "isPhoto": false,
                                            "images": [],
                                            "isMasked": false,
                                            "isAnswered": true,
                                            "createdAt": "2026-01-27T09:30:00.000000",
                                            "modifiedAt": "2026-01-27T09:30:00.000000",
                                            "product": null,
                                            "replies": [
                                              {
                                                "id": 108,
                                                "userId": 14,
                                                "parentId": 94,
                                                "board": "ONE_ON_ONE_INQUERY",
                                                "category": "DELIVERY",
                                                "targetType": null,
                                                "targetId": null,
                                                "productImageUrl": "https://example.com/image.jpg",
                                                "maskedWriterName": "홍*동",
                                                "title": "게시글 제목",
                                                "content": "게시글 내용",
                                                "isPrivate": false,
                                                "isPhoto": false,
                                                "images": null,
                                                "isMasked": false,
                                                "isAnswered": false,
                                                "createdAt": "2026-01-29T15:19:58.335327",
                                                "modifiedAt": "2026-01-29T15:19:58.348655",
                                                "product": null,
                                                "replies": []
                                              }
                                            ]
                                          },
                                          "message": "게시글 상세 정보 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetPostApiDocs {
}
