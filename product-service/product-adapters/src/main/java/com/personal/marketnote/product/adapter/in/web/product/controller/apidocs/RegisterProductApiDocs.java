package com.personal.marketnote.product.adapter.in.web.product.controller.apidocs;

import com.personal.marketnote.product.adapter.in.web.product.request.RegisterProductRequest;
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
        summary = "(판매자/관리자) 상품 등록",
        description = """
                작성일자: 2025-12-30
                
                작성자: 성효빈
                
                ---
                
                ## Description
                
                - 판매할 상품을 등록합니다.
                
                - 판매자 또는 관리자만 가능합니다.
                
                ---
                
                ## Request
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | sellerId | number | 판매자 회원 ID | Y | 1 |
                | name | string | 상품명 | Y | "스프링노트1" |
                | brandName | string | 브랜드명 | N | "노트왕" |
                | detail | string | 상품 설명 | N | "스프링노트1 설명" |
                | price | number | 상품 기본 판매 가격(원) | Y | 100000 |
                | discountPrice | number | 상품 할인 가격(원) | N | 90000 |
                | accumulatedPoint | number | 구매 시 적립 포인트 | Y | 1000 |
                | isFindAllOptions | boolean | 상품 목록 조회 시 옵션마다 개별 상품으로 조회 여부 | Y | true |
                | tags | array<string> | 상품 태그 목록 | Y | ["루테인", "아스타잔틴"] |
                | fulfillmentVendorGoods | object | 파스토 상품 등록 옵션 정보 | N | { ... } |
                
                ---
                
                ### Request > fulfillmentVendorGoods
                
                | **키** | **타입** | **설명** | **필수 여부** | **예시** |
                | --- | --- | --- | --- | --- |
                | godType | string | 상품유형(1:단일, 2:모음, 3:세트, 4:대표상품) | N | "1" |
                | giftDiv | string | 사은품구분(01:본품, 02:사은품, 03:부자재) | N | "01" |
                | godOptCd1 | string | 상품옵션코드1 | N | "" |
                | godOptCd2 | string | 상품옵션코드2 | N | "" |
                | invGodNmUseYn | string | 송장출력용 상품명 사용여부 (Y/N) | N | "N" |
                | invGodNm | string | 송장출력용 상품명 | N | "" |
                | supCd | string | 공급사코드 | N | "94388001" |
                | cateCd | string | 카테고리코드 | N | "A001" |
                | seasonCd | string | 계절상품 코드(0:공용, 1:S/S, 2:F/W) | N | "0" |
                | genderCd | string | 성별상품 코드(A:Unisex, M:men, W:woman, K:kids) | N | "A" |
                | makeYr | string | 연식 | N | "2025" |
                | godPr | string | 단가 | N | "10000" |
                | inPr | string | 공급가 | N | "8000" |
                | salPr | string | 판매가 | N | "9000" |
                | dealTemp | string | 취급온도 | N | "" |
                | pickFac | string | 피킹설비 | N | "" |
                | godBarcd | string | 상품바코드 | N | "" |
                | boxWeight | string | 내품BOX무게 | N | "" |
                | origin | string | 원산지 | N | "" |
                | distTermMgtYn | string | 유통기한관리여부 | N | "" |
                | useTermDay | string | 사용기한 | N | "" |
                | outCanDay | string | 출고가능일수 | N | "" |
                | inCanDay | string | 입고가능일수 | N | "" |
                | boxDiv | string | 출고박스 | N | "" |
                | bufGodYn | string | 완충상품여부(Y:사용,N:미사용,A:추가완충제) | N | "" |
                | loadingDirection | string | 출고박스 상품 적재 기준(NONE:관계없음, UP:세워서 적재) | N | "" |
                | subMate | string | 부자재코드(01:홍보물, 02:출력물, 03:쇼핑백, 04:포장지, 05:고객사테이프, 06:케이스, 07:단상자, 08:세트제작용 부자재, 09:출고용 패킹박스, 10:습자지, 11:고객사인박스) | N | "" |
                | useYn | string | 사용여부(Y/N) | N | "Y" |
                | safetyStock | string | 안전재고수량 | N | "" |
                | feeYn | string | 요금적용여부 | N | "" |
                | saleUnitQty | string | 상품판매단위 | N | "" |
                | cstGodImgUrl | string | 고객상품이미지URL | N | "" |
                | externalGodImgUrl | string | 상품이미지 URL | N | "" |
                
                ---
                
                ## Response
                
                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | statusCode | number | 상태 코드 | 201: 성공 / 400: 클라이언트 요청 오류 / 401: 인증 실패 / 403: 인가 실패 / 404: 리소스 조회 실패 / 409: 충돌 / 500: 그 외 |
                | code | string | 응답 코드 | "SUC01" / "BAD_REQUEST" / "UNAUTHORIZED" / "FORBIDDEN" / "NOT_FOUND" / "CONFLICT" / "INTERNAL_SERVER_ERROR" |
                | timestamp | string(datetime) | 응답 일시 | "2025-12-30T12:12:30.013" |
                | content | object | 응답 본문 | { ... } |
                | message | string | 처리 결과 | "상품 등록 성공" |
                
                ---
                
                ### Response > content

                | **키** | **타입** | **설명** | **예시** |
                | --- | --- | --- | --- |
                | id | number | 상품 ID | 1 |
                | productKey | string(uuid) | 상품 productKey | "01890d0a-1234-7000-89ab-0123456789ab" |
                """, security = {@SecurityRequirement(name = "bearer")},
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = RegisterProductRequest.class),
                        examples = @ExampleObject("""
                                {
                                    "sellerId": 1,
                                    "name": "스프링노트1",
                                    "brandName": "노트왕",
                                    "detail": "스프링노트1 설명",
                                    "price": 10000,
                                    "discountPrice": 9000,
                                    "accumulatedPoint": 1000,
                                    "isFindAllOptions": true,
                                    "tags": ["루테인", "아스타잔틴"]
                                }
                                """)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "상품 등록 성공",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 201,
                                          "code": "SUC01",
                                          "timestamp": "2025-12-30T12:12:30.013",
                                          "content": {
                                            "id": 1,
                                            "productKey": "01890d0a-1234-7000-89ab-0123456789ab"
                                          },
                                          "message": "상품 등록 성공"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "토큰 인증 실패",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 401,
                                          "code": "UNAUTHORIZED",
                                          "timestamp": "2025-12-30T12:12:30.013",
                                          "content": null,
                                          "message": "Invalid token"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "403",
                        description = "토큰 인가 실패",
                        content = @Content(
                                examples = @ExampleObject("""
                                        {
                                          "statusCode": 403,
                                          "code": "FORBIDDEN",
                                          "timestamp": "2025-12-30T12:12:30.013",
                                          "content": null,
                                          "message": "Access Denied"
                                        }
                                        """)
                        )
                ),
                @ApiResponse(
                        responseCode = "502",
                        description = "외부 서비스 통신 실패",
                        content = @Content(
                                examples = {
                                        @ExampleObject(
                                                name = "commerce",
                                                value = """
                                                        {
                                                          "statusCode": 502,
                                                          "code": "BAD_GATEWAY",
                                                          "timestamp": "2026-01-06T16:36:28.920366",
                                                          "content": null,
                                                          "message": "Commerce 서비스 통신 중 오류가 발생했습니다."
                                                        }
                                                        """
                                        ),
                                        @ExampleObject(
                                                name = "fulfillment",
                                                value = """
                                                        {
                                                          "statusCode": 502,
                                                          "code": "BAD_GATEWAY",
                                                          "timestamp": "2026-01-06T16:36:28.920366",
                                                          "content": null,
                                                          "message": "풀필먼트 서비스 통신 중 오류가 발생했습니다."
                                                        }
                                                        """
                                        )
                                }
                        )
                )
        })
public @interface RegisterProductApiDocs {
}
