package com.personal.marketnote.user.adapter.in.web.user.controller.apidocs;

import com.personal.marketnote.user.adapter.in.web.user.request.SignUpRequest;
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
        summary = "회원 로그인",
        description = """
                작성일자: 2025-12-27
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - OAuth2 콜백 URI를 통해 발급된 Access Token 또는 회원 이메일 주소/비밀번호를 전송해 로그인합니다.
                
                - Access Token을 전송하는 경우 이메일 주소/비밀번호는 무시됩니다. (우선순위: Access Token > 이메일 주소/비밀번호)
                
                - 하나 이상의 필수 약관을 동의하지 않은 경우 isRequiredTermsAgreed: false를 반환합니다.
                
                - 비활성화된 계정인 경우 ERR02(403 FORBIDDEN)를 반환합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | email | string | 이메일 주소 | N | "example@example.com" |
                | password | string | 비밀번호 | N | "Password123!" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "ERR01" / "ERR02" / "NOT_FOUND" |
                | timestamp | string(datetime) | 응답 일시 | "2025-12-26T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "회원 로그인 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | accessToken | string | 신규 발급된 Access Token | "f8310f8asohvh80scvh0zio3hr31d" |
                | isRequiredTermsAgreed | boolean | 필수 약관 동의 여부 | true /false |
                
                ---
                
                ### Response > headers
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                refreshToken(cookie) | string | Refresh Token; HTTP-only | "<jwt-refresh-token>" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = SignUpRequest.class),
                        examples = @ExampleObject("""
                                {
                                    "email": "example@example.com",
                                    "password": "Password123!"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "회원 로그인 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2025-12-26T22:52:31.889943",
                                          "content": {
                                            "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJBQ0NFU1NfVE9LRU4iLCJpYXQiOjE3NjE1MjgzMTYsImV4cCI6MTc2MTUzMDExNiwic3ViIjoiOCIsInJvbGVJZHMiOlsiUk9MRV9CVVlFUiJdLCJ1c2VySWQiOjgsImF1dGhWZW5kb3IiOiJOQVRJVkUifQ.3nhlFNz9NBfcJKIteTICcUyN7F1w068CJKu5uy5kB0I",
                                            "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJSRUZSRVNIX1RPS0VOIiwiaWF0IjoxNzYxNDg2NzUxLCJleHAiOjE3NjI2OTYzNTEsInN1YiI6Im51bGwiLCJyb2xlSWRzIjpbIlJPTEVfQlVZRVIiXSwiYXV0aFZlbmRvciI6Ik5BVElWRSJ9._YvI9YT4aklPzJdN5D4IRqx0uzsyz4wjBMgCLGcf_CA",
                                            "isRequiredTermsAgreed": true
                                          },
                                          "message": "회원 로그인 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "로그인 요청 정보 없음",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 400,
                                          "code": "BAD_REQUEST",
                                          "timestamp": "2025-12-27T15:36:06.027533",
                                          "content": null,
                                          "message": "이메일 주소는 필수값입니다."
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
                                          "timestamp": "2025-12-26T09:53:02.089234",
                                          "content": null,
                                          "message": "존재하지 않는 회원입니다. 전송된 회원 이메일 주소: example@example.com"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "로그인 실패",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 401,
                                          "code": "ERR01",
                                          "timestamp": "2025-12-28T16:22:53.799201",
                                          "content": null,
                                          "message": "아이디(이메일) 혹은 비밀번호가 올바르지 않습니다. 입력한 내용을 다시 확인해주세요."
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
                                          "timestamp": "2025-12-28T16:13:25.045291",
                                          "content": null,
                                          "message": "비활성화된 계정입니다. 전송된 이메일 주소: example@example.com"
                                        }
                                        """)
                        )
                )
        }
)
public @interface SignInApiDocs {
}
