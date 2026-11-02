package com.personal.marketnote.user.adapter.in.web.authentication.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "이메일 인증 요청 전송",
        description = """
                작성일자: 2025-12-28
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                이메일 인증 요청을 전송합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | email | string | 이메일 주소 | Y | "example@example.com" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "NOT_FOUND" / "BAD_GATEWAY" |
                | timestamp | string(datetime) | 응답 일시 | "2025-12-26T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "이메일 인증 요청 전송 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                """,
        parameters = {
                @Parameter(
                        name = "email",
                        description = "이메일 주소",
                        required = true,
                        example = "example@example.com"
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "이메일 인증 요청 전송 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2025-12-28T11:20:19.055291",
                                          "content": null,
                                          "message": "이메일 인증 요청 전송 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "502",
                        description = "이메일 인증 요청 전송 실패",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 502,
                                          "code": "BAD_GATEWAY",
                                          "timestamp": "2025-12-28T10:03:42.211246",
                                          "content": null,
                                          "message": "Failed messages: org.eclipse.angus.mail.smtp.SMTPSendFailedException: 554 Message rejected: Email address is not verified. The following identities failed the check in region AP-NORTHEAST-2: example@example.com\\n"
                                        }
                                        """)
                        )
                )
        }
)
public @interface SendEmailVerificationApiDocs {
}
