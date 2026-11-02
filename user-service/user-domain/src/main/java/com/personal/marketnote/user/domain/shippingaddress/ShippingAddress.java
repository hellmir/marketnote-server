package com.personal.marketnote.user.domain.shippingaddress;

import com.personal.marketnote.common.domain.BaseDomain;
import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.common.domain.deliveryrequestmessage.DeliveryRequestMessage;
import com.personal.marketnote.common.domain.phonenumber.PhoneNumber;
import com.personal.marketnote.common.domain.recipientname.RecipientName;
import com.personal.marketnote.common.utility.FormatValidator;
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
    private RecipientName recipientName;
    private PhoneNumber recipientPhoneNumber;
    private DeliveryRequestType deliveryRequestType;
    private DeliveryRequestMessage deliveryRequestMessage;
    private boolean isDefault;
    private ShippingAddressRegionType regionType;

    public static ShippingAddress from(ShippingAddressCreateState state) {
        ShippingAddress shippingAddress = ShippingAddress.builder()
                .userId(state.getUserId())
                .addressType(state.getAddressType())
                .address(state.getAddress())
                .addressDetail(state.getAddressDetail())
                .companyName(state.getCompanyName())
                .addressAlias(state.getAddressAlias())
                .recipientName(RecipientName.of(state.getRecipientName()))
                .recipientPhoneNumber(PhoneNumber.of(state.getRecipientPhoneNumber()))
                .deliveryRequestType(state.getDeliveryRequestType())
                .deliveryRequestMessage(resolveDeliveryRequestMessage(state.getDeliveryRequestType(), state.getDeliveryRequestMessage()))
                .isDefault(state.isDefault())
                .regionType(state.getRegionType())
                .build();

        shippingAddress.validateCompanyName();
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
                .recipientName(RecipientName.of(state.getRecipientName()))
                .recipientPhoneNumber(PhoneNumber.of(state.getRecipientPhoneNumber()))
                .deliveryRequestType(state.getDeliveryRequestType())
                .deliveryRequestMessage(toDeliveryRequestMessageOrNull(state.getDeliveryRequestMessage()))
                .isDefault(state.isDefault())
                .regionType(state.getRegionType())
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

    public void assignRegionType(ShippingAddressRegionType regionType) {
        if (FormatValidator.hasNoValue(regionType)) {
            this.regionType = ShippingAddressRegionType.NORMAL;
            return;
        }
        this.regionType = regionType;
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
            RecipientName recipientName,
            PhoneNumber recipientPhoneNumber,
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
        this.deliveryRequestMessage = resolveDeliveryRequestMessage(deliveryRequestType, deliveryRequestMessage);
        validateCompanyName();
    }

    public void updateDeliveryRequest(DeliveryRequestType deliveryRequestType, String deliveryRequestMessage) {
        this.deliveryRequestType = deliveryRequestType;
        this.deliveryRequestMessage = resolveDeliveryRequestMessage(deliveryRequestType, deliveryRequestMessage);
    }

    private void validateCompanyName() {
        if (addressType == ShippingAddressType.COMPANY && FormatValidator.hasNoValue(companyName)) {
            throw new ShippingAddressCompanyNameNoValueException();
        }
    }

    private static DeliveryRequestMessage resolveDeliveryRequestMessage(DeliveryRequestType deliveryRequestType, String deliveryRequestMessage) {
        if (FormatValidator.hasNoValue(deliveryRequestType) || !deliveryRequestType.isCustom()) {
            return null;
        }
        return DeliveryRequestMessage.of(deliveryRequestMessage);
    }

    private static DeliveryRequestMessage toDeliveryRequestMessageOrNull(String deliveryRequestMessage) {
        if (FormatValidator.hasNoValue(deliveryRequestMessage)) {
            return null;
        }
        return DeliveryRequestMessage.of(deliveryRequestMessage);
    }
}
