package com.personal.marketnote.reward.adapter.out.persistence.point.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseEntity;
import com.personal.marketnote.reward.domain.point.PointAmount;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.domain.point.UserPointSnapshotState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "user_point")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class UserPointJpaEntity extends BaseEntity {
    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_key", nullable = false, unique = true)
    private String userKey;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "add_expected_amount", nullable = false)
    private Long addExpectedAmount;

    @Column(name = "expire_expected_amount", nullable = false)
    private Long expireExpectedAmount;

    public static UserPointJpaEntity from(UserPoint userPoint) {
        return UserPointJpaEntity.builder()
                .userId(userPoint.getUserId())
                .userKey(userPoint.getUserKey())
                .amount(userPoint.getAmountValue())
                .addExpectedAmount(userPoint.getAddExpectedAmountValue())
                .expireExpectedAmount(userPoint.getExpireExpectedAmountValue())
                .build();
    }

    public UserPoint toDomain() {
        return UserPoint.from(
                UserPointSnapshotState.builder()
                        .userId(userId)
                        .userKey(userKey)
                        .amount(PointAmount.of(amount))
                        .addExpectedAmount(PointAmount.of(addExpectedAmount))
                        .expireExpectedAmount(PointAmount.of(expireExpectedAmount))
                        .createdAt(getCreatedAt())
                        .modifiedAt(getModifiedAt())
                        .build()
        );
    }

    public void updateFrom(UserPoint userPoint) {
        this.amount = userPoint.getAmountValue();
        this.addExpectedAmount = userPoint.getAddExpectedAmountValue();
        this.expireExpectedAmount = userPoint.getExpireExpectedAmountValue();
    }
}
