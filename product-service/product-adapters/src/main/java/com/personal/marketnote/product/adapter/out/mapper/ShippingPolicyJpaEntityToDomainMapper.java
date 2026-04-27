package com.personal.marketnote.product.adapter.out.mapper;

import com.personal.marketnote.product.adapter.out.persistence.shipping.entity.ShippingPolicyJpaEntity;
import com.personal.marketnote.product.domain.shipping.ShippingPolicy;
import com.personal.marketnote.product.domain.shipping.ShippingPolicySnapshotState;

import java.util.Optional;

public class ShippingPolicyJpaEntityToDomainMapper {

    private ShippingPolicyJpaEntityToDomainMapper() {
    }

    public static Optional<ShippingPolicy> mapToDomain(ShippingPolicyJpaEntity entity) {
        return Optional.ofNullable(entity)
                .map(e -> ShippingPolicy.from(
                        ShippingPolicySnapshotState.builder()
                                .id(e.getId())
                                .sellerId(e.getSellerId())
                                .deliveryCompany(e.getDeliveryCompany())
                                .shippingFee(e.getShippingFee())
                                .freeShippingThreshold(e.getFreeShippingThreshold())
                                .status(e.getStatus())
                                .createdAt(e.getCreatedAt())
                                .modifiedAt(e.getModifiedAt())
                                .build()
                ));
    }
}
