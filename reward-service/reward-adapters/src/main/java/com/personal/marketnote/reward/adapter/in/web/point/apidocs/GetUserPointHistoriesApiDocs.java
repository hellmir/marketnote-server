package com.personal.marketnote.reward.adapter.in.web.point.apidocs;

import com.personal.marketnote.reward.adapter.in.web.point.response.GetUserPointHistoryResponse;
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
        summary = "(관리자/본인) 회원 포인트 내역 조회",
        description = """
                작성일자: 2026-01-19
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 회원 포인트 적립/사용 내역을 일자별로 조회합니다.
                
                - 관리자 또는 본인만 조회할 수 있습니다.
                
                - 커서 기반 페이징을 지원합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | userId (path) | number | 회원 ID | Y | 100 |
                | filter | string | 조회 필터 (ALL/ACCRUAL/DEDUCTION) | 선택 | ACCRUAL |
                | start-date | string(date) | 조회 시작일 (YYYY-MM-DD) | 선택 | 2026-03-01 |
                | end-date | string(date) | 조회 종료일 (YYYY-MM-DD) | 선택 | 2026-03-31 |
                | cursor | number | 이전 페이지 마지막 항목 ID (미입력 시 첫 페이지) | 선택 | 25 |
                | page-size | number | 페이지 크기 (기본값: 20) | 선택 | 20 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 시간 | "2026-01-19T12:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "회원 포인트 내역 조회 성공" |
                
                ---
                
                ### Response > content > histories (CursorResponse)
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | totalElements | number | 전체 건수 (첫 페이지만) | 15 |
                | hasNext | boolean | 다음 페이지 존재 여부 | true |
                | nextCursor | number | 다음 커서 (마지막 항목 ID) | 11 |
                | items | array | 일자별 포인트 내역 | [ ... ] |
                
                ### Response > content > histories > items
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | date | string(date) | 발생일자 | "2026-01-19" |
                | count | number | 개수 | 2 |
                | histories | array | 포인트 내역 | [ ... ] |
                
                ### Response > content > histories > items > histories
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 포인트 내역 ID | 1 |
                | amount | number | 적립/사용 금액 (양수=적립, 음수=사용) | 1000 |
                | isReflected | boolean | 반영 여부 | true |
                | sourceType | string | 적립/사용 출처 타입 | "USER" |
                | sourceId | number | 적립/사용 출처 ID | 1 |
                | reason | string | 사유 | "신규 회원 적립" |
                | accumulatedAt | string(datetime) | 적립/사용 일시 | "2026-01-19T11:00:00" |
                | createdAt | string(datetime) | 생성 일시 | "2026-01-19T11:00:00" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "userId",
                        in = ParameterIn.PATH,
                        required = true,
                        description = "회원 ID",
                        schema = @Schema(type = "number", example = "100")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "회원 포인트 내역 조회 성공",
                        content = @Content(
                                schema = @Schema(implementation = GetUserPointHistoryResponse.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-19T17:15:34.945228",
                                          "content": {
                                            "histories": {
                                              "totalElements": 15,
                                              "hasNext": true,
                                              "nextCursor": 11,
                                              "items": [
                                                {
                                                  "date": "2026-01-19",
                                                  "count": 1,
                                                  "histories": [
                                                    {
                                                      "id": 25,
                                                      "amount": 1000,
                                                      "isReflected": true,
                                                      "sourceType": "OFFERWALL",
                                                      "sourceId": 54,
                                                      "reason": "애드팝콘 리워드 보상 적립",
                                                      "accumulatedAt": "2026-01-19T10:01:22.883182",
                                                      "createdAt": "2026-01-19T10:01:31.375436"
                                                    }
                                                  ]
                                                },
                                                {
                                                  "date": "2026-01-18",
                                                  "count": 1,
                                                  "histories": [
                                                    {
                                                      "id": 23,
                                                      "amount": 5000,
                                                      "isReflected": true,
                                                      "sourceType": "USER",
                                                      "sourceId": 14,
                                                      "reason": "링크 공유 회원 상품 구매",
                                                      "accumulatedAt": "2026-01-18T05:39:52.307824",
                                                      "createdAt": "2026-01-18T17:28:12.709305"
                                                    }
                                                  ]
                                                },
                                                {
                                                  "date": "2026-01-17",
                                                  "count": 3,
                                                  "histories": [
                                                    {
                                                      "id": 14,
                                                      "amount": 2000,
                                                      "isReflected": true,
                                                      "sourceType": "USER",
                                                      "sourceId": 17,
                                                      "reason": "추천인 코드 등록 적립",
                                                      "accumulatedAt": "2026-01-17T14:28:00.946674",
                                                      "createdAt": "2026-01-17T14:28:00.951884"
                                                    },
                                                    {
                                                      "id": 13,
                                                      "amount": -500,
                                                      "isReflected": true,
                                                      "sourceType": "PRODUCT",
                                                      "sourceId": 40,
                                                      "reason": "상품 구매",
                                                      "accumulatedAt": "2026-01-17T14:28:00.973419",
                                                      "createdAt": "2026-01-19T17:13:26.722427"
                                                    },
                                                    {
                                                      "id": 11,
                                                      "amount": 0,
                                                      "isReflected": true,
                                                      "sourceType": "USER",
                                                      "sourceId": 17,
                                                      "reason": "회원 가입",
                                                      "accumulatedAt": "2026-01-17T05:27:21.418247",
                                                      "createdAt": "2026-01-17T05:27:21.420814"
                                                    }
                                                  ]
                                                }
                                              ]
                                            }
                                          },
                                          "message": "회원 포인트 내역 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetUserPointHistoriesApiDocs {
}
