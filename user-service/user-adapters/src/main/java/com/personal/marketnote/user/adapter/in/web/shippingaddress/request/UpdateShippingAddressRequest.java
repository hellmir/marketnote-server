package com.personal.marketnote.user.adapter.in.web.shippingaddress.request;

import com.personal.marketnote.user.domain.shippingaddress.DeliveryRequestType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class UpdateShippingAddressRequest {

    @Schema(name = "address", description = "도로명 주소", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "주소는 필수값입니다.")
    private String address;

    @Schema(name = "addressDetail", description = "상세주소", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "상세주소는 필수값입니다.")
    private String addressDetail;

    @Schema(name = "companyName", description = "회사명 (COMPANY 타입일 때 필수)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String companyName;

    @Schema(name = "addressAlias", description = "주소 별명 (OTHER 타입일 때 필수)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String addressAlias;

    @Schema(name = "recipientName", description = "받는 분", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "받는 분은 필수값입니다.")
    private String recipientName;

    @Schema(name = "recipientPhoneNumber", description = "휴대폰 번호", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "휴대폰 번호는 필수값입니다.")
    private String recipientPhoneNumber;

    @Schema(name = "deliveryRequestType", description = "배송 요청사항 타입", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private DeliveryRequestType deliveryRequestType;

    @Schema(name = "deliveryRequestMessage", description = "배송 요청사항 직접입력 메시지 (최대 30자, CUSTOM 타입일 때 필수)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String deliveryRequestMessage;
}
