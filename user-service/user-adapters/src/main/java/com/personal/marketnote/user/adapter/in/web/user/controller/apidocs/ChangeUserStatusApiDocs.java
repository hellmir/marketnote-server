package com.personal.marketnote.user.adapter.in.web.user.controller.apidocs;

import com.personal.marketnote.user.adapter.in.web.user.request.ChangeUserStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
        summary = "(관리자) 회원 상태 변경 (비활성화/활성화)",
        description = """
                작성일자: 2026-09-20

                작성자: 성효빈

                ---

                ## Description

                - 특정 회원의 상태를 비활성화(DEACTIVATE) 또는 활성화(ACTIVATE)합니다.

                - 관리자만 가능합니다.

                - 비활성화 시 deactivatedUntil을 지정하면 기간 비활성화, null이면 영구 비활성화입니다.

                - 활성화 시 deactivatedUntil은 null로 초기화됩니다.

                - 변경 이력은 별도 테이블(user_status_histories)에 저장됩니다.

                - 사유는 필수이며 최대 500자까지 입력 가능합니다.

                ---

                ## Request

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | action | string | 상태 변경 액션 (DEACTIVATE / ACTIVATE) | "DEACTIVATE" |
                | reason | string | 상태 변경 사유 | "욕설 사용으로 인한 계정 정지" |
                | deactivatedUntil | string(datetime) | 비활성화 종료 일시 (null이면 영구) | "2026-05-13T00:00:00" |

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" |
                | timestamp | string(datetime) | 응답 일시 | "2026-09-20T10:19:52.558748" |
                | content.userId | number | 회원 ID | 1 |
                | content.status | string | 변경 후 상태 | "INACTIVE" |
                | content.deactivatedUntil | string(datetime) | 비활성화 종료 일시 | "2026-05-13T00:00:00" |
                | content.historyId | number | 생성된 상태 변경 이력 ID | 101 |
                | message | string | 처리 결과 | "회원 상태 변경 성공" |
                """,
        security = {@SecurityRequirement(name = "bearer"), @SecurityRequirement(name = "admin")},
        parameters = {
                @Parameter(
                        name = "userId",
                        in = ParameterIn.PATH,
                        required = true,
                        description = "회원 ID",
                        schema = @Schema(type = "number", example = "1")
                )
        },
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = ChangeUserStatusRequest.class),
                        examples = {
                                @ExampleObject(
                                        name = "기간 비활성화",
                                        value = """
                                                {
                                                    "action": "DEACTIVATE",
                                                    "reason": "욕설 사용으로 인한 계정 정지",
                                                    "deactivatedUntil": "2026-05-13T00:00:00"
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "영구 비활성화",
                                        value = """
                                                {
                                                    "action": "DEACTIVATE",
                                                    "reason": "반복 위반으로 영구 정지",
                                                    "deactivatedUntil": null
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "활성화",
                                        value = """
                                                {
                                                    "action": "ACTIVATE",
                                                    "reason": "정지 기간 만료로 활성화"
                                                }
                                                """
                                )
                        }
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "회원 상태 변경 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-09-20T10:19:52.558748",
                                          "content": {
                                            "userId": 1,
                                            "status": "INACTIVE",
                                            "deactivatedUntil": "2026-05-13T00:00:00",
                                            "historyId": 101
                                          },
                                          "message": "회원 상태 변경 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "요청 파라미터 오류",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 400,
                                          "code": "BAD_REQUEST",
                                          "timestamp": "2026-09-20T10:19:52.558748",
                                          "content": null,
                                          "message": "상태 변경 액션은 필수입니다."
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
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "존재하지 않는 회원",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 404,
                                          "code": "NOT_FOUND",
                                          "timestamp": "2026-09-20T10:19:52.558748",
                                          "content": null,
                                          "message": "존재하지 않는 회원입니다. 전송된 회원 ID: 1"
                                        }
                                        """)
                        )
                )
        }
)
public @interface ChangeUserStatusApiDocs {
}
