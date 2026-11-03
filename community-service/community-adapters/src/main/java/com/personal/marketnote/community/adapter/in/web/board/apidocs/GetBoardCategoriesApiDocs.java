package com.personal.marketnote.community.adapter.in.web.board.apidocs;

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
        summary = "(비회원) 게시판 카테고리 목록 조회",
        description = """
                작성일자: 2026-01-15
                
                작성자: 성효빈
                
                - 대상 게시판의 게시판 카테고리 목록을 조회합니다.
                
                - 게시판
                
                    - **"NOTICE": 공지**
                
                    - **"FAQ": FAQ**
                
                    - **"PRODUCT_INQUERY": 상품 문의**
                
                    - **"ONE_ON_ONE_INQUERY": 1:1 문의**
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | board | string | 게시판 | Y | "FAQ" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-13T16:32:18.828188" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "게시판 카테고리 목록 조회 성공" |
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | categories | array | 게시판 카테고리 목록 | [ ... ] |
                
                ---
                
                ### Response > content > categories

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | name | string | 카테고리명 | "ORDER_DELIVERY" |
                | description | string | 카테고리 설명 | "주문, 결제, 배송 관련 문의" |
                | isDefault | boolean | default 카테고리 여부 | true |
                """,
        parameters = {
                @Parameter(
                        name = "board",
                        in = ParameterIn.QUERY,
                        required = true,
                        description = "게시판",
                        schema = @Schema(type = "string", example = "FAQ")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "게시판 카테고리 목록 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-15T11:15:34.94688",
                                          "content": {
                                            "categories": [
                                              {
                                                "name": "ORDER_DELIVERY",
                                                "description": "주문, 결제, 배송 관련 문의",
                                                "isDefault": true
                                              },
                                              {
                                                "name": "CANCEL_RETURN",
                                                "description": "취소, 반품, 환불 관련 문의",
                                                "isDefault": false
                                              },
                                              {
                                                "name": "BENEFIT_REWARD",
                                                "description": "앱 내 보상 및 적립 관련 문의",
                                                "isDefault": false
                                              },
                                              {
                                                "name": "PRODUCT",
                                                "description": "제품 및 오픈 관련 문의",
                                                "isDefault": false
                                              },
                                              {
                                                "name": "ACCOUNT",
                                                "description": "회원정보 및 계정 관련 문의",
                                                "isDefault": false
                                              }
                                            ]
                                          },
                                          "message": "게시판 카테고리 목록 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetBoardCategoriesApiDocs {
}
