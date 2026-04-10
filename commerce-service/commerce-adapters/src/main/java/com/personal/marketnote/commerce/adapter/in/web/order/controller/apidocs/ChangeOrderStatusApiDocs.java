package com.personal.marketnote.commerce.adapter.in.web.order.controller.apidocs;

import com.personal.marketnote.commerce.adapter.in.web.order.request.ChangeOrderStatusRequest;
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
        summary = "주문 상태 변경",
        description = """
                작성일자: 2026-01-05
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 주문 상태를 변경합니다.
                
                - 가격 정책 목록 전송 시, 해당하는 주문 상품의 상태만 변경합니다.
                
                - 가격 정책 목록 미전송 시, 모든 주문 상품의 상태를 변경합니다.
                
                - 주문 상태 목록
                
                    - "PAYMENT_PENDING": 결제 대기
                
                    - "PAID": 결제 완료
                
                    - "FAILED": 결제 실패
                
                    - "PREPARING": 상품 준비중
                
                    - "PREPARED": 상품 준비 완료
                
                    - "CANCEL_REQUESTED": 주문 취소 요청됨
                
                    - "CANCELLED": 주문 취소
                
                    - "SHIPPING": 배송중
                
                    - "DELIVERED": 배송 완료
                
                    - "CONFIRMED": 구매 확정
                
                    - "EXCHANGE_REQUESTED": 교환 요청됨
                
                    - "EXCHANGE_RECALLING": 교환 회수 중
                
                    - "EXCHANGE_SHIPPING": 교환 배송 중
                
                    - "EXCHANGED": 교환 완료
                
                    - "REFUND_REQUESTED": 환불 요청됨
                
                    - "REFUND_RECALLING": 환불 회수 중
                
                    - "REFUND_SHIPPING": 환불 배송 중
                
                    - "PARTIALLY_REFUNDED": 부분 환불됨
                
                    - "REFUNDED": 환불 완료
                
                - 변경 사유 카테고리 목록
                
                    - "CANCEL_ORDER": 구매 의사 취소
                
                    - "CHANGE_OPTION": 색상, 사이즈 등 변경
                
                    - "MISTAKE": 주문 실수
                
                    - "ETC": 직접 입력
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | pricePolicyIds | array<number> | 가격 정책 ID 목록 | N | [1, 2, 3] |
                | orderStatus | string | 주문 상태 | Y | "PAYMENT_PENDING" |
                | reasonCategory | string | 변경 사유 카테고리 | N | "CANCEL_ORDER" |
                | reason | string | 변경 사유 | N | default: 변경된 주문 상태의 디스크립션 |
                | pickupAddressId | number | 회수지 배송지 ID (반품 신청 시, 미입력 시 배송지 기본값) | N | 5 |
                | pickupRequestType | string | 회수 요청 타입 (NONE, CALL_BEFORE_VISIT, LEAVE_AT_DOOR, LEAVE_AT_SECURITY, CUSTOM) | N | "LEAVE_AT_DOOR" |
                | pickupRequestMessage | string | 회수 요청사항 (CUSTOM 시 필수, 60자 제한) | N | "부재시 경비실에 맡겨주세요" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 201: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2026-01-05T12:12:30.013" |
                | content | object | 응답 본문 | null |
                | message | string | 처리 결과 | "주문 상태 변경 성공" |
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
                        schema = @Schema(implementation = ChangeOrderStatusRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "pricePolicyIds": [1, 2, 3],
                                  "orderStatus": "REFUND_REQUESTED",
                                  "reasonCategory": "CANCEL_ORDER",
                                  "reason": "주문 상태 변경 사유",
                                  "pickupAddressId": 5,
                                  "pickupRequestType": "LEAVE_AT_DOOR",
                                  "pickupRequestMessage": null
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "주문 상태 변경 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-05T12:12:30.013",
                                          "content": null,
                                          "message": "주문 상태 변경 성공"
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
                                          "timestamp": "2026-01-05T12:12:30.013",
                                          "content": null,
                                          "message": "Invalid token"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "403",
                        description = "토큰 인가 실패",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 403,
                                          "code": "FORBIDDEN",
                                          "timestamp": "2026-01-05T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "이미 주문 상태 변경됨",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 409,
                                          "code": "CONFLICT",
                                          "timestamp": "2026-01-07T13:34:28.180703",
                                          "content": null,
                                          "message": "이미 해당 주문 상태(결제 완료)로 변경되었습니다."
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "재고 수량이 음수값이 됨(주로 재고 부족이 원인)",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 400,
                                          "code": "BAD_REQUEST",
                                          "timestamp": "2026-01-06T17:50:25.320936",
                                          "content": null,
                                          "message": "재고 수량은 0 또는 양의 정수(0 이상의 숫자값)여야 합니다. 전송된 수량: -2"
                                        }
                                        """)
                        )
                ),
        })
public @interface ChangeOrderStatusApiDocs {
}

