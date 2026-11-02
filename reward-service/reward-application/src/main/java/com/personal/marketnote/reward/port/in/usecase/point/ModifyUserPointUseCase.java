package com.personal.marketnote.reward.port.in.usecase.point;

import com.personal.marketnote.reward.port.in.command.point.ModifyUserPointCommand;
import com.personal.marketnote.reward.port.in.result.point.UpdateUserPointResult;

/**
 * 회원 포인트 변경 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-17
 * @Description 회원 포인트 변경 기능을 제공합니다.
 */
public interface ModifyUserPointUseCase {
    /**
     * @param command 회원 포인트 변경 커맨드
     * @return 회원 포인트 변경 결과 {@link UpdateUserPointResult}
     * @Date 2026-01-17
     * @Author 성효빈
     * @Description 회원 포인트를 변경합니다.
     */
    UpdateUserPointResult modify(ModifyUserPointCommand command);
}
