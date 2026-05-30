package com.personal.marketnote.commerce.adapter.in.web.order.controller.apidocs;

import com.personal.marketnote.commerce.adapter.in.web.order.request.RequestReturnRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
        summary = "반품 요청",
        description = """
                작성일자: 2026-04-05
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 구매자가 반품을 요청합니다.
                
                - 주문 상태를 RETURN_REQUESTED(반품 요청됨)로 변경합니다.
                
                - 배송 완료, 부분 구매 확정, 부분 반품됨 상태에서만 반품 요청이 가능합니다.
                
                - 회수지 주소를 입력하면 입력값이 적용되고, 미입력 시 배송지가 회수지 기본값으로 적용됩니다.
                
                - 반품 사유 카테고리 목록
                
                    - "CANCEL_ORDER": 구매 의사 취소
                
                    - "CHANGE_OPTION": 색상, 사이즈 등 변경
                
                    - "MISTAKE": 주문 실수
                
                    - "ETC": 직접 입력
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | reasonCategory | string | 반품 사유 카테고리 | N | "ETC" |
                | reason | string | 반품 사유 | N | "상품 불량" |
                | pickupRecipientName | string | 회수지 수령인명 | N | "홍길동" |
                | pickupRecipientPhoneNumber | string | 회수지 연락처 | N | "010-1234-5678" |
                | pickupZipCode | string | 회수지 우편번호 | N | "12345" |
                | pickupAddress | string | 회수지 주소 | N | "서울시 강남구" |
                | pickupAddressDetail | string | 회수지 상세주소 | N | "테헤란로 123" |
                | pickupRequestMessage | string | 회수 요청사항 | N | "부재시 경비실에 맡겨주세요" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-04-05T12:00:00.000" |
                | content | object | 응답 본문 | null |
                | message | string | 처리 결과 | "반품 요청 성공" |
                """, security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(
                        name = "id",
                        description = "주문 ID",
                        in = ParameterIn.PATH,
                        required = true,
                        schema = @Schema(type = "number")
                )
        },
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = RequestReturnRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "reasonCategory": "ETC",
                                  "reason": "상품 불량",
                                  "pickupRecipientName": "홍길동",
                                  "pickupRecipientPhoneNumber": "010-1234-5678",
                                  "pickupZipCode": "12345",
                                  "pickupAddress": "서울시 강남구",
                                  "pickupAddressDetail": "테헤란로 123",
                                  "pickupRequestMessage": "부재시 경비실에 맡겨주세요"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "반품 요청 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-05T12:00:00.000",
                                          "content": null,
                                          "message": "반품 요청 성공"
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
                                          "timestamp": "2026-04-05T12:00:00.000",
                                          "content": null,
                                          "message": "Invalid token"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "이미 반품 요청됨",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 409,
                                          "code": "CONFLICT",
                                          "timestamp": "2026-04-05T12:00:00.000",
                                          "content": null,
                                          "message": "이미 해당 주문 상태(반품 요청됨)로 변경되었습니다."
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "상태 전이 불가",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 400,
                                          "code": "BAD_REQUEST",
                                          "timestamp": "2026-04-05T12:00:00.000",
                                          "content": null,
                                          "message": "주문 상태를 배송중에서 반품 요청됨(으)로 변경할 수 없습니다."
                                        }
                                        """)
                        )
                ),
        })
public @interface RequestReturnApiDocs {
}
