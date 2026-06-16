package com.personal.marketnote.fulfillment.adapter.in.web.delivery.controller.apidocs;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
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
        summary = "배송 상태 조회",
        description = "주문 ID로 ShippingTracker 기반 배송 상태를 조회합니다. 서비스 간 통신용 API입니다. 응답에 취소 가능 여부(cancellable), 송장번호, 택배사 코드, 마지막 폴링 시각이 포함됩니다.",
        parameters = {
                @Parameter(name = "order-id", description = "주문 ID", required = true)
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "배송 상태 조회 성공",
                        content = @Content(schema = @Schema(implementation = BaseResponse.class))
                )
        }
)
public @interface GetShippingStatusApiDocs {
}
