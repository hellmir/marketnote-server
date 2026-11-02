package com.personal.marketnote.user.adapter.in.web.user.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "(비회원) 전체 약관 목록 조회",
        description = """
                작성일자: 2025-12-27
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                회원 약관 목록을 조회합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2025-12-26T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "회원 약관 목록 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | terms | array | 회원 약관 목록 | [ ... ] |
                
                ---
                
                #### Response > content > terms
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 약관 ID | 1 |
                | content | string | 약관 내용 | "회원 약관 내용1" |
                | isRequired | boolean | 필수 동의 여부 | true / false |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "전체 약관 목록 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2025-12-26T22:52:31.889943",
                                          "content": {
                                            "terms": [
                                             {
                                                "id": 1,
                                                "content": "회원 약관 내용1",
                                                "isRequired": true
                                              },
                                              {
                                                "id": 2,
                                                "content": "회원 약관 내용2",
                                                "isRequired": true
                                              },
                                              {
                                                "id": 3,
                                                "content": "회원 약관 내용3",
                                                "isRequired": true
                                              },
                                              {
                                                "id": 4,
                                                "content": "회원 약관 내용4",
                                                "isRequired": true
                                              },
                                              {
                                                "id": 5,
                                                "content": "회원 약관 내용5",
                                                "isRequired": false
                                              }
                                            ]
                                          },
                                          "message": "전체 약관 목록 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetAllTermsApiDocs {
}
