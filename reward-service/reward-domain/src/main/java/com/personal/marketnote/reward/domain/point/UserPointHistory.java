package com.personal.marketnote.reward.domain.point;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.exception.UserPointHistoryAmountNoValueException;
import com.personal.marketnote.reward.domain.exception.UserPointHistoryChangeTypeNoValueException;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class UserPointHistory {
    private Long id;
    private Long userId;
    private UserPointChangeType changeType;
    private PointAmount amount;
    private Boolean isReflected;
    private UserPointSourceType sourceType;
    private Long sourceId;
    private String reason;
    private LocalDateTime accumulatedAt;
    private LocalDateTime createdAt;

    public static UserPointHistory from(UserPointHistoryCreateState state) {
        validate(state.getChangeType(), state.getAmount());
        return UserPointHistory.builder()
                .userId(state.getUserId())
                .changeType(state.getChangeType())
                .amount(state.getAmount())
                .isReflected(state.getIsReflected())
                .sourceType(state.getSourceType())
                .sourceId(state.getSourceId())
                .reason(state.getReason())
                .accumulatedAt(state.getAccumulatedAt())
                .build();
    }

    public static UserPointHistory from(UserPointHistorySnapshotState state) {
        return UserPointHistory.builder()
                .id(state.getId())
                .userId(state.getUserId())
                .changeType(state.getChangeType())
                .amount(state.getAmount())
                .isReflected(state.getIsReflected())
                .sourceType(state.getSourceType())
                .sourceId(state.getSourceId())
                .reason(state.getReason())
                .accumulatedAt(state.getAccumulatedAt())
                .createdAt(state.getCreatedAt())
                .build();
    }

    public boolean isAccrual() {
        return changeType.isAccrual();
    }

    public boolean isDeduction() {
        return changeType.isDeduction();
    }

    public Long signedAmount() {
        if (isDeduction()) {
            return Math.negateExact(amount.getValue());
        }
        return amount.getValue();
    }

    public Long getAmountValue() {
        return amount.getValue();
    }

    private static void validate(UserPointChangeType changeType, PointAmount amount) {
        if (FormatValidator.hasNoValue(changeType)) {
            throw new UserPointHistoryChangeTypeNoValueException();
        }
        if (FormatValidator.hasNoValue(amount)) {
            throw new UserPointHistoryAmountNoValueException();
        }
    }
}
