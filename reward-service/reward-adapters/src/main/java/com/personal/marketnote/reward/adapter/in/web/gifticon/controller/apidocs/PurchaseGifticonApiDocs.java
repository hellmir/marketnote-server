package com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs;

import com.personal.marketnote.reward.adapter.in.web.gifticon.response.PurchaseGifticonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "기프티콘 구매",
        description = """
                작성일자: 2026-04-06

                작성자: 성효빈

                ---

                ## Description

                캐시를 차감하여 기프티콘을 구매합니다.
                구매 성공 시 기프티쇼 쿠폰이 발행되며, 바코드 이미지 URL과 핀번호가 저장됩니다.
                캐시 부족 시 부족 금액과 함께 에러가 반환됩니다.
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "기프티콘 구매 성공",
                        content = @Content(
                                schema = @Schema(implementation = PurchaseGifticonResponse.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-06T12:00:00.000000",
                                          "content": {
                                            "orderId": 1,
                                            "orderNo": "20260404000001",
                                            "cashAmount": 5000,
                                            "goodsName": "아메리카노"
                                          },
                                          "message": "기프티콘 구매 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface PurchaseGifticonApiDocs {
}
