package com.personal.marketnote.product.domain.shipping;

import com.personal.marketnote.common.domain.BaseDomain;
import com.personal.marketnote.common.domain.money.Money;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ShippingPolicy extends BaseDomain {
    private Long id;
    private Long sellerId;
    private String deliveryCompany;
    private Money shippingFee;
    private Money freeShippingThreshold;
    private Money jejuSurcharge;
    private Money islandSurcharge;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static ShippingPolicy from(ShippingPolicyCreateState state) {
        validateShippingFee(state.getShippingFee());
        validateFreeShippingThreshold(state.getFreeShippingThreshold());

        Long resolvedJejuSurcharge = resolveDefaultSurcharge(state.getJejuSurcharge());
        Long resolvedIslandSurcharge = resolveDefaultSurcharge(state.getIslandSurcharge());
        validateJejuSurcharge(resolvedJejuSurcharge);
        validateIslandSurcharge(resolvedIslandSurcharge);

        ShippingPolicy policy = ShippingPolicy.builder()
                .sellerId(state.getSellerId())
                .deliveryCompany(state.getDeliveryCompany())
                .shippingFee(Money.of(state.getShippingFee()))
                .freeShippingThreshold(Money.of(state.getFreeShippingThreshold()))
                .jejuSurcharge(Money.of(resolvedJejuSurcharge))
                .islandSurcharge(Money.of(resolvedIslandSurcharge))
                .build();
        policy.activate();
        return policy;
    }

    public static ShippingPolicy from(ShippingPolicySnapshotState state) {
        ShippingPolicy policy = ShippingPolicy.builder()
                .id(state.getId())
                .sellerId(state.getSellerId())
                .deliveryCompany(state.getDeliveryCompany())
                .shippingFee(Money.of(state.getShippingFee()))
                .freeShippingThreshold(Money.of(state.getFreeShippingThreshold()))
                .jejuSurcharge(resolveDefaultMoneyFromSnapshot(state.getJejuSurcharge()))
                .islandSurcharge(resolveDefaultMoneyFromSnapshot(state.getIslandSurcharge()))
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
        policy.status = state.getStatus();
        return policy;
    }

    public void update(String deliveryCompany, Long shippingFee, Long freeShippingThreshold,
                       Long jejuSurcharge, Long islandSurcharge) {
        validateShippingFee(shippingFee);
        validateFreeShippingThreshold(freeShippingThreshold);

        Long resolvedJejuSurcharge = resolveDefaultSurcharge(jejuSurcharge);
        Long resolvedIslandSurcharge = resolveDefaultSurcharge(islandSurcharge);
        validateJejuSurcharge(resolvedJejuSurcharge);
        validateIslandSurcharge(resolvedIslandSurcharge);

        this.deliveryCompany = deliveryCompany;
        this.shippingFee = Money.of(shippingFee);
        this.freeShippingThreshold = Money.of(freeShippingThreshold);
        this.jejuSurcharge = Money.of(resolvedJejuSurcharge);
        this.islandSurcharge = Money.of(resolvedIslandSurcharge);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    public boolean isFreeShipping(Money orderAmount) {
        return orderAmount.isGreaterThanOrEqual(freeShippingThreshold);
    }

    public Money calculateShippingFee(Money orderAmount) {
        if (isFreeShipping(orderAmount)) {
            return Money.zero();
        }
        return shippingFee;
    }

    private static void validateShippingFee(Long shippingFee) {
        if (shippingFee < 0) {
            throw new InvalidShippingFeeException("배송비는 0 이상이어야 합니다. 입력값=" + shippingFee);
        }
    }

    private static void validateFreeShippingThreshold(Long freeShippingThreshold) {
        if (freeShippingThreshold < 0) {
            throw new InvalidFreeShippingThresholdException("무료배송 기준금액은 0 이상이어야 합니다. 입력값=" + freeShippingThreshold);
        }
    }

    private static void validateJejuSurcharge(Long jejuSurcharge) {
        if (jejuSurcharge < 0) {
            throw new InvalidJejuSurchargeException("제주 추가 배송비는 0 이상이어야 합니다. 입력값=" + jejuSurcharge);
        }
    }

    private static void validateIslandSurcharge(Long islandSurcharge) {
        if (islandSurcharge < 0) {
            throw new InvalidIslandSurchargeException("도서산간 추가 배송비는 0 이상이어야 합니다. 입력값=" + islandSurcharge);
        }
    }

    private static Long resolveDefaultSurcharge(Long surcharge) {
        if (FormatValidator.hasNoValue(surcharge)) {
            return 0L;
        }
        return surcharge;
    }

    private static Money resolveDefaultMoneyFromSnapshot(Long surcharge) {
        if (FormatValidator.hasNoValue(surcharge)) {
            return Money.zero();
        }
        return Money.of(surcharge);
    }
}
