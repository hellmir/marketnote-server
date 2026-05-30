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
        summary = "애드팝콘 리워드 콜백 (외부 콜백 전용 응답 형식)",
        description = """
                작성일자: 2026-01-17
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 애드팝콘 리워드 적립 콜백을 처리합니다.
                
                - 서명 검증에 실패하거나 중복 지급인 경우 오류 코드를 반환합니다.
                
                - **⚠️ 이 API는 외부 콜백 전용 응답 형식을 사용합니다 (BaseResponse 아님).**
                  응답 필드: `Result`(boolean), `ResultCode`(number), `ResultMsg`(string)
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | reward_key | string | 리워드 키 | Y | 20241211-abc |
                | usn | string | 회원 식별자 | Y | U1000 |
                | user_device_type | string | 회원 디바이스 유형 | Y | ANDROID |
                | campaign_key | string | 캠페인 키 | Y | C123 |
                | campaign_type | number | 캠페인 유형 | N | 1 |
                | campaign_name | string | 캠페인 이름 | N | "앱 설치" |
                | quantity | number | 지급 수량 | Y | 100 |
                | signed_value | string | 서명 값 | Y | "abc123" |
                | app_key | number | 앱 키 | N | 10 |
                | app_name | string | 앱 이름 | N | "my-app" |
                | adid | string | 구글 광고 ID | N | "adid-1" |
                | idfa | string | IDFA | N | "idfa-1" |
                | time_stamp | string | 캠페인 완료 일시 | N | "2026-01-17T12:00:00" |
                
                ---
                
                ## Response
                
                외부 콜백 전용 응답 형식 (BaseResponse 아님). 성공/실패 시 각 코드와 메시지를 포함한 JSON 문자열 반환
                
                | **필드** | **타입** | **설명** |
                | --- | --- | --- |
                | Result | boolean | 성공 여부 |
                | ResultCode | number | 결과 코드 (성공: 1, 실패: 1100/3100/3200/4000) |
                | ResultMsg | string | 결과 메시지 |
                """,
        parameters = {
                @Parameter(
                        name = "reward_key",
                        in = ParameterIn.QUERY,
                        required = true,
                        description = "리워드 키",
                        schema = @Schema(type = "string")
                ),
                @Parameter(
                        name = "usn",
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
                        name = "campaign_key",
                        in = ParameterIn.QUERY,
                        required = true,
                        description = "캠페인 키",
                        schema = @Schema(type = "string")
                ),
                @Parameter(
                        name = "campaign_type",
                        in = ParameterIn.QUERY,
                        description = "캠페인 유형",
                        schema = @Schema(type = "number")
                ),
                @Parameter(
                        name = "campaign_name",
                        in = ParameterIn.QUERY,
                        description = "캠페인 이름",
                        schema = @Schema(type = "string")
                ),
                @Parameter(
                        name = "quantity",
                        in = ParameterIn.QUERY,
                        required = true,
                        description = "지급 수량",
                        schema = @Schema(type = "number")
                ),
                @Parameter(
                        name = "signed_value",
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
                        name = "app_name",
                        in = ParameterIn.QUERY,
                        description = "앱 이름",
                        schema = @Schema(type = "string")
                ),
                @Parameter(
                        name = "adid",
                        in = ParameterIn.QUERY,
                        description = "구글 광고 ID",
                        schema = @Schema(type = "string")
                ),
                @Parameter(
                        name = "idfa",
                        in = ParameterIn.QUERY,
                        description = "IDFA",
                        schema = @Schema(type = "string")
                ),
                @Parameter(
                        name = "time_stamp",
                        in = ParameterIn.QUERY,
                        description = "캠페인 완료 일시",
                        schema = @Schema(type = "string")
                )
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "외부 콜백 전용 응답 형식 (BaseResponse 아님) — Result, ResultCode, ResultMsg 필드로 구성",
                        content = @Content(
                                mediaType = "application/json",
                                examples = {
                                        @ExampleObject(
                                                name = "성공",
                                                value = """
                                                        {
                                                          "Result": true,
                                                          "ResultCode": 1,
                                                          "ResultMsg": "success"
                                                        }
                                                        """
                                        ),
                                        @ExampleObject(
                                                name = "서명 검증 실패",
                                                value = """
                                                        {
                                                          "Result": false,
                                                          "ResultCode": 1100,
                                                          "ResultMsg": "invalid signed value"
                                                        }
                                                        """
                                        ),
                                        @ExampleObject(
                                                name = "회원 정보 존재하지 않음",
                                                value = """
                                                        {
                                                          "Result": false,
                                                          "ResultCode": 3200,
                                                          "ResultMsg": "invalid user"
                                                        }
                                                        """
                                        ),
                                        @ExampleObject(
                                                name = "중복 지급",
                                                value = """
                                                        {
                                                          "Result": false,
                                                          "ResultCode": 3100,
                                                          "ResultMsg": "duplicate transaction"
                                                        }
                                                        """
                                        ),
                                        @ExampleObject(
                                                name = "그 외 오류",
                                                value = """
                                                        {
                                                          "Result": false,
                                                          "ResultCode": 4000,
                                                          "ResultMsg": "custom error message"
                                                        }
                                                        """
                                        )
                                }
                        )
                )
        }
)
public @interface AdpopcornCallbackApiDocs {
}
