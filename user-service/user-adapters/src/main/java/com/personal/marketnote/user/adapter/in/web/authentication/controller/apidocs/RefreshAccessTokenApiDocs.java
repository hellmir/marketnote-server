package com.personal.marketnote.user.adapter.in.web.authentication.controller.apidocs;

import com.personal.marketnote.user.adapter.in.web.authentication.controller.schema.RefreshedAccessTokenResponseSchema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpHeaders;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "Refresh Token으로 새로운 Access Token 발급",
        description = """
                작성일자: 2025-12-26
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                Refresh Token을 전송해 새로운 Access Token을 발급합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | refreshToken(cookie) | string | Refresh Token; HTTP-only | Y | "<jwt-refresh-token>" |
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 201: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" |
                | content | object | 응답 본문 | { ... } |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | accessToken | string | 신규 발급된 Access Token | "f8310f8asohvh80scvh0zio3hr31d" |
                """,
        parameters = @Parameter(
                name = "refresh_token",
                in = ParameterIn.COOKIE,
                required = true,
                description = "Refresh Token; HTTP-only"
        ),
        responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Access Token 재발급 성공",
                        content = @Content(
                                schema = @Schema(implementation = RefreshedAccessTokenResponseSchema.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 201,
                                          "timestamp": "2025-12-26T09:53:02.089234",
                                          "code": "SUC01",
                                          "content": {
                                            "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJBQ0NFU1NfVE9LRU4iLCJpYXQiOjE3NjE1MzI0MTEsImV4cCI6MTc2MTUzNDIxMSwic3ViIjoibnVsbCIsInJvbGVJZHMiOlsiUk9MRV9CVVlFUiJdLCJhdXRoVmVuZG9yIjoiTkFUSVZFIn0.ZzIV6LtD8jd5VQKdKcWschPDkyzOTSbIdhUE4ezvwk4"
                                          },
                                          "message": "Access Token 재발급 성공"
                                        }
                                        """)
                        ),
                        headers = @Header(name = HttpHeaders.SET_COOKIE, description = "Refresh Token; HTTP-only")
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "지원하지 않는 리프레시 토큰 값 전송",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 400,
                                          "timestamp": "2025-12-26T09:53:02.089234",
                                          "code": "BAD_REQUEST",
                                          "content": null,
                                          "message": "지원하지 않는 리프레시 토큰입니다."
                                        }
                                        """)
                        )
                )
        }
)
public @interface RefreshAccessTokenApiDocs {
}
