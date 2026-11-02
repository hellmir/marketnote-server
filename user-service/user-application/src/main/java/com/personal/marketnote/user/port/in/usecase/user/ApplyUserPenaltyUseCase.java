package com.personal.marketnote.user.port.in.usecase.user;

import com.personal.marketnote.user.port.in.command.ApplyUserPenaltyCommand;
import com.personal.marketnote.user.port.in.result.ApplyUserPenaltyResult;

/**
 * 회원 패널티 부과 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-09-20
 * @Description 관리자가 회원에게 패널티를 부과하는 기능을 제공합니다.
 */
public interface ApplyUserPenaltyUseCase {
    /**
     * @param command 패널티 부과 요청
     * @return 패널티 부과 결과 {@link ApplyUserPenaltyResult}
     * @Date 2026-09-20
     * @Author 성효빈
     * @Description 회원의 패널티 횟수를 1 증가시키고 이력을 저장합니다.
     */
    ApplyUserPenaltyResult applyPenalty(ApplyUserPenaltyCommand command);
}
