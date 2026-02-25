package com.personal.marketnote.reward.port.in.usecase.point;

import com.personal.marketnote.reward.port.in.command.point.RegisterUserPointCommand;

/**
 * 회원 포인트 등록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-17
 * @Description 회원 포인트 등록 기능을 제공합니다.
 */
public interface RegisterUserPointUseCase {
    /**
     * @param command 회원 포인트 등록 커맨드
     * @Date 2026-01-17
     * @Author 성효빈
     * @Description 회원 포인트를 등록합니다.
     */
    void register(RegisterUserPointCommand command);
}
