package com.personal.marketnote.user.adapter.in.web.user.controller.apidocs;

import com.personal.marketnote.user.adapter.in.web.user.request.UpdateUserPenaltyCountRequest;
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
        summary = "(관리자) 회원 패널티 횟수 수정",
        description = """
                작성일자: 2026-09-20

                작성자: 성효빈

                ---

                ## Description

                - 특정 회원의 패널티 횟수를 지정된 값으로 수정합니다.

                - 관리자만 가능합니다.

                - 수정 이력은 별도 테이블에 저장되어 이전 횟수와 변경 후 횟수, 사유, 작업자가 기록됩니다.

                - 패널티 횟수는 0 이상이어야 합니다.

                - 기존 횟수와 동일한 값을 요청하면 409 응답이 반환됩니다.

                ---

                ## Request

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | penaltyCount | number | 변경할 패널티 횟수 (0 이상) | 5 |
                | reason | string | 패널티 횟수 수정 사유 | "중복 부과 보정" |

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 동일한 값 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "ERR04" |
                | timestamp | string(datetime) | 응답 일시 | "2026-09-20T10:19:52.558748" |
                | content.userId | number | 회원 ID | 1 |
                | content.penaltyCount | number | 수정 후 패널티 횟수 | 5 |
                | content.historyId | number | 생성된 수정 이력 ID | 202 |
                | message | string | 처리 결과 | "회원 패널티 횟수 수정 성공" |
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
                        schema = @Schema(implementation = UpdateUserPenaltyCountRequest.class),
                        examples = @ExampleObject("""
                                {
                                    "penaltyCount": 5,
                                    "reason": "중복 부과 보정"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "회원 패널티 횟수 수정 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-09-20T10:19:52.558748",
                                          "content": {
                                            "userId": 1,
                                            "penaltyCount": 5,
                                            "historyId": 202
                                          },
                                          "message": "회원 패널티 횟수 수정 성공"
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
                                          "message": "패널티 횟수는 0 이상이어야 합니다."
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
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "동일한 값 수정 요청",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 409,
                                          "code": "ERR04",
                                          "timestamp": "2026-09-20T10:19:52.558748",
                                          "content": null,
                                          "message": "업데이트할 대상의 값과 입력한 값이 동일합니다. 전송한 값: 5"
                                        }
                                        """)
                        )
                )
        }
)
public @interface UpdateUserPenaltyCountApiDocs {
}
