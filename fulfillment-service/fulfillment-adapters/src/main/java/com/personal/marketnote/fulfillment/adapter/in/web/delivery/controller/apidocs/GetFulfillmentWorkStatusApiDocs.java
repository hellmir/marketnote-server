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
        summary = "풀필먼트 작업 상태 조회",
        description = "주문 ID로 풀필먼트 작업 상태를 조회합니다. 서비스 간 통신용 API입니다.",
        parameters = {
                @Parameter(name = "order-id", description = "주문 ID", required = true)
        },
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "풀필먼트 작업 상태 조회 성공",
                        content = @Content(schema = @Schema(implementation = BaseResponse.class))
                )
        }
)
public @interface GetFulfillmentWorkStatusApiDocs {
}
