package com.personal.marketnote.user.adapter.in.web.user.controller.apidocs;

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
        summary = "(관리자) 회원 상태 변경 내역 목록 조회",
        description = """
                작성일자: 2026-09-20

                작성자: 성효빈

                ---

                ## Description

                - 특정 회원의 비활성화/활성화 상태 변경 내역을 조회합니다.

                - 관리자만 가능합니다.

                - 페이징과 정렬을 지원합니다.

                ---

                ## Request

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | pageSize | number | 페이지 크기 (기본값 20) | 20 |
                | pageNumber | number | 페이지 번호 (1부터 시작, 기본값 1) | 1 |
                | sortDirection | string | 정렬 방향 (ASC/DESC, 기본값 DESC) | "DESC" |
                | sortProperty | string | 정렬 속성 (기본값 ID) | "ID" |

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 401: 인증 실패 / 403: 인가 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "UNAUTHORIZED" / "FORBIDDEN" |
                | timestamp | string(datetime) | 응답 일시 | "2026-09-20T10:19:52.558748" |
                | content.pageSize | number | 페이지 크기 | 20 |
                | content.pageNumber | number | 현재 페이지 번호 | 1 |
                | content.totalCount | number | 전체 항목 수 | 3 |
                | content.hasPrevious | boolean | 이전 페이지 존재 여부 | false |
                | content.hasNext | boolean | 다음 페이지 존재 여부 | false |
                | content.histories | array | 상태 변경 이력 목록 | [...] |
                | content.histories[].id | number | 이력 ID | 101 |
                | content.histories[].userId | number | 회원 ID | 1 |
                | content.histories[].statusAction | string | 상태 변경 액션 | "DEACTIVATE" |
                | content.histories[].reason | string | 변경 사유 | "욕설 사용으로 인한 계정 정지" |
                | content.histories[].deactivatedUntil | string(datetime) | 비활성화 종료 일시 (null이면 영구) | "2026-05-13T00:00:00" |
                | content.histories[].createdBy | number | 작업한 관리자 ID | 99 |
                | content.histories[].createdAt | string(datetime) | 작업 일시 | "2026-09-20T09:10:00" |
                | message | string | 처리 결과 | "회원 상태 변경 내역 조회 성공" |
                """,
        security = {@SecurityRequirement(name = "bearer"), @SecurityRequirement(name = "admin")},
        parameters = {
                @Parameter(
                        name = "userId",
                        in = ParameterIn.PATH,
                        required = true,
                        description = "회원 ID",
                        schema = @Schema(type = "number", example = "1")
                ),
                @Parameter(
                        name = "pageSize",
                        in = ParameterIn.QUERY,
                        required = false,
                        description = "페이지 크기",
                        schema = @Schema(type = "number", example = "20", defaultValue = "20")
                ),
                @Parameter(
                        name = "pageNumber",
                        in = ParameterIn.QUERY,
                        required = false,
                        description = "페이지 번호 (1부터 시작)",
                        schema = @Schema(type = "number", example = "1", defaultValue = "1")
                ),
                @Parameter(
                        name = "sortDirection",
                        in = ParameterIn.QUERY,
                        required = false,
                        description = "정렬 방향",
                        schema = @Schema(type = "string", example = "DESC", defaultValue = "DESC", allowableValues = {"ASC", "DESC"})
                ),
                @Parameter(
                        name = "sortProperty",
                        in = ParameterIn.QUERY,
                        required = false,
                        description = "정렬 속성",
                        schema = @Schema(type = "string", example = "ID", defaultValue = "ID", allowableValues = {"ID"})
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "회원 상태 변경 내역 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-09-20T10:19:52.558748",
                                          "content": {
                                            "pageSize": 20,
                                            "pageNumber": 1,
                                            "totalCount": 2,
                                            "hasPrevious": false,
                                            "hasNext": false,
                                            "histories": [
                                              {
                                                "id": 102,
                                                "userId": 1,
                                                "statusAction": "ACTIVATE",
                                                "reason": "정지 기간 만료로 활성화",
                                                "deactivatedUntil": null,
                                                "createdBy": 99,
                                                "createdAt": "2026-04-14T09:00:00"
                                              },
                                              {
                                                "id": 101,
                                                "userId": 1,
                                                "statusAction": "DEACTIVATE",
                                                "reason": "욕설 사용으로 인한 계정 정지",
                                                "deactivatedUntil": "2026-05-13T00:00:00",
                                                "createdBy": 99,
                                                "createdAt": "2026-09-20T09:10:00"
                                              }
                                            ]
                                          },
                                          "message": "회원 상태 변경 내역 조회 성공"
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
                                          "timestamp": "2026-09-20T10:19:52.558748",
                                          "content": null,
                                          "message": "Invalid token"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "403",
                        description = "토큰 인가 실패(관리자가 아님)",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 403,
                                          "code": "FORBIDDEN",
                                          "timestamp": "2026-09-20T10:19:52.558748",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetUserStatusHistoriesApiDocs {
}
