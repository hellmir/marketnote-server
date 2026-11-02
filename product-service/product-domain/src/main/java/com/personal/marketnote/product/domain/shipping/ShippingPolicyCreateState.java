package com.personal.marketnote.product.domain.shipping;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class ShippingPolicyCreateState {
    private Long sellerId;
    private String deliveryCompany;
    private Long shippingFee;
    private Long freeShippingThreshold;
    private Long jejuSurcharge;
    private Long islandSurcharge;
}
