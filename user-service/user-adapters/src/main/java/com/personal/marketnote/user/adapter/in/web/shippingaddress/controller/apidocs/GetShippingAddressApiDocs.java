package com.personal.marketnote.user.adapter.in.web.shippingaddress.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "배송지 정보 조회",
        description = """
                작성일자: 2026-02-19
                
                작성자: 성효빈
                
                ---
                
                ## Description
                - 배송지 ID로 배송지 정보를 조회합니다.
                
                - 본인의 배송지만 조회할 수 있습니다.
                
                - Soft Delete된 배송지는 조회되지 않습니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | id | number(path) | 배송지 ID | O | 1 |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-02-19T12:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "배송지 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 배송지 ID | 1 |
                | addressType | string(enum) | 배송지 타입 (HOME, COMPANY, OTHER) | "HOME" |
                | address | string | 도로명 주소 | "서울특별시 마포구 와우산로29바길 19" |
                | addressDetail | string | 상세주소 | "삼이빌딩, 3층" |
                | companyName | string | 회사명 (COMPANY 타입만) | "올버스(주)" |
                | addressAlias | string | 주소 별명 (OTHER 타입만) | "친구집" |
                | recipientName | string | 받는 분 | "박구글" |
                | recipientPhoneNumber | string | 휴대폰 번호 | "01000000000" |
                | deliveryRequestType | string(enum) | 배송 요청사항 타입 | "LEAVE_AT_DOOR" |
                | deliveryRequestMessage | string | 직접입력 메시지 (CUSTOM 시) | "공동현관 비밀번호 *1234" |
                | isDefault | boolean | 기본 배송지 여부 | true |
                """,
        parameters = {
                @Parameter(name = "id", description = "배송지 ID", required = true, example = "1")
        },
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "배송지 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-19T12:00:00.000000",
                                          "content": {
                                            "id": 1,
                                            "addressType": "HOME",
                                            "address": "서울특별시 마포구 와우산로29바길 19",
                                            "addressDetail": "삼이빌딩, 3층",
                                            "companyName": null,
                                            "addressAlias": null,
                                            "recipientName": "박구글",
                                            "recipientPhoneNumber": "01000000000",
                                            "deliveryRequestType": "LEAVE_AT_DOOR",
                                            "deliveryRequestMessage": null,
                                            "isDefault": true
                                          },
                                          "message": "배송지 조회 성공"
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
                                          "code": "NOT_FOUND",
                                          "timestamp": "2026-02-19T12:00:00.000000",
                                          "message": "배송지를 찾을 수 없습니다. 전송된 배송지 ID: 999"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetShippingAddressApiDocs {
}
