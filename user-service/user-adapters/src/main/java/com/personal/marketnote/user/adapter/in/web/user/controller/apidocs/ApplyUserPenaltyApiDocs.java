package com.personal.marketnote.user.adapter.in.web.user.controller.apidocs;

import com.personal.marketnote.user.adapter.in.web.user.request.ApplyUserPenaltyRequest;
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
        summary = "(관리자) 회원 패널티 부과",
        description = """
                작성일자: 2026-09-20

                작성자: 성효빈

                ---

                ## Description

                - 특정 회원에게 패널티를 부과합니다.

                - 관리자만 가능합니다.

                - 회원의 패널티 횟수가 1 증가하고, 부과 이력이 별도 테이블에 저장됩니다.

                - 사유는 필수이며 최대 500자까지 입력 가능합니다.

                ---

                ## Request

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | reason | string | 패널티 부과 사유 | "게시글 도배 행위" |

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" |
                | timestamp | string(datetime) | 응답 일시 | "2026-09-20T10:19:52.558748" |
                | content.userId | number | 회원 ID | 1 |
                | content.penaltyCount | number | 부과 후 패널티 횟수 | 3 |
                | content.historyId | number | 생성된 패널티 이력 ID | 101 |
                | message | string | 처리 결과 | "회원 패널티 부과 성공" |
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
                        schema = @Schema(implementation = ApplyUserPenaltyRequest.class),
                        examples = @ExampleObject("""
                                {
                                    "reason": "게시글 도배 행위"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "회원 패널티 부과 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-09-20T10:19:52.558748",
                                          "content": {
                                            "userId": 1,
                                            "penaltyCount": 3,
                                            "historyId": 101
                                          },
                                          "message": "회원 패널티 부과 성공"
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
                                          "message": "패널티 사유는 필수입니다."
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
public @interface ApplyUserPenaltyApiDocs {
}
