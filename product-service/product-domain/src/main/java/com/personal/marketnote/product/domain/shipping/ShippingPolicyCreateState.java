package com.personal.marketnote.product.domain.shipping;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class ShippingPolicyCreateState {
    private Long sellerId;
    private String deliveryCompany;
    private Long shippingFee;
    private Long freeShippingThreshold;
}
