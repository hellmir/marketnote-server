package com.personal.marketnote.user.adapter.in.web.remotearea.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
        summary = "도서산간 지역 삭제",
        description = """
                작성일자: 2026-04-26

                작성자: 성효빈

                ---

                ## Description
                - 도서산간 지역을 비활성화(소프트 삭제)합니다.

                ---

                ## Path Parameter

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 도서산간 지역 ID | 1 |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(name = "id", description = "도서산간 지역 ID", required = true, example = "1")
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "도서산간 지역 삭제 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-26T12:00:00.000000",
                                          "content": null,
                                          "message": "도서산간 지역 삭제 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "도서산간 지역을 찾을 수 없음",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 404,
                                          "code": "ERR_REMOTE_AREA_07",
                                          "timestamp": "2026-04-26T12:00:00.000000",
                                          "content": null,
                                          "message": "도서산간 지역을 찾을 수 없습니다. id=999"
                                        }
                                        """)
                        )
                )
        }
)
public @interface DeleteRemoteAreaApiDocs {
}
