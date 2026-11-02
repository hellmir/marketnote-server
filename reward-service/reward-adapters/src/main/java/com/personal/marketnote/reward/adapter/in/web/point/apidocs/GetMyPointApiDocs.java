package com.personal.marketnote.reward.adapter.in.web.point.apidocs;

import com.personal.marketnote.reward.adapter.in.web.point.response.GetMyPointReponse;
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
        summary = "나의 포인트 정보 조회",
        description = """
                작성일자: 2026-01-18
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                회원의 포인트/적립 예정/소멸 예정 포인트 정보를 조회합니다.
                
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
                | timestamp | string(datetime) | 응답 시간 | "2026-01-18T12:00:00.000" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "나의 포인트 정보 조회 성공" |
                
                ---
                
                ### Response > content
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | userId | number | 회원 ID | 100 |
                | amount | number | 포인트 | 1500 |
                | addExpectedAmount | number | 적립 예정 포인트 | 500 |
                | expireExpectedAmount | number | 소멸 예정 포인트 | 100 |
                | createdAt | string(datetime) | 생성 일시 | "2026-01-16T11:00:00" |
                | modifiedAt | string(datetime) | 수정 일시 | "2026-01-17T11:00:00" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "나의 포인트 조회 성공",
                        content = @Content(
                                schema = @Schema(implementation = GetMyPointReponse.class),
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-01-18T18:01:50.214036",
                                          "content": {
                                            "userId": 17,
                                            "amount": 2000,
                                            "addExpectedAmount": 0,
                                            "expireExpectedAmount": 0,
                                            "createdAt": "2026-01-17T05:27:21.418776",
                                            "modifiedAt": "2026-01-17T14:28:00.973419"
                                          },
                                          "message": "나의 포인트 정보 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetMyPointApiDocs {
}
