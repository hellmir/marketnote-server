package com.personal.marketnote.user.adapter.in.web.remotearea.controller.apidocs;

import com.personal.marketnote.user.adapter.in.web.remotearea.request.RegisterRemoteAreaRequest;
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
        summary = "도서산간 지역 등록",
        description = """
                작성일자: 2026-04-24
                
                작성자: 성효빈
                
                ---
                
                ## Description
                - 도서산간 지역을 등록합니다.
                
                - **광역시도(province)**는 필수이며, **시군구(district)**, **읍면동(village)**, **세부지역(subarea)**은 선택입니다.
                
                - 광역시도만 등록하면 해당 광역시도 전체가 도서산간 지역으로 지정됩니다.
                
                - 동일한 (광역시도, 시군구, 읍면동, 세부지역) 조합은 중복 등록할 수 없습니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | province | string | 광역시도 (최대 50자) | Y | "충남" |
                | district | string | 시군구 (최대 50자) | N | "보령시" |
                | village | string | 읍면동 (최대 50자) | N | "오천면" |
                | subarea | string | 세부지역 (최대 50자) | N | "녹도리" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 201: 성공 / 400: 요청 검증 실패 / 409: 중복 등록 |
                | code | string | 응답 코드 | "SUC01" / "ERR01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-04-24T12:00:00.000" |
                | content | null | 응답 본문 (없음) | null |
                | message | string | 처리 결과 | "도서산간 지역 등록 성공" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = RegisterRemoteAreaRequest.class),
                        examples = @ExampleObject("""
                                {
                                    "province": "충남",
                                    "district": "보령시",
                                    "village": "오천면",
                                    "subarea": "녹도리"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "도서산간 지역 등록 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 201,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-24T12:00:00.000000",
                                          "content": null,
                                          "message": "도서산간 지역 등록 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "이미 등록된 도서산간 지역",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 409,
                                          "code": "ERR01",
                                          "timestamp": "2026-04-24T12:00:00.000000",
                                          "content": null,
                                          "message": "ERR_REMOTE_AREA_06::이미 등록된 도서산간 지역입니다."
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "요청 검증 실패",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 400,
                                          "code": "ERR01",
                                          "timestamp": "2026-04-24T12:00:00.000000",
                                          "content": null,
                                          "message": "광역시도는 필수값입니다."
                                        }
                                        """)
                        )
                )
        }
)
public @interface RegisterRemoteAreaApiDocs {
}
