package com.personal.marketnote.commerce.domain.order;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RequiredArgsConstructor
public enum OrderPeriod {
    ONE_MONTH(1),
    THREE_MONTHS(3),
    SIX_MONTHS(6),
    ONE_YEAR(12),
    ALL(null);

    private final Integer months;

    public LocalDateTime startDate(LocalDate now) {
        if (FormatValidator.hasNoValue(months)) {
            return null;
        }

        return LocalDateTime.of(now.minusMonths(months), LocalTime.MIDNIGHT);
    }
}
