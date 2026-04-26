package com.personal.marketnote.user.adapter.out.persistence.shippingaddress.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressRegionType;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shipping_addresses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ShippingAddressJpaEntity extends BaseGeneralEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 15)
    private ShippingAddressType addressType;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "address_detail", nullable = false)
    private String addressDetail;

    @Column(name = "company_name", length = 63)
    private String companyName;

    @Column(name = "address_alias")
    private String addressAlias;

    @Column(name = "recipient_name", nullable = false, length = 31)
    private String recipientName;

    @Column(name = "recipient_phone_number", nullable = false, length = 15)
    private String recipientPhoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_request_type", nullable = false, length = 31)
    private DeliveryRequestType deliveryRequestType;

    @Column(name = "delivery_request_message", length = 60)
    private String deliveryRequestMessage;

    @Column(name = "is_default", nullable = false, columnDefinition = "boolean default false")
    private Boolean isDefault;

    @Enumerated(EnumType.STRING)
    @Column(name = "region_type", nullable = false, length = 15)
    private ShippingAddressRegionType regionType;

    public void updateFrom(ShippingAddress shippingAddress) {
        if (shippingAddress.isInactive()) {
            deactivate();
        }
        this.address = shippingAddress.getAddress();
        this.addressDetail = shippingAddress.getAddressDetail();
        this.companyName = shippingAddress.getCompanyName();
        this.addressAlias = shippingAddress.getAddressAlias();
        this.recipientName = shippingAddress.getRecipientName();
        this.recipientPhoneNumber = shippingAddress.getRecipientPhoneNumber();
        this.deliveryRequestType = shippingAddress.getDeliveryRequestType();
        this.deliveryRequestMessage = shippingAddress.getDeliveryRequestMessage();
        this.isDefault = shippingAddress.isDefault();
        this.regionType = shippingAddress.getRegionType();
    }

    public static ShippingAddressJpaEntity from(ShippingAddress shippingAddress) {
        return ShippingAddressJpaEntity.builder()
                .userId(shippingAddress.getUserId())
                .addressType(shippingAddress.getAddressType())
                .address(shippingAddress.getAddress())
                .addressDetail(shippingAddress.getAddressDetail())
                .companyName(shippingAddress.getCompanyName())
                .addressAlias(shippingAddress.getAddressAlias())
                .recipientName(shippingAddress.getRecipientName())
                .recipientPhoneNumber(shippingAddress.getRecipientPhoneNumber())
                .deliveryRequestType(shippingAddress.getDeliveryRequestType())
                .deliveryRequestMessage(shippingAddress.getDeliveryRequestMessage())
                .isDefault(shippingAddress.isDefault())
                .regionType(shippingAddress.getRegionType())
                .build();
    }
}
