package com.personal.marketnote.reward.adapter.in.web.attendance.apidocs;

import com.personal.marketnote.reward.adapter.in.web.attendance.response.GetAttendanceRelayStatusResponse;
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
        summary = "출석 릴레이 현황 조회",
        description = """
                작성일자: 2026-09-03

                작성자: 성효빈

                ---

                ## Description

                현재 릴레이 사이클의 진행 상태를 조회합니다.
                4일 주기 릴레이에서 각 일차의 보상 정보와 완료 여부, 오늘 출석 여부를 반환합니다.

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 시간 | "2026-09-03T12:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "출석 릴레이 현황 조회 성공" |

                ---

                ### Response > content

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | currentRelayDay | number | 현재 릴레이 일차 (1-4) | 2 |
                | todayChecked | boolean | 오늘 출석 여부 | true |
                | relaySlots | array(object) | 릴레이 슬롯 목록 (4개) | [...] |

                ---

                ### Response > content > relaySlots[]

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | day | number | 릴레이 일차 (1-4) | 1 |
                | rewardType | string | 보상 유형 | "POINT" |
                | rewardQuantity | number | 보상 수량 | 50 |
                | completed | boolean | 완료 여부 | true |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "출석 릴레이 현황 조회 성공",
                        content = @Content(
                                schema = @Schema(implementation = GetAttendanceRelayStatusResponse.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-09-03T12:00:00.000",
                                          "content": {
                                            "currentRelayDay": 2,
                                            "todayChecked": true,
                                            "relaySlots": [
                                              {
                                                "day": 1,
                                                "rewardType": "POINT",
                                                "rewardQuantity": 50,
                                                "completed": true
                                              },
                                              {
                                                "day": 2,
                                                "rewardType": "POINT",
                                                "rewardQuantity": 100,
                                                "completed": true
                                              },
                                              {
                                                "day": 3,
                                                "rewardType": "POINT",
                                                "rewardQuantity": 150,
                                                "completed": false
                                              },
                                              {
                                                "day": 4,
                                                "rewardType": "BOOSTER",
                                                "rewardQuantity": 1,
                                                "completed": false
                                              }
                                            ]
                                          },
                                          "message": "출석 릴레이 현황 조회 성공"
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
                                          "timestamp": "2026-09-03T12:00:00.000",
                                          "content": null,
                                          "message": "Invalid token"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetAttendanceRelayStatusApiDocs {
}
