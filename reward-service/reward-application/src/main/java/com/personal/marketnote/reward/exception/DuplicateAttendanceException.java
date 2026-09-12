package com.personal.marketnote.reward.exception;

import java.time.LocalDate;

public class DuplicateAttendanceException extends RuntimeException {
    public DuplicateAttendanceException(Long userAttendanceId, LocalDate attendedDate) {
        super("ERR_ATTENDANCE_01::이미 출석이 등록된 날짜입니다. userAttendanceId=" + userAttendanceId + ", attendedDate=" + attendedDate);
    }
}
