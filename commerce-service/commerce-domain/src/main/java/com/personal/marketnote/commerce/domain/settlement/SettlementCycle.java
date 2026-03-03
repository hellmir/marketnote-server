package com.personal.marketnote.commerce.domain.settlement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettlementCycle {
    MONTHLY("월별 정산"),
    BIWEEKLY("격주 정산"),
    WEEKLY("주별 정산");

    private final String description;
}
