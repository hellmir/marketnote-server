package com.personal.marketnote.reward.adapter.in.web.attendance.apidocs;

import com.personal.marketnote.reward.adapter.in.web.attendance.response.GetMonthlyAttendanceResponse;
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
        summary = "나의 월별 출석 내역 조회",
        description = """
                작성일자: 2026-01-21
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                월별 출석 내역을 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | month | number | 조회 월 (1-12) | N | 이번 달 |
                | year | number | 조회 연도 | N | default: 이번 년도 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 시간 | "2026-01-21T12:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "월별 출석 내역 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | attendanceDates | array(string(date)) | 출석일자 목록 | ["2026-01-26", "2026-01-27"] |
                | totalAttendanceDays | number | 월별 총 출석일 | 3 |
                | totalRewardQuantity | number | 누적 보상 개수 | 2000 |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "month",
                        in = ParameterIn.QUERY,
                        required = false,
                        description = "조회 월 (1-12)",
                        schema = @Schema(type = "number", example = "1")
                ),
                @Parameter(
                        name = "year",
                        in = ParameterIn.QUERY,
                        required = false,
                        description = "조회 연도",
                        schema = @Schema(type = "number", example = "2026")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "월별 출석 내역 조회 성공",
                        content = @Content(
                                schema = @Schema(implementation = GetMonthlyAttendanceResponse.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-21T17:54:49.103705",
                                          "content": {
                                            "attendanceDates": [
                                              "2026-01-18",
                                              "2026-01-20",
                                              "2026-01-21"
                                            ],
                                            "totalAttendanceDays": 3,
                                            "totalRewardQuantity": 430
                                          },
                                          "message": "월별 출석 내역 조회 성공"
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
                )
        }
)
public @interface GetMonthlyAttendanceApiDocs {
}
