package com.personal.marketnote.common.domain.calendar;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum Year {
    Y2025(2025),
    Y2026(2026),
    Y2027(2027),
    Y2028(2028),
    Y2029(2029),
    Y2030(2030),
    Y2031(2031),
    Y2032(2032),
    Y2033(2033),
    Y2034(2034),
    Y2035(2035),
    Y2036(2036),
    Y2037(2037),
    Y2038(2038),
    Y2039(2039),
    Y2040(2040);

    private final int value;

    public static Year from(int year) {
        return Arrays.stream(values())
                .filter(it -> it.value == year)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 연도입니다. year: " + year));
    }
}

