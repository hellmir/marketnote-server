package com.personal.marketnote.reward.adapter.in.web.attendance.apidocs;

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
        summary = "(관리자) 출석 정책 삭제",
        description = """
                작성일자: 2026-01-21
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                출석 정책을 삭제합니다.
                
                ---
                
                ## Request
                
                | 키 | 타입 | 설명 | 필수 | 예시 |
                | --- | --- | --- | --- | --- |
                | id | number | 삭제할 출석 정책 ID | Y | 10001 |
                
                ---
                
                ## Response
                
                | 키 | 타입 | 설명 | 예시 |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-21T12:12:30.013" |
                | content | object | 응답 본문 | null |
                | message | string | 처리 결과 | "출석 정책 삭제 성공" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(name = "id", in = ParameterIn.PATH, required = true,
                        description = "삭제할 출석 정책 ID",
                        schema = @Schema(type = "number", example = "10001")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "출석 정책 삭제 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-21T17:07:33.697826",
                                          "content": null,
                                          "message": "출석 정책 삭제 성공"
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
                                          "timestamp": "2026-01-21T12:12:30.013",
                                          "content": null,
                                          "message": "Invalid token"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "403",
                        description = "토큰 인가 실패",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 403,
                                          "code": "FORBIDDEN",«
                                          "timestamp": "2026-01-21T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "대상 출석 정책 조회 실패",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 404,
                                          "code": "NOT_FOUND",
                                          "timestamp": "2026-01-21T17:25:41.108337",
                                          "content": null,
                                          "message": "출석 정책을 찾을 수 없습니다. id: 10001"
                                        }
                                        """)
                        )
                )
        }
)
public @interface DeleteAttendancePolicyApiDocs {
}

