package com.personal.marketnote.reward.port.in.usecase.offerwall;

import com.personal.marketnote.reward.port.in.command.offerwall.RegisterOfferwallRewardCommand;

/**
 * 오퍼월 보상 등록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-16
 * @Description 오퍼월 보상 등록 기능을 제공합니다.
 */
public interface RegisterOfferwallRewardUseCase {
    /**
     * @param command 오퍼월 보상 등록 커맨드
     * @return 오퍼월 보상 등록 결과 ID {@link Long}
     * @Date 2026-01-16
     * @Author 성효빈
     * @Description 오퍼월 보상을 등록합니다.
     */
    Long register(RegisterOfferwallRewardCommand command);
}
