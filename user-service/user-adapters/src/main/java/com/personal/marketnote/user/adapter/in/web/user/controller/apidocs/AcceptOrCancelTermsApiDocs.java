package com.personal.marketnote.user.adapter.in.web.user.controller.apidocs;

import com.personal.marketnote.user.adapter.in.web.user.request.AcceptOrCancelTermsRequest;
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
        summary = "약관 동의 또는 철회",
        description = """
                작성일자: 2025-12-27
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 회원이 약관에 동의하거나 철회합니다.
                
                - 요청 시 기존 동의 여부의 반대 상태가 됩니다. (동의 → 철회, 철회 → 동의)
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | ids | array | 동의 또는 철회할 약관 ID 목록 | Y | [ 1, 2, 3, 4 ] |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "UNAUTHORIZED" |
                | timestamp | string(datetime) | 응답 일시 | "2025-12-26T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "약관 동의/철회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | userTerms | array | 회원 약관 목록 | [ ... ] |
                
                ---
                
                #### Response > content > userTerms
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 약관 ID | 1 |
                | content | string | 약관 내용 | "서비스 이용 약관 동의" |
                | isRequired | boolean | 필수 동의 여부 | true / false |
                | isAgreed | boolean | 동의/미동의 여부 | true / false |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = AcceptOrCancelTermsRequest.class),
                        examples = @ExampleObject("""
                                {
                                    "ids": [ 1, 2, 3, 4 ]
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "약관 동의/철회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2025-12-27T14:32:14.084538",
                                          "content": {
                                            "userTerms": [
                                              {
                                                "id": 1,
                                                "content": "서비스 이용 약관 동의",
                                                "isRequired": true,
                                                "isAgreed": false
                                              },
                                              {
                                                "id": 2,
                                                "content": "개인정보 수집 및 이용 동의",
                                                "isRequired": true,
                                                "isAgreed": false
                                              },
                                              {
                                                "id": 3,
                                                "content": "만 14세 이상입니다.",
                                                "isRequired": true,
                                                "isAgreed": true
                                              },
                                              {
                                                "id": 4,
                                                "content": "무슨무슨 동의",
                                                "isRequired": true,
                                                "isAgreed": true
                                              },
                                              {
                                                "id": 5,
                                                "content": "마케팅 정보 수집 동의",
                                                "isRequired": false,
                                                "isAgreed": false
                                              }
                                            ]
                                          },
                                          "message": "약관 동의/철회 성공"
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
                )
        }
)
public @interface AcceptOrCancelTermsApiDocs {
}
