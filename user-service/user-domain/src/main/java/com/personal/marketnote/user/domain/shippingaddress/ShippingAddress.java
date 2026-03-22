package com.personal.marketnote.user.domain.shippingaddress;

import com.personal.marketnote.common.domain.BaseDomain;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.domain.shippingaddress.exception.DeliveryRequestMessageNoValueException;
import com.personal.marketnote.user.domain.shippingaddress.exception.InvalidDeliveryRequestMessageLengthException;
import com.personal.marketnote.user.domain.shippingaddress.exception.InvalidShippingAddressDeletionException;
import com.personal.marketnote.user.domain.shippingaddress.exception.ShippingAddressCompanyNameNoValueException;
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
            throw new InvalidShippingAddressDeletionException("집 배송지는 삭제할 수 없습니다.");
        }
        if (isDefault) {
            throw new InvalidShippingAddressDeletionException("기본 배송지는 삭제할 수 없습니다. 다른 배송지를 기본으로 설정한 후 삭제해주세요.");
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
            throw new ShippingAddressCompanyNameNoValueException();
        }

        if (FormatValidator.hasNoValue(deliveryRequestType) || !deliveryRequestType.isCustom()) {
            this.deliveryRequestMessage = null;
            return;
        }

        if (FormatValidator.hasNoValue(deliveryRequestMessage)) {
            throw new DeliveryRequestMessageNoValueException();
        }

        if (deliveryRequestMessage.length() > 60) {
            throw new InvalidDeliveryRequestMessageLengthException();
        }
    }
}
