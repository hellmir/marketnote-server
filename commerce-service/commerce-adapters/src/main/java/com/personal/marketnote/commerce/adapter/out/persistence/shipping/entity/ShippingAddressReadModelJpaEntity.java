package com.personal.marketnote.commerce.adapter.out.persistence.shipping.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

@Entity
@Table(
        name = "shipping_address_read_models",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_shipping_address_read_model_shipping_address_id",
                columnNames = "shipping_address_id"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ShippingAddressReadModelJpaEntity extends BaseGeneralEntity {

    @Column(name = "shipping_address_id", nullable = false, unique = true)
    private Long shippingAddressId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "recipient_name", nullable = false, length = 31)
    private String recipientName;

    @Column(name = "recipient_phone_number", nullable = false, length = 15)
    private String recipientPhoneNumber;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "address_detail", nullable = false)
    private String addressDetail;

    public static ShippingAddressReadModelJpaEntity of(
            Long shippingAddressId, Long userId,
            String recipientName, String recipientPhoneNumber,
            String address, String addressDetail
    ) {
        return ShippingAddressReadModelJpaEntity.builder()
                .shippingAddressId(shippingAddressId)
                .userId(userId)
                .recipientName(recipientName)
                .recipientPhoneNumber(recipientPhoneNumber)
                .address(address)
                .addressDetail(addressDetail)
                .build();
    }

    public void updateFrom(
            String recipientName, String recipientPhoneNumber,
            String address, String addressDetail
    ) {
        this.recipientName = recipientName;
        this.recipientPhoneNumber = recipientPhoneNumber;
        this.address = address;
        this.addressDetail = addressDetail;
        activate();
    }

    public void markInactive() {
        deactivate();
    }
}
