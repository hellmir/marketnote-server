package com.personal.marketnote.reward.domain.point;

import com.personal.marketnote.common.utility.FormatConverter;
import lombok.Getter;

@Getter
public enum UserPointHistoryFilter {
    ALL("전체", 0),
    ACCRUAL("적립", 1),
    DEDUCTION("사용", -1);

    private final String description;
    private final String camelCaseValue;
    private final int amountFilterValue;

    UserPointHistoryFilter(String description, int amountFilterValue) {
        this.description = description;
        this.camelCaseValue = FormatConverter.snakeToCamel(name());
        this.amountFilterValue = amountFilterValue;
    }

    public boolean isAll() {
        return this == ALL;
    }

    public boolean isAccrual() {
        return this == ACCRUAL;
    }

    public boolean isDeduction() {
        return this == DEDUCTION;
    }
}

