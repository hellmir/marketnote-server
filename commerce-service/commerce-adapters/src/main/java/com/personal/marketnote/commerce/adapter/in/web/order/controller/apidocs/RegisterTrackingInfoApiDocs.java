package com.personal.marketnote.commerce.adapter.in.web.order.controller.apidocs;

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
        summary = "(관리자/판매자) 송장 정보 등록",
        description = """
                작성일자: 2026-03-02

                작성자: 성효빈

                ---

                ## Description

                - 주문의 송장 정보(택배사, 송장번호)를 등록/수정합니다.

                - 이미 송장 정보가 등록된 경우 덮어씁니다.

                - 배송 상태(SHIPPING)로 전환하기 전에 반드시 송장 정보를 등록해야 합니다.

                ---

                ## Path Parameters

                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | id | number | 주문 ID | Y | 1 |

                ---

                ## Request Body

                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | courierCompany | string (enum) | 택배사 (CJ_LOGISTICS, HANJIN, LOTTE, LOGEN, POST_OFFICE, ETC) | Y | CJ_LOGISTICS |
                | trackingNumber | string | 송장번호 (최대 63자) | Y | 1234567890 |
                """,
        parameters = {
                @Parameter(name = "id", description = "주문 ID", required = true, example = "1")
        },
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "송장 정보 등록 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-03-02T12:00:00.000",
                                          "content": null,
                                          "message": "송장 정보 등록 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(responseCode = "401", description = "인증 실패"),
                @ApiResponse(responseCode = "403", description = "권한 없음"),
                @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
        })
public @interface RegisterTrackingInfoApiDocs {
}
