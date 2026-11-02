package com.personal.marketnote.user.adapter.in.web.user.controller.apidocs;

import com.personal.marketnote.user.adapter.in.web.user.request.UpdateUserInfoRequest;
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
        summary = "자신의 정보 수정",
        description = """
                작성일자: 2025-12-28
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 자신의 정보를 수정합니다.
                
                - 이메일 주소, 닉네임, 전화번호, 비밀번호 중 **하나**를 전송해 수정합니다.
                
                    - 이메일 주소: example@example.com과 같은 형식이어야 합니다.
                
                    - 닉네임: 한글만 가능하며, 6글자 이하여야 합니다.
                
                    - 전화번호: dash를 포함하여 010-1234-5678과 같은 형식이어야 합니다.
                
                    - 비밀번호: 8자 이상, 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | email | string | 이메일 주소 | "example@example.com" |
                | nickname | string | 닉네임 | "고길동" |
                | phoneNumber | string | 전화번호 | "010-1234-5678" |
                | password | string | 비밀번호 | "password123" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "NOT_FOUND" / "ERR01" / "ERR02" / "ERR03" / "ERR04" / "ERR05" / "ERR06" |
                | timestamp | string(datetime) | 응답 일시 | "2025-12-29T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "자신의 정보 수정 성공" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = UpdateUserInfoRequest.class),
                        examples = @ExampleObject("""
                                {
                                    "email": "example2@example.com"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "자신의 정보 수정 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2025-12-28T10:41:37.842294",
                                          "content": null,
                                          "message": "자신의 정보 수정 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "업데이트 요청 파라미터 없음",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 400,
                                          "code": "BAD_REQUEST",
                                          "timestamp": "2025-12-28T11:54:12.809362",
                                          "content": null,
                                          "message": "업데이트할 대상과 값을 전송해야 합니다."
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
                                          "timestamp": "2025-12-27T16:22:02.196732",
                                          "content": null,
                                          "message": "Invalid token"
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
                                          "timestamp": "2025-12-29T09:53:02.089234",
                                          "content": null,
                                          "message": "존재하지 않는 회원입니다. 회원 ID: 1"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "동일한 값 업데이트 요청",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 409,
                                          "code": "ERR01",
                                          "timestamp": "2025-12-28T14:18:34.282528",
                                          "content": null,
                                          "message": "업데이트할 대상의 값과 입력한 값이 동일합니다. 전송한 값: abc"
                                        }
                                        """)
                        )
                )
        }
)
public @interface UpdateMyInfoApiDocs {
}
