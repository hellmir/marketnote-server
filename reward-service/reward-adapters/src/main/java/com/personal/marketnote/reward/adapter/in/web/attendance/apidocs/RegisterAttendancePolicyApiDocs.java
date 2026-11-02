package com.personal.marketnote.reward.adapter.in.web.attendance.apidocs;

import com.personal.marketnote.reward.adapter.in.web.attendance.request.RegisterAttendancePolicyRequest;
import com.personal.marketnote.reward.adapter.in.web.attendance.response.RegisterAttendancePolicyResponse;
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
        summary = "(관리자) 출석 정책 등록",
        description = """
                작성일자: 2026-01-21
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 출석 정책을 등록합니다.
                
                    - 대상 리워드 유형
                
                        - "POINT": 포인트
                
                        - "BOOSTER": 영양제 제조 속도 향상 부스터
                
                ---
                
                ## Request Body
                
                | 키 | 타입 | 설명 | 필수 | 예시 |
                | --- | --- | --- | --- | --- |
                | continuousPeriod | number | 대상 연속 출석 일수 | Y | 3 |
                | rewardType | string(enum) | 리워드 유형 | Y | "BOOSTER" |
                | rewardQuantity | number | 리워드 수량 | Y | 100 |
                | attendenceDate | string(date) | 대상 출석일자(크리스마스 등 특정 기념일 설정용) | N | "2026-01-22" |
                
                ---
                
                ## Response
                
                | 키 | 타입 | 설명 | 예시 |
                | --- | --- | --- | --- |
                | id | number | 생성된 정책 ID | 10001 |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = RegisterAttendancePolicyRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "continuousPeriod": 3,
                                  "rewardType": "POINT",
                                  "rewardQuantity": 100,
                                  "attendenceDate": "2026-01-22"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "출석 정책 등록 성공",
                        content = @Content(
                                schema = @Schema(implementation = RegisterAttendancePolicyResponse.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 201,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-21T12:35:00.000",
                                          "content": {
                                            "id": 10001
                                          },
                                          "message": "출석 정책 등록 성공"
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
                                          "code": "FORBIDDEN",
                                          "timestamp": "2026-01-21T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        }
)
public @interface RegisterAttendancePolicyApiDocs {
}

