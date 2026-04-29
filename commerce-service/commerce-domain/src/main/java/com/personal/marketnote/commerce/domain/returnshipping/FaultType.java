package com.personal.marketnote.commerce.domain.returnshipping;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FaultType {
    BUYER("고객 귀책"),
    SELLER("판매자 귀책");

    private final String description;

    public boolean isBuyer() {
        return this == BUYER;
    }

    public boolean isSeller() {
        return this == SELLER;
    }
}
