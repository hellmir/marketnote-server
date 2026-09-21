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
        summary = "(관리자) 회원 목록 조회",
        description = """
                작성일자: 2025-12-29
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 회원 목록을 조회합니다.
                
                - 관리자만 가능합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | pageSize | number | 페이지 크기 | N | default: 10 |
                | pageNumber | number | 페이지 번호(1부터) | N | default: 1 |
                | sortDirection | string | 정렬 방향 | N | default: "DESC" |
                | userSortProperty | string | 정렬 속성 | N | default: "ORDER_NUM" |
                | productSearchTarget | string | 검색 대상 | N | default: "EMAIL" |
                | searchKeyword | string | 검색 키워드 | N | default: "ample@example.co" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "UNAUTHORIZED" / / "FORBIDDEN" / "NOT_FOUND" |
                | timestamp | string(datetime) | 응답 일시 | "2025-12-29T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "회원 목록 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | pageSize | number | 페이지 크기 | 10 |
                | pageNumber | number | 현재 페이지 | 1 |
                | totalCount | number | 총 아이템 수 | 127 |
                | hasPrevious | boolean | 이전 페이지 존재 여부 | true / false |
                | hasNext | boolean | 다음 페이지 존재 여부 | true / false |
                | users | array | 회원 목록 | [ ... ] |
                
                ---
                
                ### Response > content > users
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 회원 ID | 1 |
                | accountInfo | object | 계정 정보 | { ... } |
                | nickname | string | 닉네임 | "고길동" |
                | email | string | 이메일 주소 | "example@example.com" |
                | fullName | string | 성명 | "홍길동" |
                | phoneNumber | string | 전화번호 | "010-1234-5678" |
                | referenceCode | string | 참조 코드 | "1234567890" |
                | roleId | string | 역할 ID | "ROLE_BUYER" |
                | signedUpAt | string(datetime) | 가입 일시 | "2025-12-29T10:19:52.558748" |
                | lastLoggedInAt | string(datetime) | 마지막 로그인 일시 | "2025-12-29T10:19:52.558748" |
                | status | string | 상태 | "ACTIVE" / "INACTIVE" / "DELETED" |
                | isWithdrawn | boolean | 탈퇴 여부 | true / false |
                | orderNum | number | 정렬 순서 | 1 |
                | penaltyCount | number | 패널티 횟수 | 0 |
                ---
                
                ### Response > content > users > accountInfo
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | accounts | array | 계정 목록 | [ ... ] |
                
                ---
                
                ### Response > content > users > accountInfo > accounts
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | oauth2VendorId | string | 인증 제공자 | "NATIVE" / "KAKAO" / "GOOGLE" / "APPLE" |
                | oidcId | string | OIDC ID(일반 회원인 경우 이메일 주소) | "1234567890" |
                """,
        security = {@SecurityRequirement(name = "bearer"), @SecurityRequirement(name = "admin")},
        parameters = {
                @Parameter(
                        name = "pageSize",
                        in = ParameterIn.QUERY,
                        required = false,
                        description = "페이지 크기",
                        schema = @Schema(type = "integer", minimum = "1", example = "10")
                ),
                @Parameter(
                        name = "pageNumber",
                        in = ParameterIn.QUERY,
                        required = false,
                        description = "페이지 번호(1부터)",
                        schema = @Schema(type = "integer", minimum = "1", example = "1")
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
                        schema = @Schema(type = "string", example = "ORDER_NUM")
                ),
                @Parameter(
                        name = "productSearchTarget",
                        in = ParameterIn.QUERY,
                        required = false,
                        description = "검색 대상",
                        schema = @Schema(type = "string", example = "EMAIL")
                ),
                @Parameter(
                        name = "searchKeyword",
                        in = ParameterIn.QUERY,
                        required = false,
                        description = "검색 키워드",
                        schema = @Schema(type = "string", example = "")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "회원 목록 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2025-12-29T17:12:17.565718",
                                          "content": {
                                            "pageSize": 3,
                                            "pageNumber": 2,
                                            "totalCount": 5,
                                            "hasPrevious": true,
                                            "hasNext": false,
                                            "users": [
                                              {
                                                "id": 86,
                                                "accountInfo": {
                                                  "accounts": [
                                                    {
                                                      "oauth2VendorId": "NATIVE",
                                                      "oidcId": "sung@personal.com"
                                                    },
                                                    {
                                                      "oauth2VendorId": "KAKAO",
                                                      "oidcId": null
                                                    },
                                                    {
                                                      "oauth2VendorId": "GOOGLE",
                                                      "oidcId": null
                                                    },
                                                    {
                                                      "oauth2VendorId": "APPLE",
                                                      "oidcId": null
                                                    }
                                                  ]
                                                },
                                                "nickname": null,
                                                "email": "sung@personal.com",
                                                "fullName": null,
                                                "phoneNumber": null,
                                                "referenceCode": "Q9J4N2",
                                                "roleId": "ROLE_BUYER",
                                                "signedUpAt": "2025-12-28T15:59:59.132803",
                                                "lastLoggedInAt": "2025-12-28T15:04:14.896225",
                                                "status": "INACTIVE",
                                                "isWithdrawn": true,
                                                "orderNum": 1,
                                                "penaltyCount": 3
                                              },
                                              {
                                                "id": 87,
                                                "accountInfo": {
                                                  "accounts": [
                                                    {
                                                      "oauth2VendorId": "NATIVE",
                                                      "oidcId": "sung1@personal.com"
                                                    },
                                                    {
                                                      "oauth2VendorId": "KAKAO",
                                                      "oidcId": null
                                                    },
                                                    {
                                                      "oauth2VendorId": "GOOGLE",
                                                      "oidcId": null
                                                    },
                                                    {
                                                      "oauth2VendorId": "APPLE",
                                                      "oidcId": null
                                                    }
                                                  ]
                                                },
                                                "nickname": null,
                                                "email": "sung1@personal.com",
                                                "fullName": null,
                                                "phoneNumber": null,
                                                "referenceCode": "H8M7G6",
                                                "roleId": "ROLE_BUYER",
                                                "signedUpAt": "2025-12-28T15:59:59.132803",
                                                "lastLoggedInAt": "2025-12-29T15:22:45.433588",
                                                "status": "ACTIVE",
                                                "isWithdrawn": false,
                                                "orderNum": 2,
                                                "penaltyCount": 0
                                              }
                                            ]
                                          },
                                          "message": "회원 목록 조회 성공"
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
                                          "timestamp": "2025-12-29T10:19:52.558748",
                                          "content": null,
                                          "message": "Invalid token"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "403",
                        description = "토큰 인가 실패(관리자가 아님)",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 403,
                                          "code": "FORBIDDEN",
                                          "timestamp": "2025-12-29T10:19:52.558748",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetUsersApiDocs {
}
