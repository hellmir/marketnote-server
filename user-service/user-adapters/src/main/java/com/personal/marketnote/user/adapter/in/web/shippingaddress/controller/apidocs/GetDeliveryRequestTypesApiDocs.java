package com.personal.marketnote.user.adapter.in.web.shippingaddress.controller.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Operation(
        summary = "배송 요청사항 목록 조회",
        description = """
                작성일자: 2026-03-19

                작성자: 성효빈

                ---

                ## Description
                - 배송 요청사항 타입 목록을 조회합니다.

                - 배송지 등록/수정 화면에서 배송 요청사항 선택지를 표시하기 위해 사용합니다.

                ---

                ## Request

                파라미터 없음

                ---

                ## Response

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 200: 성공 |
                | code | string | 응답 코드 | "SUC01" |
                | timestamp | string(datetime) | 응답 일시 | "2026-03-19T12:00:00.000" |
                | content | array | 응답 본문 | [ ... ] |
                | message | string | 처리 결과 | "배송 요청사항 목록 조회 성공" |

                ---

                ### Response > content[]

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | type | string | 배송 요청사항 타입 | "LEAVE_AT_DOOR" |
                | description | string | 배송 요청사항 설명 | "문 앞에 놓아주세요" |
                """,
        security = {@SecurityRequirement(name = "bearer")},
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "배송 요청사항 목록 조회 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 200,
                                          "code": "SUC01",
                                          "timestamp": "2026-03-19T12:00:00.000000",
                                          "content": [
                                            { "type": "NONE", "description": "선택 안 함" },
                                            { "type": "LEAVE_AT_DOOR", "description": "문 앞에 놓아주세요" },
                                            { "type": "RECEIVE_OR_LEAVE_AT_DOOR", "description": "직접 받고 부재시 문 앞에 놓아 주세요" },
                                            { "type": "LEAVE_AT_SECURITY", "description": "경비실에 맡겨주세요" },
                                            { "type": "LEAVE_AT_DELIVERY_BOX", "description": "택배함에 넣어주세요" },
                                            { "type": "CUSTOM", "description": "직접 입력" }
                                          ],
                                          "message": "배송 요청사항 목록 조회 성공"
                                        }
                                        """)
                        )
                )
        }
)
public @interface GetDeliveryRequestTypesApiDocs {
}
