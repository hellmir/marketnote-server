package com.personal.marketnote.reward.adapter.in.web.point.apidocs;

import com.personal.marketnote.reward.adapter.in.web.point.response.RegisterUserPointResponseSchema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
        summary = "(관리자) 회원 포인트 정보 생성",
        description = """
                작성일자: 2026-01-17
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                회원 포인트 정보를 생성합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | userId (path) | number | 회원 ID | Y | 100 |
                | userKey (query) | string(uuid) | 회원 키 | Y | "1a1c9131-ea3a-4809-b940-3dd665c96698" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 201 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 시간 | "2026-01-17T12:00:00.000" |
                | content | object | 응답 본문 | null |
                | message | string | 처리 결과 | "회원 포인트 정보 생성 성공" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "userId",
                        in = ParameterIn.PATH,
                        required = true,
                        description = "회원 ID",
                        schema = @Schema(type = "number", example = "100")
                ),
                @Parameter(
                        name = "userKey",
                        in = ParameterIn.QUERY,
                        required = true,
                        description = "회원 키",
                        schema = @Schema(type = "string", example = "1a1c9131-ea3a-4809-b940-3dd665c96698")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "회원 포인트 정보 생성 성공",
                        content = @Content(
                                schema = @Schema(implementation = RegisterUserPointResponseSchema.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 201,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-17T12:00:00.000",
                                          "content": null,
                                          "message": "회원 포인트 정보 생성 성공"
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
                                          "timestamp": "2026-01-07T12:12:30.013",
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
                                          "timestamp": "2026-01-07T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "회원 포인트 정보 존재",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 409,
                                          "code": "CONFLICT",
                                          "timestamp": "2026-01-17T15:45:05.92451",
                                          "content": null,
                                          "message": "이미 회원 포인트 정보가 생성되어 있습니다. 회원 ID: 100"
                                        }
                                        """)
                        )
                )
        }
)
public @interface RegisterUserPointApiDocs {
}
