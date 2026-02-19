package com.personal.marketnote.user.adapter.in.web.shippingaddress.request;

import com.personal.marketnote.user.domain.shippingaddress.DeliveryRequestType;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RegisterShippingAddressRequest {

    @Schema(name = "addressType", description = "배송지 타입 (HOME, COMPANY, OTHER)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "배송지 타입은 필수값입니다.")
    private ShippingAddressType addressType;

    @Schema(name = "address", description = "도로명 주소 (최대 255자)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "주소는 필수값입니다.")
    @Size(max = 255, message = "주소는 최대 255자까지 입력할 수 있습니다.")
    private String address;

    @Schema(name = "addressDetail", description = "상세주소 (최대 255자)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "상세주소는 필수값입니다.")
    @Size(max = 255, message = "상세주소는 최대 255자까지 입력할 수 있습니다.")
    private String addressDetail;

    @Schema(name = "companyName", description = "회사명 (COMPANY 타입일 때 필수, 최대 63자)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 63, message = "회사명은 최대 63자까지 입력할 수 있습니다.")
    private String companyName;

    @Schema(name = "addressAlias", description = "주소 별명 (OTHER 타입일 때 필수, 최대 31자)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 31, message = "주소 별명은 최대 31자까지 입력할 수 있습니다.")
    private String addressAlias;

    @Schema(name = "recipientName", description = "받는 분 (최대 31자)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "받는 분은 필수값입니다.")
    @Size(max = 31, message = "받는 분은 최대 31자까지 입력할 수 있습니다.")
    private String recipientName;

    @Schema(name = "recipientPhoneNumber", description = "휴대폰 번호 (최대 15자)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "휴대폰 번호는 필수값입니다.")
    @Size(max = 15, message = "휴대폰 번호는 최대 15자까지 입력할 수 있습니다.")
    private String recipientPhoneNumber;

    @Schema(name = "deliveryRequestType", description = "배송 요청사항 타입", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private DeliveryRequestType deliveryRequestType;

    @Schema(name = "deliveryRequestMessage", description = "배송 요청사항 직접입력 메시지 (최대 30자, CUSTOM 타입일 때 필수)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 30, message = "배송 요청사항 메시지는 최대 30자까지 입력할 수 있습니다.")
    private String deliveryRequestMessage;

    @Schema(name = "isDefault", description = "기본 배송지 여부", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean isDefault;
}
