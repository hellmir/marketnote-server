package com.personal.marketnote.product.domain.shipping;

import com.personal.marketnote.common.domain.BaseDomain;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ShippingPolicy extends BaseDomain {
    private Long id;
    private Long sellerId;
    private String deliveryCompany;
    private Long shippingFee;
    private Long freeShippingThreshold;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static ShippingPolicy from(ShippingPolicyCreateState state) {
        validateShippingFee(state.getShippingFee());
        validateFreeShippingThreshold(state.getFreeShippingThreshold());

        ShippingPolicy policy = ShippingPolicy.builder()
                .sellerId(state.getSellerId())
                .deliveryCompany(state.getDeliveryCompany())
                .shippingFee(state.getShippingFee())
                .freeShippingThreshold(state.getFreeShippingThreshold())
                .build();
        policy.activate();
        return policy;
    }

    public static ShippingPolicy from(ShippingPolicySnapshotState state) {
        ShippingPolicy policy = ShippingPolicy.builder()
                .id(state.getId())
                .sellerId(state.getSellerId())
                .deliveryCompany(state.getDeliveryCompany())
                .shippingFee(state.getShippingFee())
                .freeShippingThreshold(state.getFreeShippingThreshold())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
        policy.status = state.getStatus();
        return policy;
    }

    public void update(String deliveryCompany, Long shippingFee, Long freeShippingThreshold) {
        validateShippingFee(shippingFee);
        validateFreeShippingThreshold(freeShippingThreshold);

        this.deliveryCompany = deliveryCompany;
        this.shippingFee = shippingFee;
        this.freeShippingThreshold = freeShippingThreshold;
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    public boolean isFreeShipping(long orderAmount) {
        return orderAmount >= freeShippingThreshold;
    }

    public long calculateShippingFee(long orderAmount) {
        if (isFreeShipping(orderAmount)) {
            return 0L;
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
}
