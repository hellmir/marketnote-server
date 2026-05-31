package com.personal.marketnote.fulfillment.adapter.in.web.delivery.controller.apidocs;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.delivery.request.RegisterInternalReturnDeliveryRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "풀필먼트 반품 등록",
        description = "주문 ID와 반품 정보를 기반으로 파스토에 반품을 등록합니다. 서비스 간 통신용 내부 API입니다.",
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = RegisterInternalReturnDeliveryRequest.class)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "풀필먼트 반품 등록 성공",
                        content = @Content(schema = @Schema(implementation = BaseResponse.class))
                )
        }
)
public @interface RegisterInternalReturnDeliveryApiDocs {
}
