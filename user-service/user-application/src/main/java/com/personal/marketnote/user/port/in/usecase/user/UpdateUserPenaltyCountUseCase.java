package com.personal.marketnote.user.port.in.usecase.user;

import com.personal.marketnote.user.port.in.command.UpdateUserPenaltyCountCommand;
import com.personal.marketnote.user.port.in.result.UpdateUserPenaltyCountResult;

/**
 * 회원 패널티 횟수 수정 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-09-20
 * @Description 관리자가 회원의 패널티 횟수를 임의의 값으로 수정하는 기능을 제공합니다.
 */
public interface UpdateUserPenaltyCountUseCase {
    /**
     * @param command 패널티 횟수 수정 요청
     * @return 패널티 횟수 수정 결과 {@link UpdateUserPenaltyCountResult}
     * @Date 2026-09-20
     * @Author 성효빈
     * @Description 회원의 패널티 횟수를 지정된 값으로 수정하고 이력을 저장합니다.
     */
    UpdateUserPenaltyCountResult updatePenaltyCount(UpdateUserPenaltyCountCommand command);
}
