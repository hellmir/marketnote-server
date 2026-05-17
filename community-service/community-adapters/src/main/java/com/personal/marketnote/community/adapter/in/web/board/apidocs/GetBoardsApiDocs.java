package com.personal.marketnote.community.adapter.in.web.board.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "(비회원) 게시판 목록 조회",
        description = """
                작성일자: 2026-01-15
                
                작성자: 성효빈
                
                - 게시판 목록을 조회합니다.
                
                - **"NOTICE": 공지**
                
                - **"FAQ": FAQ**
                
                - **"PRODUCT_INQUERY": 상품 문의**
                
                - **"ONE_ON_ONE_INQUERY": 1:1 문의**
                
                ---
                
                ## Request
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-13T16:32:18.828188" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "게시판 목록 조회 성공" |
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | boards | array | 게시판 목록 | [ ... ] |
                
                ---
                
                ### Response > content > boards
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | name | string | 게시판명 | "NOTICE" |
                | description | string | 게시판 설명 | "공지" |
                """,
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "게시판 목록 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-15T11:30:12.790346",
                                          "content": {
                                            "categories": [
                                              {
                                                "name": "NOTICE",
                                                "description": "공지"
                                              },
                                              {
                                                "name": "FAQ",
                                                "description": "FAQ"
                                              },
                                              {
                                                "name": "PRODUCT_INQUERY",
                                                "description": "상품 문의"
                                              },
                                              {
                                                "name": "ONE_ON_ONE_INQUERY",
                                                "description": "1:1 문의"
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
public @interface GetBoardsApiDocs {
}
