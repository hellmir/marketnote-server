package com.personal.marketnote.reward.adapter.in.web.attendance.apidocs;

import com.personal.marketnote.reward.adapter.in.web.attendance.response.RegisterAttendanceResponse;
import io.swagger.v3.oas.annotations.Operation;
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
        summary = "회원 출석체크 등록",
        description = """
                작성일자: 2026-01-20
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 회원 출석체크를 등록합니다.
                
                - 클라이언트 출석 요청 시간 검증 후, 출석 정책을 적용해 출석 기록을 생성합니다.
                
                ---
                
                ## Request Body
                
                | 키 | 타입 | 설명 | 필수 | 예시 |
                | --- | --- | --- | --- | --- |
                | attendedAt | string(datetime) | 클라이언트 출석 요청 시간 | Y | "2026-01-20T12:34:56" |
                
                ---
                
                ## Response
                
                | 키 | 타입 | 설명 | 예시 |
                | --- | --- | --- | --- |
                | id | number | 생성된 출석 기록 ID | 12345 |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "출석체크 등록 성공",
                        content = @Content(
                                schema = @Schema(implementation = RegisterAttendanceResponse.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 201,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-20T12:35:00.000",
                                          "content": {
                                            "id": 12345
                                          },
                                          "message": "회원 출석체크 등록 성공"
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
                                          "timestamp": "2026-01-20T12:12:30.013",
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
                                          "code": "FORBIDDEN",
                                          "timestamp": "2026-01-20T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "출석일시 검증 실패",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 400,
                                          "code": "BAD_REQUEST",
                                          "timestamp": "2026-01-20T14:05:19.481073",
                                          "content": null,
                                          "message": "전송된 출석일시가 올바르지 않습니다."
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "중복 출석 요청 및 기본 출석 정책 조회 실패",
                        content = @Content(
                                examples = {
                                        @ExampleObject(
                                                name = "중복 출석 요청",
                                                summary = "중복 출석 요청",
                                                value = """
                                                        {
                                                          "statusCode": 409,
                                                          "code": "CONFLICT",
                                                          "timestamp": "2026-01-21T13:59:16.397054",
                                                          "content": null,
                                                          "message": "이미 오늘 출석이 등록되었습니다."
                                                        }
                                                        """
                                        ),
                                        @ExampleObject(
                                                name = "기본 출석 정책 조회 실패",
                                                summary = "기본 출석 정책 조회 실패",
                                                value = """
                                                        {
                                                          "statusCode": 409,
                                                          "code": "CONFLICT",
                                                          "timestamp": "2026-01-20T14:13:11.731434",
                                                          "content": null,
                                                          "message": "기본 출석 정책을 찾을 수 없습니다. 서버 담당자에게 문의 바랍니다."
                                                        }
                                                        """
                                        )
                                }
                        )
                )
        }
)
public @interface RegisterAttendanceApiDocs {
}
