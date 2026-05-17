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
        summary = "회원 가입",
        description = """
                작성일자: 2025-12-26
                
                작성자: 성효빈
                
                ---
                
                ## Description
                - **소셜 로그인**은 OAuth2 콜백 URI를 통해 발급된 **Access Token**과 **이메일 주소**, 회원 이메일 주소로 발송된 **인증 코드**가 필요합니다.
                
                - **일반 로그인**은 **이메일 주소**와 **비밀번호**, 회원 이메일 주소로 발송된 **인증 코드**가 필요합니다.
                
                    - 이메일 주소: example@example.com과 같은 형식이어야 합니다.
                
                    - 닉네임: 한글만 가능하며, 6글자 이하여야 합니다.
                
                    - 전화번호: dash를 포함하여 010-1234-5678과 같은 형식이어야 합니다.
                
                    ~- 비밀번호: 8자 이상, 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다.~
                
                - 이미 **다른 방법으로 가입된 회원**인 경우 **isNewUser**: **false**를 반환합니다. 이 경우 계정이 새로 생성되지 않고, 기존 계정에 통합됩니다.
                
                - 비활성화된 계정인 경우 ERR06(403 FORBIDDEN)을 반환합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | email | string | 이메일 주소(형식: example@example.com) | Y | "example@example.com" |
                | password | string | 비밀번호(8자 이상, 대문자, 소문자, 숫자, 특수문자 포함) | N | "Password123!" |
                | verificationCode | string | 인증 코드 | Y | "NZW32E" |
                | nickname | string | 닉네임(2~20자, 한글) | N | "고길동" |
                | fullName | string | 성명(2~10자, 한글) | N | "홍길동" |
                | phoneNumber | string | 전화번호(형식: 010-1234-5678) | N | "010-1234-5678" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 201: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "ERR01" / "ERR02" / "ERR03" / "ERR04" / "ERR05" / "ERR06" |
                | timestamp | string(datetime) | 응답 일시 | "2025-12-26T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "회원 가입 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | accessToken | string | 신규 발급된 Access Token | "f8310f8asohvh80scvh0zio3hr31d" |
                | isNewUser | boolean | 신규 회원 여부 | true |
                
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
                                    "password": "Password123!",
                                    "verificationCode": "NZW32E"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "회원 가입 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 201,
                                          "code": "SUC01",
                                          "timestamp": "2025-12-28T10:38:42.720864",
                                          "content": {
                                            "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJBQ0NFU1NfVE9LRU4iLCJpYXQiOjE3NjIzMDY3MjIsImV4cCI6MTc2MjMwODUyMiwic3ViIjoiODUiLCJyb2xlSWRzIjpbIlJPTEVfQlVZRVIiXSwidXNlcklkIjo4NSwiYXV0aFZlbmRvciI6Ik5BVElWRSJ9.O053YP2Vs41O_LYI2P3IGk7GLzaMeYM5mGgj0PZklxY",
                                            "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJSRUZSRVNIX1RPS0VOIiwiaWF0IjoxNzYyMzA2NzIyLCJleHAiOjE3NjM1MTYzMjIsInN1YiI6Ijg1Iiwicm9sZUlkcyI6WyJST0xFX0JVWUVSIl0sInVzZXJJZCI6ODUsImF1dGhWZW5kb3IiOiJOQVRJVkUifQ.k55p3WUr3i0GB1AIZCtIFLuQiAcXfPOv3qwmUMhAjmc",
                                            "isNewUser": true
                                          },
                                          "message": "회원 가입 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "중복된 회원 등록",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 409,
                                          "code": "ERR01",
                                          "timestamp": "2025-12-28T11:50:53.656526",
                                          "content": null,
                                          "message": "이미 가입된 회원입니다. 가입된 OIDC ID: 1234"
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
                                          "code": "ERR05",
                                          "timestamp": "2025-12-282T09:23:22.091551",
                                          "content": null,
                                          "message": "이메일 인증 코드가 유효하지 않거나 만료되었습니다. 전송된 이메일 주소: example@example.com"
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
                                          "code": "ERR06",
                                          "timestamp": "2025-12-28T16:13:25.045291",
                                          "content": null,
                                          "message": "비활성화된 계정입니다. 전송된 이메일 주소: example@example.com"
                                        }
                                        """)
                        )
                )
        }
)
public @interface SignUpApiDocs {
}
