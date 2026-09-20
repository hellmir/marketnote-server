package com.personal.marketnote.reward.domain.point;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.exception.InsufficientPendingPointAmountException;
import com.personal.marketnote.reward.domain.exception.InvalidPointAmountException;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class UserPoint {
    private Long userId;
    private String userKey;
    private PointAmount amount;
    private PointAmount addExpectedAmount;
    private PointAmount expireExpectedAmount;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public UserPoint withAmount(Long amount) {
        return UserPoint.builder()
                .userId(userId)
                .userKey(userKey)
                .amount(PointAmount.of(amount))
                .addExpectedAmount(addExpectedAmount)
                .expireExpectedAmount(expireExpectedAmount)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .build();
    }

    public static UserPoint from(UserPointCreateState state) {
        return UserPoint.builder()
                .userId(state.getUserId())
                .userKey(state.getUserKey())
                .amount(resolvePointAmountOrZero(state.getAmount()))
                .addExpectedAmount(resolvePointAmountOrZero(state.getAddExpectedAmount()))
                .expireExpectedAmount(resolvePointAmountOrZero(state.getExpireExpectedAmount()))
                .build();
    }

    public static UserPoint from(UserPointSnapshotState state) {
        return UserPoint.builder()
                .userId(state.getUserId())
                .userKey(state.getUserKey())
                .amount(resolvePointAmountOrZero(state.getAmount()))
                .addExpectedAmount(resolvePointAmountOrZero(state.getAddExpectedAmount()))
                .expireExpectedAmount(resolvePointAmountOrZero(state.getExpireExpectedAmount()))
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
    }

    public void changeAmount(boolean isAccrual, Long amount) {
        PointAmount delta = PointAmount.of(amount);
        if (isAccrual) {
            this.amount = this.amount.add(delta);
            return;
        }
        this.amount = this.amount.subtract(delta);
    }

    public void addPendingAmount(Long amount) {
        validatePendingAmount(amount);
        this.addExpectedAmount = this.addExpectedAmount.add(PointAmount.of(amount));
    }

    public void deductPendingAmount(Long amount) {
        validatePendingAmount(amount);
        if (!hasSufficientPendingAmount(amount)) {
            throw new InsufficientPendingPointAmountException(this.addExpectedAmount.getValue(), amount);
        }
        this.addExpectedAmount = this.addExpectedAmount.subtract(PointAmount.of(amount));
    }

    public boolean hasSufficientPendingAmount(Long amount) {
        return this.addExpectedAmount.isGreaterThanOrEqual(PointAmount.of(amount));
    }

    private void validatePendingAmount(Long amount) {
        if (amount <= 0) {
            throw new InvalidPointAmountException(
                    String.format("적립 예정 포인트 금액은 0보다 커야 합니다. 요청 금액: %d", amount)
            );
        }
    }

    public void confirmPendingAmount(Long amount) {
        deductPendingAmount(amount);
        changeAmount(true, amount);
    }

    public Long getAmountValue() {
        return amount.getValue();
    }

    public Long getAddExpectedAmountValue() {
        return addExpectedAmount.getValue();
    }

    public Long getExpireExpectedAmountValue() {
        return expireExpectedAmount.getValue();
    }

    private static PointAmount resolvePointAmountOrZero(PointAmount value) {
        if (FormatValidator.hasNoValue(value)) {
            return PointAmount.zero();
        }
        return value;
    }
}
