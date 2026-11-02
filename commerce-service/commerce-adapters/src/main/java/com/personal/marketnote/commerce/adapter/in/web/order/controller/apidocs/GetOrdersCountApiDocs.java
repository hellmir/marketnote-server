package com.personal.marketnote.commerce.adapter.in.web.order.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "나의 주문 내역 개수 조회",
        description = """
                작성일자: 2026-01-19
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                회원의 주문 내역 개수를 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | period | string | 조회 기간(ONE_MONTH, THREE_MONTHS, SIX_MONTHS, ONE_YEAR, ALL) | N | ONE_YEAR |
                | status | string | 주문 상태 필터(SHIPPING, DELIVERED, CONFIRMED, CANCEL_RETURN, ALL) | N | SHIPPING |
                | productName | string | 상품명 검색 키워드 | N | "공책" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-15T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "나의 주문 내역 개수 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | totalCount | number | 조회된 총 주문 개수 | 12 |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "나의 주문 내역 개수 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-15T12:12:30.013",
                                          "content": {
                                            "totalCount": 12
                                          },
                                          "message": "나의 주문 내역 개수 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetOrdersCountApiDocs {
}
