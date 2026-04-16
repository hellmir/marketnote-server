package com.personal.marketnote.commerce.adapter.in.web.quickpayment.controller.apidocs;

import com.personal.marketnote.commerce.adapter.in.web.quickpayment.request.IssueBatchKeyRequest;
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
        summary = "빠른결제 카드 배치키 발급",
        description = """
                작성일자: 2026-04-16

                작성자: 성효빈

                ---

                ## Description

                - 결제창 인증 완료 후 enc_data/enc_info로 KCP 배치키를 발급합니다.

                - 발급된 배치키와 카드 정보를 DB에 저장하고 카드 정보를 반환합니다.

                ---

                ## Request

                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | encData | string | KCP 결제창 인증결과 암호화 데이터 | Y | "encrypted_data..." |
                | encInfo | string | KCP 결제창 인증결과 암호화 정보 | Y | "encrypted_info..." |

                ---

                ## Response > content

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | quickPaymentCardId | number | 빠른결제 카드 ID | 1 |
                | cardCode | string | 카드사 코드 | "CCDI" |
                | cardName | string | 카드사명 | "현대카드" |
                | maskedCardNumber | string | 마스킹 카드번호 | null |
                | cardBinType01 | string | 개인(0)/법인(1) | "0" |
                | cardBinType02 | string | 일반(0)/체크(1) | "0" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = IssueBatchKeyRequest.class),
                        examples = @ExampleObject("""
                                {
                                  "encData": "encrypted_data_from_payment_window",
                                  "encInfo": "encrypted_info_from_payment_window"
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "빠른결제 카드 배치키 발급 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-16T17:19:33.409686",
                                          "content": {
                                            "quickPaymentCardId": 1,
                                            "cardCode": "CCDI",
                                            "cardName": "현대카드",
                                            "maskedCardNumber": null,
                                            "cardBinType01": "0",
                                            "cardBinType02": "0"
                                          },
                                          "message": "빠른결제 카드 배치키 발급 성공"
                                        }
                                        """)
                        )
                )
        })
public @interface IssueBatchKeyApiDocs {
}
