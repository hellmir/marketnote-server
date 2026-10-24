package com.personal.marketnote.commerce.domain.returnshipping;

import com.personal.marketnote.common.domain.money.Money;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReturnShippingFeeContextCreateState {
    private final FaultType faultType;
    private final InitialShippingType initialShippingType;
    private final ReturnType returnType;
    private final Money remainingAmount;
    private final Money freeShippingThreshold;
    private final Money shippingFee;
    private final Money regionSurcharge;
}
