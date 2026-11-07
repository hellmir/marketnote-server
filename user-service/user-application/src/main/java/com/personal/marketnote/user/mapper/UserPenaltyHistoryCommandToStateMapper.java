package com.personal.marketnote.user.mapper;

import com.personal.marketnote.user.domain.user.UserPenaltyHistoryCreateState;
import com.personal.marketnote.user.port.in.command.ApplyUserPenaltyCommand;
import com.personal.marketnote.user.port.in.command.UpdateUserPenaltyCountCommand;

public class UserPenaltyHistoryCommandToStateMapper {
    private UserPenaltyHistoryCommandToStateMapper() {
    }

    /**
     * {@link ApplyUserPenaltyCommand}로부터 {@link UserPenaltyHistoryCreateState}를 생성한다.
     *
     * <p><b>구조적 제약:</b>
     * {@code previousCount}, {@code currentCount}는 Command가 아닌 도메인 상태 변경 전/후의
     * {@code User#getPenaltyCount()} 값이다. Command에는 존재하지 않으므로 개별 파라미터로 전달한다.</p>
     */
    public static UserPenaltyHistoryCreateState mapToCreateState(
            ApplyUserPenaltyCommand command, int previousCount, int currentCount
    ) {
        return UserPenaltyHistoryCreateState.builder()
                .userId(command.userId())
                .previousCount(previousCount)
                .currentCount(currentCount)
                .reason(command.reason())
                .createdBy(command.adminId())
                .build();
    }

    /**
     * {@link UpdateUserPenaltyCountCommand}로부터 {@link UserPenaltyHistoryCreateState}를 생성한다.
     *
     * <p><b>구조적 제약:</b>
     * {@code previousCount}, {@code currentCount}는 Command가 아닌 도메인 상태 변경 전/후의
     * {@code User#getPenaltyCount()} 값이다. Command에는 존재하지 않으므로 개별 파라미터로 전달한다.</p>
     */
    public static UserPenaltyHistoryCreateState mapToCreateState(
            UpdateUserPenaltyCountCommand command, int previousCount, int currentCount
    ) {
        return UserPenaltyHistoryCreateState.builder()
                .userId(command.userId())
                .previousCount(previousCount)
                .currentCount(currentCount)
                .reason(command.reason())
                .createdBy(command.adminId())
                .build();
    }
}
