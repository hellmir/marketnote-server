package com.personal.marketnote.reward.adapter.in.web.gifticon.controller.apidocs;

import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetMyGifticonOrdersResponse;
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
        summary = "내 기프티콘 목록 조회",
        description = """
                작성일자: 2026-04-06

                작성자: 성효빈

                ---

                ## Description

                사용자가 구매한 기프티콘 목록을 조회합니다.
                AVAILABLE(사용가능) / COMPLETED_OR_EXPIRED(완료/만료) 탭별 필터링과
                PURCHASE_LATEST(구매 최신순) / EXPIRY_SOONEST(유효기간 임박순) 정렬을 지원합니다.

                ### Parameters
                - status: AVAILABLE(사용가능, ISSUED) / COMPLETED_OR_EXPIRED(사용완료/기간만료/취소)
                - sort: PURCHASE_LATEST(구매 최신순) / EXPIRY_SOONEST(유효기간 임박순)
                - cursor: 페이징 커서 (기본값 -1)
                - page-size: 페이지 크기 (기본값 10)
                """,
        parameters = {
                @Parameter(name = "status", description = "상태 필터 (AVAILABLE / COMPLETED_OR_EXPIRED)", example = "AVAILABLE"),
                @Parameter(name = "sort", description = "정렬 (PURCHASE_LATEST / EXPIRY_SOONEST)", example = "PURCHASE_LATEST"),
                @Parameter(name = "cursor", description = "페이징 커서", example = "-1"),
                @Parameter(name = "page-size", description = "페이지 크기", example = "10")
        },
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "내 기프티콘 목록 조회 성공",
                        content = @Content(
                                schema = @Schema(implementation = GetMyGifticonOrdersResponse.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-04-06T12:00:00.000000",
                                          "content": {
                                            "availableCount": 3,
                                            "completedOrExpiredCount": 5,
                                            "hasNext": true,
                                            "nextCursor": 8,
                                            "items": [
                                              {
                                                "orderId": 10,
                                                "goodsName": "아메리카노",
                                                "brandName": "스타벅스",
                                                "productImageUrl": "https://example.com/goods.jpg",
                                                "cashPrice": 5000,
                                                "expiryDate": "26.05.04까지 사용 가능",
                                                "daysRemaining": 30,
                                                "statusLabel": null,
                                                "orderStatus": "ISSUED",
                                                "createdAt": "2026-04-06T12:00:00"
                                              }
                                            ]
                                          },
                                          "message": "내 기프티콘 목록 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetMyGifticonOrdersApiDocs {
}
