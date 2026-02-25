package com.personal.marketnote.reward.port.in.usecase.attendance;

import com.personal.marketnote.reward.port.in.command.attendance.RegisterAttendancePolicyCommand;
import com.personal.marketnote.reward.port.in.result.attendance.RegisterAttendancePolicyResult;

/**
 * 출석 정책 등록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-21
 * @Description 출석 정책 등록 기능을 제공합니다.
 */
public interface RegisterAttendancePolicyUseCase {
    /**
     * @param command 출석 정책 등록 커맨드
     * @return 출석 정책 등록 결과 {@link RegisterAttendancePolicyResult}
     * @Date 2026-01-21
     * @Author 성효빈
     * @Description 출석 정책을 등록합니다.
     */
    RegisterAttendancePolicyResult register(RegisterAttendancePolicyCommand command);
}

