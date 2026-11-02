package com.personal.marketnote.reward.domain.point;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserPointChangeType {
    ACCRUAL("적립"),
    DEDUCTION("차감");

    private final String description;

    public boolean isAccrual() {
        return this == ACCRUAL;
    }

    public boolean isDeduction() {
        return this == DEDUCTION;
    }
}
