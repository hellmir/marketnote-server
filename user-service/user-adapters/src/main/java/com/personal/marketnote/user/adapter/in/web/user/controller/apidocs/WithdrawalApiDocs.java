package com.personal.marketnote.user.adapter.in.web.user.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "회원 탈퇴",
        description = """
                작성일자: 2025-12-29
                
                 작성자: 성효빈
                
                 ## Description
                
                 - 회원에서 탈퇴합니다.
                
                 - 모든 소셜 로그인 OIDC ID 및 OAuth2 공급 업체 제공 정보가 삭제됩니다.
                
                 - 모든 소셜 로그인(애플 로그인 제외)의 연결이 해제됩니다. 구글 연결 해제를 위해서는 현재 구글 로그인 상태여야 하며, 엑세스 토큰이 필요합니다.
                
                 - 현재 연결 해제 상태(서버에서 연결 해제 성공하거나, 처음부터 연결된 정보가 없는 경우)이면 isDisconnected: true를 반환합니다.
                
                 - 해당 공급업체 계정이 연결 해제되지 않은 경우 **isDisconnected: false**를 반환합니다.
                
                 ## Request
                
                 | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                 | --- | --- | --- | --- | --- |
                 | X-Google-Access-Token | string | 구글 액세스 토큰(현재 구글 로그인 상태인 경우 함께 전송) | N | "ya29.a0Af..." |
                
                 ---
                
                 ## Response
                
                 | **키** | **타입** | **설명** | **예시** |
                 | --- | --- | --- | --- |
                 | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                 | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                 | timestamp | string(datetime) | 응답 일시 | "2025-12-29T12:12:30.013" |
                 | content | object | 응답 본문 | { ... } |
                 | message | string | 처리 결과 | "회원 탈퇴 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | isKakaoDisconnected | boolean | 카카오 계정 연결 해제 여부 | true |
                | isGoogleDisconnected | boolean | 구글 계정 연결 해제 여부 | true |
                | isAppleDisconnected | boolean | 애플 계정 연결 해제 여부 | true |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "X-Google-Access-Token",
                        description = "현재 구글 로그인 상태인 경우 액세스 토큰",
                        required = false
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "회원 탈퇴 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2025-12-26T22:52:31.889943",
                                          "content": {
                                            "isKakaoDisconnected": true,
                                            "isGoogleDisconnected": true,
                                            "isAppleDisconnected": true
                                          },
                                          "message": "회원 탈퇴 성공"
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
                                          "timestamp": "2025-12-30T12:12:30.013",
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
                                          "timestamp": "2025-12-30T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        }
)
public @interface WithdrawalApiDocs {
}
