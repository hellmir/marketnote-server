package com.personal.marketnote.community.adapter.in.web.post.controller.apidocs;

import com.personal.marketnote.community.adapter.in.web.post.request.RegisterPostRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "게시글 등록",
        description = """
                작성일자: 2026-01-13
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 게시글을 등록합니다.
                
                - 상품 문의글인 경우, 해당 상품의 이미지 URL을 함께 전송해야 합니다.
                
                - 게시판 및 카테고리 목록
                
                    - **"NOTICE": 공지**
                
                        - "ANNOUNCEMENT": 공지
                
                        - "EVENT": 이벤트
                
                    - **"FAQ": FAQ**
                
                        - "ORDER_PAYMENT": 주문/결제
                
                        - "DELIVERY": 배송 관련
                
                        - "CANCEL_REFUND": 취소/환불
                
                        - "RETURN_EXCHANGE": 반품/교환
                
                        - "POINT": 적립금(포인트)
                
                        - "EVENT_COUPON": 이벤트/쿠폰
                
                        - "LOGIN_USER_INFO": 로그인/회원정보
                
                    - **"PRODUCT_INQUERY": 상품 문의**
                
                        - "PRODUCT_QUESTION": 상품 문의
                
                        - "RESTOCK": 재입고 문의
                
                        - "SHIPPING": 배송 문의
                
                    - **"ONE_ON_ONE_INQUERY": 1:1 문의**
                
                        - "ORDER_PAYMENT": 주문/결제
                
                        - "DELIVERY": 배송 관련
                
                        - "CANCEL_REFUND": 취소/환불
                
                        - "RETURN_EXCHANGE": 반품/교환
                
                        - "POINT": 적립금(포인트)
                
                        - "EVENT_COUPON": 이벤트/쿠폰
                
                - 대상 그룹 유형 목록
                
                    - "PRODUCT": 상품(상품 문의 시에만 전송)
                
                - 대상 유형 목록
                
                    - "PRICE_POLICY": 상품 가격 정책(상품 문의 시에만 전송)
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | parentId | number | 부모 게시글 ID (답변 시 사용) | N | 1 |
                | board | string | 대상 게시판 | Y | "NOTICE" |
                | category | string | 게시글 카테고리 | Y | "ANNOUNCEMENT" |
                | targetGroupType | string | 대상 유형 | N | "PRODUCT" |
                | targetGroupId | number | 대상 ID | N | 1 |
                | targetType | string | 대상 유형 | N | "PRICE_POLICY" |
                | targetId | number | 대상 ID | N | 1 |
                | productImageUrl | string | 상품 이미지 URL | N | "https://example.com/image.jpg" |
                | writerName | string | 작성자 이름 | Y | "홍길동" |
                | title | string | 제목 | N | "게시글 제목" |
                | content | string | 내용 | Y | "게시글 내용" |
                | isPrivate | boolean | 비밀글 여부 | Y | default: false |
                | isPhoto | boolean | 이미지 첨부 여부 | Y | default: false |
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 201 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-09T16:32:18.828188" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "게시글 등록 성공" |
                
                ### Response > content

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 생성된 게시글 ID | 3 |
                | postKey | string(uuid) | 게시글 postKey (파일 업로드 ownerKey) | "01890d0a-1234-7000-89ab-0123456789ab" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = RegisterPostRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "board": "PRODUCT_INQUERY",
                                  "category": "PRODUCT_QUESTION",
                                  "targetGroupType": "PRODUCT",
                                  "targetGroupId": 1,
                                  "targetType": "PRICE_POLICY",
                                  "targetId": 1,
                                  "productImageUrl": "https://example.com/image.jpg",
                                  "writerName": "홍길동",
                                  "title": "게시글 제목",
                                  "content": "게시글 내용",
                                  "isPrivate": false,
                                  "isPhoto": false
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "게시글 등록 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 201,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-13T16:39:31.057206",
                                          "content": {
                                            "id": 3,
                                            "postKey": "01890d0a-1234-7000-89ab-0123456789ab"
                                          },
                                          "message": "게시글 등록 성공"
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
                                          "timestamp": "2026-01-09T16:32:18.828188",
                                          "content": null,
                                          "message": "Invalid token"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "403",
                        description = "관리자 권한 인가 실패 / 관리자 또는 판매자 권한 인가 실패",
                        content = @Content(
                                examples = {
                                        @ExampleObject(
                                                name = "관리자 권한 인가 실패",
                                                summary = "관리자 권한 없음",
                                                value = """
                                                        {
                                                          "statusCode": 403,
                                                          "code": "FORBIDDEN",
                                                          "timestamp": "2026-01-13T17:11:31.590392",
                                                          "content": null,
                                                          "message": "관리자만 작성할 수 있습니다."
                                                        }
                                                        """
                                        ),
                                        @ExampleObject(
                                                name = "관리자 또는 판매자 권한 인가 실패",
                                                summary = "관리자 또는 판매자 권한 없음",
                                                value = """
                                                        {
                                                          "statusCode": 403,
                                                          "code": "FORBIDDEN",
                                                          "timestamp": "2026-01-13T17:11:31.590392",
                                                          "content": null,
                                                          "message": "관리자 또는 판매자만 작성할 수 있습니다."
                                                        }
                                                        """
                                        ),
                                        @ExampleObject(
                                                name = "상품 판매자가 아님",
                                                summary = "본인의 상품이 아닌 상품에 답글 등록 시도",
                                                value = """
                                                        {
                                                          "statusCode": 403,
                                                          "code": "FORBIDDEN",
                                                          "timestamp": "2026-01-13T17:11:31.590392",
                                                          "content": null,
                                                          "message": "상품 판매자가 아닙니다. 전송된 가격 정책 ID: 180"
                                                        }
                                                        """
                                        )
                                }
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "게시판에 맞지 않는 카테고리명 전송",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 400,
                                          "code": "BAD_REQUEST",
                                          "timestamp": "2026-01-13T16:43:07.990429",
                                          "content": null,
                                          "message": "게시판에 맞지 않는 카테고리입니다. 카테고리명: PRODUCT_QUESTION"
                                        }
                                        """)
                        )
                )
        }
)
public @interface RegisterPostApiDocs {
}
