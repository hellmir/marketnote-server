package com.personal.marketnote.user.port.in.usecase.user;

import com.personal.marketnote.user.port.in.command.ChangeUserStatusCommand;
import com.personal.marketnote.user.port.in.result.ChangeUserStatusResult;

/**
 * 회원 상태 변경 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-09-20
 * @Description 관리자가 회원의 상태를 비활성화/활성화하는 기능을 제공합니다.
 */
public interface ChangeUserStatusUseCase {
    /**
     * @param command 상태 변경 요청
     * @return 상태 변경 결과 {@link ChangeUserStatusResult}
     * @Date 2026-09-20
     * @Author 성효빈
     * @Description 회원의 상태를 변경하고 변경 이력을 저장합니다.
     */
    ChangeUserStatusResult changeStatus(ChangeUserStatusCommand command);
}
