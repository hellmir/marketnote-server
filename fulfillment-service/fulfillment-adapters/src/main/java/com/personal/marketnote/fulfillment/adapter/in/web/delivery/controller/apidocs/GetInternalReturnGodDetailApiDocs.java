package com.personal.marketnote.fulfillment.adapter.in.web.delivery.controller.apidocs;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.delivery.response.GetInternalReturnGodDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "(내부) 반품 완료 상품 상세 목록 조회",
        description = "서비스 간 통신용 반품 완료 상품 상세 목록을 조회합니다.",
        parameters = {
                @Parameter(name = "return-slip-numbers", description = "반품 요청번호 (콤마 구분)", required = true)
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "조회 성공",
                        content = @Content(schema = @Schema(implementation = BaseResponse.class))
                )
        }
)
public @interface GetInternalReturnGodDetailApiDocs {
}
