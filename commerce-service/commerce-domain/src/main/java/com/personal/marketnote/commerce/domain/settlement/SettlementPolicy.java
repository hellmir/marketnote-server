package com.personal.marketnote.commerce.domain.settlement;

import com.personal.marketnote.common.domain.BaseDomain;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.money.Money;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 판매자별 정산 정책 도메인 모델.
 * <p>
 * 판매자마다 PG 수수료율, 플랫폼 수수료율, 정산 주기, 최소 지급 금액을 관리한다.
 * 정산 실행 시 해당 판매자의 정책이 존재하면 정책의 수수료율을 적용하고,
 * 없으면 시스템 기본 수수료율(DefaultSettlementPolicyProvider)을 사용한다.
 * </p>
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class SettlementPolicy extends BaseDomain {
    private Long id;
    private Long sellerId;
    private FeeRate pgFeeRate;
    private FeeRate platformFeeRate;
    private SettlementCycle settlementCycle;
    private Money minPayoutAmount;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    /**
     * 새 정산 정책 생성 시 사용하는 팩토리 메서드.
     * 수수료율 합계 100% 이하, 최소 지급 금액 양수 조건을 검증한다.
     * 개별 수수료율 범위 검증은 FeeRate VO에서 수행된다.
     */
    public static SettlementPolicy from(SettlementPolicyCreateState state) {
        validateFeeRateSum(state.getPgFeeRate(), state.getPlatformFeeRate());
        validateMinPayoutAmount(state.getMinPayoutAmount());

        SettlementPolicy policy = SettlementPolicy.builder()
                .sellerId(state.getSellerId())
                .pgFeeRate(state.getPgFeeRate())
                .platformFeeRate(state.getPlatformFeeRate())
                .settlementCycle(state.getSettlementCycle())
                .minPayoutAmount(Money.of(state.getMinPayoutAmount()))
                .build();
        policy.status = EntityStatus.ACTIVE;
        return policy;
    }

    /**
     * DB에서 복원할 때 사용하는 팩토리 메서드.
     * 검증 없이 그대로 매핑한다.
     */
    public static SettlementPolicy from(SettlementPolicySnapshotState state) {
        SettlementPolicy policy = SettlementPolicy.builder()
                .id(state.getId())
                .sellerId(state.getSellerId())
                .pgFeeRate(state.getPgFeeRate())
                .platformFeeRate(state.getPlatformFeeRate())
                .settlementCycle(state.getSettlementCycle())
                .minPayoutAmount(Money.of(state.getMinPayoutAmount()))
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
        policy.status = state.getStatus();
        return policy;
    }

    /**
     * 정산 정책을 업데이트한다.
     * 비즈니스 규칙을 재검증한 후 필드를 변경한다.
     */
    public void update(FeeRate pgFeeRate, FeeRate platformFeeRate,
                       SettlementCycle settlementCycle, Long minPayoutAmount) {
        validateFeeRateSum(pgFeeRate, platformFeeRate);
        validateMinPayoutAmount(minPayoutAmount);

        this.pgFeeRate = pgFeeRate;
        this.platformFeeRate = platformFeeRate;
        this.settlementCycle = settlementCycle;
        this.minPayoutAmount = Money.of(minPayoutAmount);
    }

    /**
     * 정산 정책을 비활성화(소프트 삭제)한다.
     */
    @Override
    public void deactivate() {
        super.deactivate();
    }

    private static void validateFeeRateSum(FeeRate pgFeeRate, FeeRate platformFeeRate) {
        if (pgFeeRate.getValue() + platformFeeRate.getValue() > FeeRate.BASIS_POINT_DENOMINATOR) {
            throw new InvalidSettlementPolicyException(
                    "수수료율 합계가 100%를 초과합니다. pgFeeRate=" + pgFeeRate.getValue()
                            + ", platformFeeRate=" + platformFeeRate.getValue());
        }
    }

    private static void validateMinPayoutAmount(Long minPayoutAmount) {
        if (FormatValidator.hasNoValue(minPayoutAmount) || minPayoutAmount < 0) {
            throw new InvalidSettlementPolicyException(
                    "최소 지급 금액은 0 이상이어야 합니다. minPayoutAmount=" + minPayoutAmount);
        }
    }
}
