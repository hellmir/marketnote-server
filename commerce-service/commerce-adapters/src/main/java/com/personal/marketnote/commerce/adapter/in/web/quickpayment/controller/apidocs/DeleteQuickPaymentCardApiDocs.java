package com.personal.marketnote.commerce.adapter.in.web.quickpayment.controller.apidocs;

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
        summary = "빠른결제 카드 삭제",
        description = """
                작성일자: 2026-04-16
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - KCP에 배치키 삭제를 요청한 후 DB에서 카드를 비활성화합니다.
                
                - KCP 삭제 성공 시에만 DB 상태가 변경됩니다.
                
                ---
                
                ## Path Parameter
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | quickPaymentCardId | number | 빠른결제 카드 ID | 1 |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        parameters = {
                @Parameter(name = "quickPaymentCardId", description = "빠른결제 카드 ID", required = true, example = "1")
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "빠른결제 카드 삭제 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-16T17:19:33.409686",
                                          "content": null,
                                          "message": "빠른결제 카드 삭제 성공"
                                        }
                                        """)
                        )
                )
        })
public @interface DeleteQuickPaymentCardApiDocs {
}
