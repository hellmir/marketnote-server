package com.personal.marketnote.product.domain.cart;

import com.personal.marketnote.common.domain.BaseDomain;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class CartProduct extends BaseDomain {
    private Long userId;
    private UUID sharerKey;
    private PricePolicy pricePolicy;
    private String imageUrl;
    private Short quantity;

    public static CartProduct from(CartProductCreateState state) {
        CartProduct cartProduct = CartProduct.builder()
                .userId(state.getUserId())
                .sharerKey(state.getSharerKey())
                .pricePolicy(state.getPricePolicy())
                .imageUrl(state.getImageUrl())
                .quantity(state.getQuantity())
                .build();
        cartProduct.activate();

        return cartProduct;
    }

    public static CartProduct from(CartProductSnapshotState state) {
        CartProduct cartProduct = CartProduct.builder()
                .userId(state.getUserId())
                .sharerKey(state.getSharerKey())
                .pricePolicy(state.getPricePolicy())
                .imageUrl(state.getImageUrl())
                .quantity(state.getQuantity())
                .build();
        cartProduct.status = state.getStatus();

        return cartProduct;
    }

    public Long getPricePolicyId() {
        return pricePolicy.getId();
    }

    public void addQuantity(Short additionalQuantity) {
        if (FormatValidator.hasNoValue(additionalQuantity) || additionalQuantity <= 0) {
            throw new InvalidCartProductQuantityException("추가 수량은 1 이상이어야 합니다.");
        }
        int sum = this.quantity + additionalQuantity;
        if (sum > Short.MAX_VALUE) {
            throw new InvalidCartProductQuantityException("수량 한도를 초과했습니다.");
        }
        this.quantity = (short) sum;
    }

    public void updateQuantity(Short newQuantity) {
        quantity = newQuantity;
    }

    public void updatePricePolicy(PricePolicy pricePolicy) {
        this.pricePolicy = pricePolicy;
    }
}
