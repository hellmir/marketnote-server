package com.personal.marketnote.user.adapter.in.web.shippingaddress.controller.apidocs;

import com.personal.marketnote.user.adapter.in.web.shippingaddress.request.RegisterShippingAddressRequest;
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
        summary = "배송지 등록",
        description = """
                작성일자: 2026-02-19
                
                작성자: 성효빈
                
                ---
                
                ## Description
                - 새로운 배송지를 등록합니다.
                
                - **집(HOME)** 배송지는 사용자당 **1개**만 등록 가능합니다.
                
                - **회사(COMPANY)** 배송지는 사용자당 **1개**만 등록 가능합니다.
                
                - **기타(OTHER)** 배송지는 사용자당 최대 **5개**까지 등록 가능합니다.
                
                - 첫 번째 등록되는 배송지는 자동으로 **기본 배송지**로 설정됩니다.
                
                - **COMPANY** 타입일 때 `companyName`은 필수입니다.
                
                - **OTHER** 타입일 때 `addressAlias`는 필수입니다.
                
                - `deliveryRequestType`이 **CUSTOM**일 때 `deliveryRequestMessage`는 필수이며, 최대 **30자**까지 입력 가능합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | addressType | string(enum) | 배송지 타입 (HOME, COMPANY, OTHER) | Y | "HOME" |
                | address | string | 도로명 주소 | Y | "서울특별시 마포구 와우산로29바길 19" |
                | addressDetail | string | 상세주소 | Y | "삼이빌딩, 3층" |
                | companyName | string | 회사명 (COMPANY 타입 필수) | N | "올버스(주)" |
                | addressAlias | string | 주소 별명 (OTHER 타입 필수) | N | "친구집" |
                | recipientName | string | 받는 분 | Y | "박구글" |
                | recipientPhoneNumber | string | 휴대폰 번호 | Y | "01000000000" |
                | deliveryRequestType | string(enum) | 배송 요청사항 타입 (NONE: 선택 안 함, LEAVE_AT_DOOR: 문 앞에 놓아주세요, RECEIVE_OR_LEAVE_AT_DOOR: 직접 받고 부재시 문 앞에 놓아 주세요, LEAVE_AT_SECURITY: 경비실에 맡겨주세요, LEAVE_AT_DELIVERY_BOX: 택배함에 넣어주세요, CUSTOM: 직접 입력) | N | "LEAVE_AT_DOOR" |
                | deliveryRequestMessage | string | 직접입력 메시지 (CUSTOM 시 필수, 최대 30자) | N | "공동현관 비밀번호 *1234" |
                | isDefault | boolean | 기본 배송지 여부 | N | false |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 201: 성공 / 400: 클라이언트 요청 오류 / 409: 충돌 |
                | code | string | 응답 코드 | "SUC01" / "ERR01" / "ERR02" |
                | timestamp | string(datetime) | 응답 일시 | "2026-02-19T12:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "배송지 등록 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 배송지 ID | 1 |
                | addressType | string(enum) | 배송지 타입 | "HOME" |
                | isDefault | boolean | 기본 배송지 여부 | true |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = RegisterShippingAddressRequest.class),
                        examples = @ExampleObject("""
                                {
                                    "addressType": "HOME",
                                    "address": "서울특별시 마포구 와우산로29바길 19",
                                    "addressDetail": "삼이빌딩, 3층",
                                    "recipientName": "박구글",
                                    "recipientPhoneNumber": "01000000000",
                                    "deliveryRequestType": "LEAVE_AT_DOOR",
                                    "isDefault": false
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "배송지 등록 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 201,
                                          "code": "SUC01",
                                          "timestamp": "2026-02-19T12:00:00.000000",
                                          "content": {
                                            "id": 1,
                                            "addressType": "HOME",
                                            "isDefault": true
                                          },
                                          "message": "배송지 등록 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "이미 등록된 배송지 존재 (HOME/COMPANY)",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 409,
                                          "code": "ERR01",
                                          "timestamp": "2026-02-19T12:00:00.000000",
                                          "content": null,
                                          "message": "ERR01:: 이미 등록된 집 배송지가 존재합니다."
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "기타 배송지 최대 등록 수 초과 / 요청 검증 실패",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 400,
                                          "code": "ERR02",
                                          "timestamp": "2026-02-19T12:00:00.000000",
                                          "content": null,
                                          "message": "ERR02:: 기타 배송지는 최대 10개까지 등록할 수 있습니다."
                                        }
                                        """)
                        )
                )
        }
)
public @interface RegisterShippingAddressApiDocs {
}
