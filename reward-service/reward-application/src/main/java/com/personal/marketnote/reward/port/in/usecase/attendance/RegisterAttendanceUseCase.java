package com.personal.marketnote.reward.port.in.usecase.attendance;

import com.personal.marketnote.reward.port.in.command.attendance.RegisterAttendanceCommand;
import com.personal.marketnote.reward.port.in.result.attendance.RegisterAttendanceResult;

/**
 * 출석 등록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-20
 * @Description 출석 등록 기능을 제공합니다.
 */
public interface RegisterAttendanceUseCase {
    /**
     * @param command 출석 등록 커맨드
     * @return 출석 등록 결과 {@link RegisterAttendanceResult}
     * @Date 2026-01-20
     * @Author 성효빈
     * @Description 출석을 등록합니다.
     */
    RegisterAttendanceResult register(RegisterAttendanceCommand command);
}
