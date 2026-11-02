package com.personal.marketnote.reward.adapter.in.web.point.apidocs;

import com.personal.marketnote.reward.adapter.in.web.point.response.ClaimReferralBonusResponse;
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
        summary = "친구 초대 누적 보너스 클레임",
        description = """
                작성일자: 2026-06-03
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                달성한 누적 보너스 티어의 보너스를 클레임(수령)합니다.
                티어 미달성 시 또는 이미 수령한 보너스를 중복 요청하면 에러를 반환합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | tier | string | 보너스 티어 (TIER_1, TIER_2, TIER_3) | 필수 | "TIER_1" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | HTTP 상태 코드 | 200 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 시간 | "2026-06-03T12:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "보너스 클레임 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | requiredCount | number | 달성 필요 초대 수 | 5 |
                | bonusAmount | number | 적립된 보너스 캐시 | 1000 |
                | reason | string | 적립 사유 | "친구 초대 누적 보너스 (5명)" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "보너스 클레임 성공",
                        content = @Content(
                                schema = @Schema(implementation = ClaimReferralBonusResponse.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-06-03T18:01:50.214036",
                                          "content": {
                                            "requiredCount": 5,
                                            "bonusAmount": 1000,
                                            "reason": "친구 초대 누적 보너스 (5명)"
                                          },
                                          "message": "보너스 클레임 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface ClaimReferralBonusApiDocs {
}
