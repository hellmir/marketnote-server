package com.personal.marketnote.fulfillment.adapter.in.web.delivery.controller.apidocs;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.delivery.request.CancelInternalFulfillmentDeliveryRequest;
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
        summary = "풀필먼트 출고 취소",
        description = "주문 ID로 풀필먼트 출고를 취소합니다. 서비스 간 통신용 내부 API입니다.",
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        schema = @Schema(implementation = CancelInternalFulfillmentDeliveryRequest.class)
                )
        ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "풀필먼트 출고 취소 처리 완료",
                        content = @Content(schema = @Schema(implementation = BaseResponse.class))
                )
        }
)
public @interface CancelInternalFulfillmentDeliveryApiDocs {
}
