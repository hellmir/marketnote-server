package com.personal.marketnote.user.adapter.in.web.shippingaddress.request;

import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.common.utility.RegularExpressionConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateDeliveryRequestRequest {

    @Schema(name = "deliveryRequestType", description = "배송 요청사항 타입", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private DeliveryRequestType deliveryRequestType;

    @Schema(name = "deliveryRequestMessage", description = "배송 요청사항 직접입력 메시지 (최대 60자, CUSTOM 타입일 때 필수)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 60, message = "배송 요청사항 메시지는 최대 60자까지 입력할 수 있습니다.")
    @Pattern(regexp = RegularExpressionConstant.NO_HTML_TAG_PATTERN, message = "배송 요청사항 메시지에 허용되지 않는 문자가 포함되어 있습니다.")
    private String deliveryRequestMessage;
}
