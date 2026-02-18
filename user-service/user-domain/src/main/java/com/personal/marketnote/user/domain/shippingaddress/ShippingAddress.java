package com.personal.marketnote.user.domain.shippingaddress;

import com.personal.marketnote.common.domain.BaseDomain;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ShippingAddress extends BaseDomain {
    private Long id;
    private Long userId;
    private ShippingAddressType addressType;
    private String address;
    private String addressDetail;
    private String companyName;
    private String addressAlias;
    private String recipientName;
    private String recipientPhoneNumber;
    private DeliveryRequestType deliveryRequestType;
    private String deliveryRequestMessage;
    private boolean isDefault;

    public static ShippingAddress from(ShippingAddressCreateState state) {
        return from(state, null);
    }

    public static ShippingAddress from(ShippingAddressCreateState state, Long id) {
        ShippingAddress shippingAddress = ShippingAddress.builder()
                .id(id)
                .userId(state.getUserId())
                .addressType(state.getAddressType())
                .address(state.getAddress())
                .addressDetail(state.getAddressDetail())
                .companyName(state.getCompanyName())
                .addressAlias(state.getAddressAlias())
                .recipientName(state.getRecipientName())
                .recipientPhoneNumber(state.getRecipientPhoneNumber())
                .deliveryRequestType(state.getDeliveryRequestType())
                .deliveryRequestMessage(state.getDeliveryRequestMessage())
                .isDefault(state.isDefault())
                .build();

        shippingAddress.validate();
        return shippingAddress;
    }

    public static ShippingAddress referenceOf(Long id) {
        return ShippingAddress.builder()
                .id(id)
                .build();
    }

    private void validate() {
        if (addressType == ShippingAddressType.COMPANY && FormatValidator.hasNoValue(companyName)) {
            throw new IllegalArgumentException("회사 배송지에는 회사명이 필수입니다.");
        }

        if (addressType == ShippingAddressType.OTHER && FormatValidator.hasNoValue(addressAlias)) {
            throw new IllegalArgumentException("기타 배송지에는 주소 별명이 필수입니다.");
        }

        if (FormatValidator.hasValue(deliveryRequestType)
                && deliveryRequestType.isCustom()
                && FormatValidator.hasNoValue(deliveryRequestMessage)) {
            throw new IllegalArgumentException("직접입력 선택 시 배송 요청사항 메시지는 필수입니다.");
        }

        if (FormatValidator.hasValue(deliveryRequestMessage) && deliveryRequestMessage.length() > 30) {
            throw new IllegalArgumentException("배송 요청사항 메시지는 최대 30자까지 입력할 수 있습니다.");
        }
    }
}
