package com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs;

import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetMyGifticonOrderDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
        summary = "내 기프티콘 상세 조회",
        description = """
                작성일자: 2026-04-06
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                사용자가 구매한 기프티콘의 상세 정보를 조회합니다.
                ISSUED 상태인 경우 기프티쇼 API를 호출하여 최신 상태를 동기화합니다.
                PIN 번호는 복호화되어 응답됩니다.
                """,
        parameters = {
                @Parameter(name = "orderId", description = "주문 ID", example = "1", required = true)
        },
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "내 기프티콘 상세 조회 성공",
                        content = @Content(
                                schema = @Schema(implementation = GetMyGifticonOrderDetailResponse.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-06T12:00:00.000000",
                                          "content": {
                                            "orderId": 1,
                                            "goodsName": "아메리카노",
                                            "brandName": "스타벅스",
                                            "brandImageUrl": "https://example.com/brand.jpg",
                                            "productImageUrl": "https://example.com/goods.jpg",
                                            "description": "스타벅스 아메리카노 Tall",
                                            "cashPrice": 5000,
                                            "couponImageUrl": "https://example.com/coupon.jpg",
                                            "pinNo": "900343630367",
                                            "expiryDate": "26.05.04까지 사용 가능",
                                            "daysRemaining": 30,
                                            "statusLabel": null,
                                            "orderStatus": "ISSUED",
                                            "createdAt": "2026-04-06T12:00:00"
                                          },
                                          "message": "내 기프티콘 상세 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetMyGifticonOrderDetailApiDocs {
}
