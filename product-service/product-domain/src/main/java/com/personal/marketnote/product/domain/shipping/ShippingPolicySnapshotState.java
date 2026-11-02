package com.personal.marketnote.product.domain.shipping;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class ShippingPolicySnapshotState {
    private Long id;
    private Long sellerId;
    private String deliveryCompany;
    private Long shippingFee;
    private Long freeShippingThreshold;
    private Long jejuSurcharge;
    private Long islandSurcharge;
    private EntityStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
