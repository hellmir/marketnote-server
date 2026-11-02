package com.personal.marketnote.user.adapter.in.web.authentication.controller.apidocs;

import com.personal.marketnote.user.adapter.in.web.user.request.VerifyCodeRequest;
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
        summary = "이메일 인증 코드 검증",
        description = """
                작성일자: 2026-01-01
                
                작성자: 성효빈
                
                ---
                
                ## Description
                이메일 인증 코드를 검증합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | email | string | 이메일 주소(형식: example@example.com) | Y | "example@example.com" |
                | verificationCode | string | 인증 코드 | Y | "NZW32E" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 201: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "NOT_FOUND" / "CONFLICT" / "ERR01" / "ERR02" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-01T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "이메일 인증 코드 검증 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | accessToken | string | 신규 발급된 Access Token | "f8310f8asohvh80scvh0zio3hr31d" |
                | refreshToken | string | 신규 발급된 Refresh Token | "f8310f8asohvh80scvh0zio3hr31d" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = VerifyCodeRequest.class),
                        examples = @ExampleObject("""
                                {
                                    "email": "example@example.com",
                                    "verificationCode": "NZW32E"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "이메일 인증 코드 검증 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-01T12:12:30.013",
                                          "content": {
                                            "accessToken": "f8310f8asohvh80scvh0zio3hr31d",
                                            "refreshToken": "f8310f8asohvh80scvh0zio3hr31d"
                                          },
                                          "message": "이메일 인증 코드 검증 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "인증 코드 검증 실패",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 401,
                                          "code": "ERR01",
                                          "timestamp": "2026-01-01T09:23:22.091551",
                                          "content": null,
                                          "message": "이메일 인증 코드가 유효하지 않거나 만료되었습니다. 전송된 이메일 주소: example@example.com"
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
                                          "timestamp": "2026-01-01T09:23:22.091551",
                                          "content": null,
                                          "message": "존재하지 않는 회원입니다. 전송된 회원 이메일 주소: example@example.com"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "403",
                        description = "비활성화된 계정",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 403,
                                          "code": "ERR02",
                                          "timestamp": "2026-01-01T09:23:22.091551",
                                          "content": null,
                                          "message": "비활성화된 계정입니다. 전송된 이메일 주소: example@example.com"
                                        }
                                        """)
                        )
                )
        }
)
public @interface ValidateVerificationCodeApiDocs {
}
