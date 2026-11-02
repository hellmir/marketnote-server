package com.personal.marketnote.reward.adapter.in.web.attendance.apidocs;

import com.personal.marketnote.reward.adapter.in.web.attendance.response.GetAttendancePoliciesResponse;
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
        summary = "(관리자) 출석 정책 목록 조회",
        description = """
                작성일자: 2026-01-21
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                출석 정책 목록을 조회합니다.
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "출석 정책 목록 조회 성공",
                        content = @Content(
                                schema = @Schema(implementation = GetAttendancePoliciesResponse.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-21T16:47:48.313208",
                                          "content": {
                                            "policies": [
                                              {
                                                "id": 6,
                                                "continuousPeriod": 2,
                                                "rewardType": "BOOSTER",
                                                "rewardQuantity": 20,
                                                "attendenceDate": null,
                                                "status": "ACTIVE"
                                              },
                                              {
                                                "id": 5,
                                                "continuousPeriod": 1,
                                                "rewardType": "BOOSTER",
                                                "rewardQuantity": 10,
                                                "attendenceDate": null,
                                                "status": "ACTIVE"
                                              },
                                              {
                                                "id": 4,
                                                "continuousPeriod": 3,
                                                "rewardType": "POINT",
                                                "rewardQuantity": 100,
                                                "attendenceDate": "2026-01-22",
                                                "status": "ACTIVE"
                                              },
                                              {
                                                "id": 10000,
                                                "continuousPeriod": 1,
                                                "rewardType": "BOOSTER",
                                                "rewardQuantity": 50,
                                                "attendenceDate": null,
                                                "status": "ACTIVE"
                                              }
                                            ]
                                          },
                                          "message": "출석 정책 목록 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetAttendancePoliciesApiDocs {
}

