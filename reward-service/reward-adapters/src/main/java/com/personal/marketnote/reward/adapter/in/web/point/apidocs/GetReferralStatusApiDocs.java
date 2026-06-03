package com.personal.marketnote.reward.adapter.in.web.point.apidocs;

import com.personal.marketnote.reward.adapter.in.web.point.response.GetReferralStatusResponse;
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
        summary = "친구 초대 현황 조회",
        description = """
                작성일자: 2026-03-21

                작성자: 성효빈

                ---

                ## Description

                나의 친구 초대 현황을 조회합니다. 초대한 친구 수, 받은 총 캐시, 누적 보너스 달성 현황을 포함합니다.

                ---

                ## Request

                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 시간 | "2026-03-21T12:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "친구 초대 현황 조회 성공" |

                ---

                ### Response > content

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | totalInvitedCount | number | 내가 초대한 친구 수 | 7 |
                | totalEarnedCash | number | 받은 총 캐시 | 4500 |
                | maxInviteCount | number | 최대 초대 가능 인원 | 20 |
                | tiers | array | 누적 보너스 단계 목록 | [ ... ] |

                ---

                ### Response > content > tiers[]

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | requiredCount | number | 달성 필요 초대 수 | 5 |
                | bonusAmount | number | 보너스 캐시 | 1000 |
                | achieved | boolean | 달성 여부 | true |
                | claimed | boolean | 수령 여부 | true |
                | claimable | boolean | 클레임 가능 여부 (달성 && 미수령) | false |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "친구 초대 현황 조회 성공",
                        content = @Content(
                                schema = @Schema(implementation = GetReferralStatusResponse.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-06-03T18:01:50.214036",
                                          "content": {
                                            "totalInvitedCount": 7,
                                            "totalEarnedCash": 4500,
                                            "maxInviteCount": 20,
                                            "tiers": [
                                              { "requiredCount": 5, "bonusAmount": 1000, "achieved": true, "claimed": true, "claimable": false },
                                              { "requiredCount": 10, "bonusAmount": 1500, "achieved": false, "claimed": false, "claimable": false },
                                              { "requiredCount": 20, "bonusAmount": 2000, "achieved": false, "claimed": false, "claimable": false }
                                            ]
                                          },
                                          "message": "친구 초대 현황 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetReferralStatusApiDocs {
}
