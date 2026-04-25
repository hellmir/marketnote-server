package com.personal.marketnote.user.adapter.in.web.remotearea.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "도서산간 지역 목록 조회",
        description = """
                작성일자: 2026-04-26

                작성자: 성효빈

                ---

                ## Description
                - ACTIVE 상태의 도서산간 지역 전체 목록을 조회합니다.

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-04-26T12:00:00.000" |
                | content.remoteAreas | array | 도서산간 지역 목록 | |
                | content.remoteAreas[].id | number | 도서산간 지역 ID | 1 |
                | content.remoteAreas[].province | string | 광역시도 | "충남" |
                | content.remoteAreas[].district | string | 시군구 | "보령시" |
                | content.remoteAreas[].village | string | 읍면동 | "오천면" |
                | content.remoteAreas[].subarea | string | 세부지역 | "녹도리" |
                | message | string | 처리 결과 | "도서산간 지역 목록 조회 성공" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "도서산간 지역 목록 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-26T12:00:00.000000",
                                          "content": {
                                            "remoteAreas": [
                                              {
                                                "id": 1,
                                                "province": "제주",
                                                "district": "",
                                                "village": "",
                                                "subarea": ""
                                              },
                                              {
                                                "id": 2,
                                                "province": "충남",
                                                "district": "보령시",
                                                "village": "오천면",
                                                "subarea": "녹도리"
                                              }
                                            ]
                                          },
                                          "message": "도서산간 지역 목록 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetRemoteAreaApiDocs {
}
