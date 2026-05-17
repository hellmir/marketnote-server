package com.personal.marketnote.user.adapter.in.web.user.controller.apidocs;

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
        summary = "(관리자) 회원 로그인 내역 조회",
        description = """
                작성일자: 2025-12-30
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 회원 로그인 내역 목록을 조회합니다.
                
                - 관리자만 가능합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | userId | number | 회원 ID (path) | Y | 1 |
                | pageSize | number | 페이지 크기 | N | default: 10 |
                | pageNumber | number | 페이지 번호(1부터) | N | default: 1 |
                | sortDirection | string | 정렬 방향 | N | default: "DESC" |
                | userSortProperty | string | 정렬 속성 | N | default: "ID" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2025-12-30T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "회원 로그인 내역 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | pageSize | number | 페이지 크기 | 10 |
                | pageNumber | number | 현재 페이지 | 1 |
                | totalCount | number | 총 아이템 수 | 127 |
                | hasPrevious | boolean | 이전 페이지 존재 여부 | true / false |
                | hasNext | boolean | 다음 페이지 존재 여부 | true / false |
                | histories | array | 로그인 내역 목록 | [ ... ] |
                
                ---
                
                ### Response > content > histories
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 로그인 내역 ID | 1 |
                | userId | number | 회원 ID | 1 |
                | authVendor | string | 인증 제공자 | "NATIVE"/"KAKAO"/"GOOGLE"/"APPLE" |
                | ipAddress | string | 로그인 IP | "203.0.113.10" |
                """, security = {@SecurityRequirement(name = "bearer"),
        @SecurityRequirement(name = "admin")}, parameters = {
        @Parameter(
                name = "userId",
                in = ParameterIn.PATH,
                required = true,
                description = "회원 ID",
                schema = @Schema(type = "number", example = "1")
        ),
        @Parameter(
                name = "pageSize",
                in = ParameterIn.QUERY,
                required = false,
                description = "페이지 크기",
                schema = @Schema(type = "number", example = "10")
        ),
        @Parameter(
                name = "pageNumber",
                in = ParameterIn.QUERY,
                required = false,
                description = "페이지 번호(1부터)",
                schema = @Schema(type = "number", example = "1")
        ),
        @Parameter(
                name = "sortDirection",
                in = ParameterIn.QUERY,
                required = false,
                description = "정렬 방향",
                schema = @Schema(type = "string", example = "DESC")
        ),
        @Parameter(
                name = "userSortProperty",
                in = ParameterIn.QUERY,
                required = false,
                description = "정렬 속성",
                schema = @Schema(type = "string", example = "ID")
        )
}, responses = {
        @ApiResponse(
                responseCode = "200",
                description = "회원 로그인 내역 조회 성공",
                content = @Content(
                        examples = @ExampleObject("""
                                {
                                  "statusCode": 200,
                                  "code": "SUC01",
                                  "timestamp": "2025-12-30T13:32:27.721435519",
                                  "content": {
                                    "pageSize": 10,
                                    "pageNumber": 1,
                                    "totalCount": 7,
                                    "hasPrevious": false,
                                    "hasNext": false,
                                    "histories": [
                                      {
                                        "id": 13,
                                        "userId": 13,
                                        "authVendor": "KAKAO",
                                        "ipAddress": "221.147.24.144",
                                        "loggedInAt": "2025-12-30T13:31:08.926108"
                                      },
                                      {
                                        "id": 12,
                                        "userId": 13,
                                        "authVendor": "KAKAO",
                                        "ipAddress": "221.147.24.144",
                                        "loggedInAt": "2025-12-30T13:30:04.124981"
                                      },
                                      {
                                        "id": 11,
                                        "userId": 13,
                                        "authVendor": "KAKAO",
                                        "ipAddress": "221.147.24.144",
                                        "loggedInAt": "2025-12-30T13:29:48.890364"
                                      },
                                      {
                                        "id": 10,
                                        "userId": 13,
                                        "authVendor": "KAKAO",
                                        "ipAddress": "221.147.24.144",
                                        "loggedInAt": "2025-12-30T22:28:21.960414"
                                      },
                                      {
                                        "id": 9,
                                        "userId": 13,
                                        "authVendor": "NATIVE",
                                        "ipAddress": "221.147.24.144",
                                        "loggedInAt": "2025-12-30T13:16:49.951182"
                                      },
                                      {
                                        "id": 8,
                                        "userId": 13,
                                        "authVendor": "GOOGLE",
                                        "ipAddress": "221.147.24.144",
                                        "loggedInAt": "2025-12-30T13:16:39.257946"
                                      },
                                      {
                                        "id": 7,
                                        "userId": 13,
                                        "authVendor": "GOOGLE",
                                        "ipAddress": "221.147.24.144",
                                        "loggedInAt": "2025-12-30T13:16:16.724356"
                                      }
                                    ]
                                  },
                                  "message": "회원 로그인 내역 조회 성공"
                                }
                                """)
                )
        )
})
public @interface GetLoginHistoriesApiDocs {
}
