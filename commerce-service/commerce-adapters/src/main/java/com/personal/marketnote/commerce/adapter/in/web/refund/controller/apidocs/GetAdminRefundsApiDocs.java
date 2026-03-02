package com.personal.marketnote.commerce.adapter.in.web.refund.controller.apidocs;

import com.personal.marketnote.commerce.adapter.in.web.refund.response.GetAdminRefundResponse;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * 관리자 환불 목록 조회 API 문서 애노테이션.
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "주문별 환불 목록 조회",
        description = "주문 ID에 해당하는 환불 상세 목록을 조회합니다.",
        security = @SecurityRequirement(name = "bearer"),
        parameters = {
                @Parameter(name = "order-id", description = "주문 ID", required = true, example = "1")
        }
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "환불 목록 조회 성공",
                content = @Content(schema = @Schema(implementation = BaseResponse.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "인증 실패"
        ),
        @ApiResponse(
                responseCode = "403",
                description = "관리자 권한 없음"
        )
})
public @interface GetAdminRefundsApiDocs {
}
