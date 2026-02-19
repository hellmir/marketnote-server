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
        ShippingAddress shippingAddress = ShippingAddress.builder()
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

    public static ShippingAddress from(ShippingAddressSnapshotState state) {
        return ShippingAddress.builder()
                .id(state.getId())
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
    }

    public static ShippingAddress referenceOf(Long id) {
        return ShippingAddress.builder()
                .id(id)
                .build();
    }

    public void setAsDefault() {
        this.isDefault = true;
    }

    public void unsetAsDefault() {
        this.isDefault = false;
    }

    public void delete() {
        if (addressType == ShippingAddressType.HOME) {
            throw new IllegalArgumentException("집 배송지는 삭제할 수 없습니다.");
        }
        if (isDefault) {
            throw new IllegalArgumentException("기본 배송지는 삭제할 수 없습니다. 다른 배송지를 기본으로 설정한 후 삭제해주세요.");
        }
        deactivate();
    }

    public void update(
            String address,
            String addressDetail,
            String companyName,
            String addressAlias,
            String recipientName,
            String recipientPhoneNumber,
            DeliveryRequestType deliveryRequestType,
            String deliveryRequestMessage
    ) {
        this.address = address;
        this.addressDetail = addressDetail;
        this.companyName = companyName;
        this.addressAlias = addressAlias;
        this.recipientName = recipientName;
        this.recipientPhoneNumber = recipientPhoneNumber;
        this.deliveryRequestType = deliveryRequestType;
        this.deliveryRequestMessage = deliveryRequestMessage;
        validate();
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
