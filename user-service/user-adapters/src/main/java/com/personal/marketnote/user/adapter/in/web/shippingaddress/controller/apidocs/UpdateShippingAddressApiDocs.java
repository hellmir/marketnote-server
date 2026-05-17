package com.personal.marketnote.user.adapter.in.web.shippingaddress.controller.apidocs;

import com.personal.marketnote.user.adapter.in.web.shippingaddress.request.UpdateShippingAddressRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
        summary = "배송지 수정",
        description = """
                작성일자: 2026-02-19
                
                작성자: 성효빈
                
                ---
                
                ## Description
                - 기존 배송지 정보를 수정합니다.
                
                - **배송지 타입(addressType)은 변경할 수 없습니다.**
                
                - 본인의 배송지만 수정할 수 있습니다.
                
                - **COMPANY** 타입일 때 `companyName`은 필수입니다.
                
                - **OTHER** 타입일 때 `addressAlias`는 필수입니다.
                
                - `deliveryRequestType`이 **CUSTOM**일 때 `deliveryRequestMessage`는 필수이며, 최대 **30자**까지 입력 가능합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | address | string | 도로명 주소 | Y | "서울특별시 마포구 와우산로29바길 19" |
                | addressDetail | string | 상세주소 | Y | "삼이빌딩, 3층" |
                | companyName | string | 회사명 (COMPANY 타입 필수) | N | "올버스(주)" |
                | addressAlias | string | 주소 별명 (OTHER 타입 필수) | N | "친구집" |
                | recipientName | string | 받는 분 | Y | "박구글" |
                | recipientPhoneNumber | string | 휴대폰 번호 | Y | "01000000000" |
                | deliveryRequestType | string(enum) | 배송 요청사항 타입 (NONE: 선택 안 함, LEAVE_AT_DOOR: 문 앞에 놓아주세요, RECEIVE_OR_LEAVE_AT_DOOR: 직접 받고 부재시 문 앞에 놓아 주세요, LEAVE_AT_SECURITY: 경비실에 맡겨주세요, LEAVE_AT_DELIVERY_BOX: 택배함에 넣어주세요, CUSTOM: 직접 입력) | N | "LEAVE_AT_DOOR" |
                | deliveryRequestMessage | string | 직접입력 메시지 (CUSTOM 시 필수, 최대 30자) | N | "공동현관 비밀번호 *1234" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 404: 배송지 없음 |
                | code | string | 응답 코드 | "SUC01" / "ERR01" / "ERR02" |
                | timestamp | string(datetime) | 응답 일시 | "2026-02-19T12:00:00.000" |
                | content | object | 응답 본문 | null |
                | message | string | 처리 결과 | "배송지 수정 성공" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "id",
                        description = "배송지 ID",
                        required = true,
                        example = "1"
                )
        },
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = UpdateShippingAddressRequest.class),
                        examples = @ExampleObject("""
                                {
                                    "address": "서울특별시 마포구 와우산로29바길 19",
                                    "addressDetail": "삼이빌딩, 5층",
                                    "recipientName": "박구글",
                                    "recipientPhoneNumber": "01000000000",
                                    "deliveryRequestType": "LEAVE_AT_DOOR"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "배송지 수정 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-19T12:00:00.000000",
                                          "content": null,
                                          "message": "배송지 수정 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "배송지를 찾을 수 없음",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 404,
                                          "code": "ERR01",
                                          "timestamp": "2026-02-19T12:00:00.000000",
                                          "content": null,
                                          "message": "ERR01:: 배송지를 찾을 수 없습니다. 전송된 배송지 ID: 999"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "요청 검증 실패 (필수 필드 누락 등)",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 400,
                                          "code": "ERR02",
                                          "timestamp": "2026-02-19T12:00:00.000000",
                                          "content": null,
                                          "message": "ERR02:: 회사 배송지에는 회사명이 필수입니다."
                                        }
                                        """)
                        )
                )
        }
)
public @interface UpdateShippingAddressApiDocs {
}
