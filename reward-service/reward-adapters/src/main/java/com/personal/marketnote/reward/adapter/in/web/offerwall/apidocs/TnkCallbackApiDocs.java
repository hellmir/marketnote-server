package com.personal.marketnote.reward.adapter.in.web.offerwall.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "TNK 리워드 콜백",
        description = """
                작성일자: 2026-01-19
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - TNK 리워드 적립 콜백을 처리합니다.
                
                - 서명 검증에 실패하거나 중복 지급인 경우 오류 코드를 반환합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | seq_id | string | 리워드 키 | Y | 20241211-abc |
                | md_user_nm | string | 회원 식별자 | Y | U1000 |
                | user_device_type | string | 회원 디바이스 유형 | Y | ANDROID |
                | campaign_type | number | 캠페인 유형 | N | 1 |
                | pay_pnt | number | 지급 수량 | Y | 100 |
                | md_chk | string | 서명 값 | Y | "abc123" |
                | app_key | number | 앱 키 | N | 10 |
                | app_nm | string | 앱 이름 | N | "my-app" |
                | app_id | string | 구글 광고 ID | N | "adid-1" |
                | pay_dt | string | 캠페인 완료 일시 | N | "2026-01-17T12:00:00" |
                | pay_amt | number | 수익 | N | 100 |
                
                ---
                
                ## Response
                
                성공/실패 시 각 코드와 메시지를 포함한 JSON 문자열 반환
                """,
        parameters = {
                @Parameter(
                        name = "seq_id",
                        in = ParameterIn.QUERY,
                        required = true,
                        description = "리워드 키",
                        schema = @Schema(type = "string")
                ),
                @Parameter(
                        name = "md_user_nm",
                        in = ParameterIn.QUERY,
                        required = true,
                        description = "회원 식별자",
                        schema = @Schema(type = "string")
                ),
                @Parameter(
                        name = "user_device_type",
                        in = ParameterIn.QUERY,
                        required = true,
                        description = "회원 디바이스 유형",
                        schema = @Schema(type = "string")
                ),
                @Parameter(
                        name = "campaign_type",
                        in = ParameterIn.QUERY,
                        description = "캠페인 유형",
                        schema = @Schema(type = "number")
                ),
                @Parameter(
                        name = "pay_pnt",
                        in = ParameterIn.QUERY,
                        required = true,
                        description = "지급 수량",
                        schema = @Schema(type = "number")
                ),
                @Parameter(
                        name = "md_chk",
                        in = ParameterIn.QUERY,
                        required = true,
                        description = "서명 값",
                        schema = @Schema(type = "string")
                ),
                @Parameter(
                        name = "app_key",
                        in = ParameterIn.QUERY,
                        description = "앱 키",
                        schema = @Schema(type = "number")
                ),
                @Parameter(
                        name = "app_nm",
                        in = ParameterIn.QUERY,
                        description = "앱 이름",
                        schema = @Schema(type = "string")
                ),
                @Parameter(
                        name = "app_id",
                        in = ParameterIn.QUERY,
                        description = "구글 광고 ID",
                        schema = @Schema(type = "string")
                ),
                @Parameter(
                        name = "pay_dt",
                        in = ParameterIn.QUERY,
                        description = "캠페인 완료 일시",
                        schema = @Schema(type = "string")
                ),
                @Parameter(
                        name = "pay_amt",
                        in = ParameterIn.QUERY,
                        description = "리워드 수익",
                        schema = @Schema(type = "number")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "성공",
                        content = @Content(
                                mediaType = "application/json",
                                examples = {
                                        @ExampleObject(
                                                name = "성공",
                                                value = """
                                                        {
                                                          "statusCode": 200,
                                                          "code": "OK",
                                                          "timestamp": "2026-01-19T11:11:38.786628",
                                                          "content": null,
                                                          "message": "TNK 리워드 포인트 지급 성공"
                                                        }
                                                        """
                                        )
                                }
                        )
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "서명 검증 실패",
                        content = @Content(
                                mediaType = "application/json",
                                examples = {
                                        @ExampleObject(
                                                name = "서명 검증 실패",
                                                value = """
                                                        {
                                                          "statusCode": 401,
                                                          "code": "UNAUTHORIZED",
                                                          "timestamp": "2026-01-19T11:11:38.786628",
                                                          "content": null,
                                                          "message": "invalid signed value"
                                                        }
                                                        """
                                        )
                                }
                        )
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "회원 포인트 도메인 정보 조회 실패",
                        content = @Content(
                                mediaType = "application/json",
                                examples = {
                                        @ExampleObject(
                                                name = "회원 포인트 도메인 정보 조회 실패",
                                                value = """
                                                                                                                {
                                                          "statusCode": 404,
                                                          "code": "NOT_FOUND",
                                                          "timestamp": "2026-01-19T11:16:17.71728",
                                                          "content": null,
                                                          "message": "회원 정보를 찾을 수 없습니다. userKey: 165e6421-12a4-41c1-9e56-68d3bb03ca48"
                                                        }
                                                        """
                                        )
                                }
                        )
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "중복 지급",
                        content = @Content(
                                mediaType = "application/json",
                                examples = {
                                        @ExampleObject(
                                                name = "중복 지급",
                                                value = """
                                                        {
                                                          "statusCode": 409,
                                                          "code": "CONFLICT",
                                                          "timestamp": "2026-01-19T11:11:38.786628",
                                                          "content": null,
                                                          "message": "이미 해당 캠페인에 대한 리워드가 지급되었습니다. 전송된 캠페인 ID: abc"
                                                        }
                                                        """
                                        )
                                }
                        )
                ),
        }
)
public @interface TnkCallbackApiDocs {
}
