package com.personal.marketnote.common.domain.calendar;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum Month {
    JANUARY(1),
    FEBRUARY(2),
    MARCH(3),
    APRIL(4),
    MAY(5),
    JUNE(6),
    JULY(7),
    AUGUST(8),
    SEPTEMBER(9),
    OCTOBER(10),
    NOVEMBER(11),
    DECEMBER(12);

    private final int value;

    public static Month from(int month) {
        return Arrays.stream(values())
                .filter(it -> it.value == month)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 월입니다. month: " + month));
    }
}

