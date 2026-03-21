package com.personal.marketnote.reward.exception;

import java.time.LocalDate;

public class InvalidPointHistoryDateRangeException extends IllegalArgumentException {
    public InvalidPointHistoryDateRangeException(LocalDate startDate, LocalDate endDate) {
        super("ERR_POINT_HISTORY_01::조회 시작일이 종료일보다 늦을 수 없습니다. startDate=" + startDate + ", endDate=" + endDate);
    }
}
