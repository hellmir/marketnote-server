package com.personal.marketnote.community.port.in.usecase.report;

import com.personal.marketnote.community.port.in.command.report.UpdateTargetStatusCommand;

/**
 * 대상 상태 변경 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-15
 * @Description 신고 대상의 노출/숨기기 기능을 제공합니다.
 */
public interface UpdateTargetStatusUseCase {
    /**
     * @param command 대상 노출/숨기기 커맨드
     * @Date 2026-01-15
     * @Author 성효빈
     * @Description 대상을 노출/숨기기합니다.
     */
    void updateTargetStatus(UpdateTargetStatusCommand command);
}
