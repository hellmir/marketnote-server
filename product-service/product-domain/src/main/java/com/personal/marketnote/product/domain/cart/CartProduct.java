package com.personal.marketnote.product.domain.cart;

import com.personal.marketnote.common.domain.BaseDomain;
import com.personal.marketnote.common.domain.quantity.Quantity;
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
    private Quantity quantity;

    public static CartProduct from(CartProductCreateState state) {
        CartProduct cartProduct = CartProduct.builder()
                .userId(state.getUserId())
                .sharerKey(state.getSharerKey())
                .pricePolicy(state.getPricePolicy())
                .imageUrl(state.getImageUrl())
                .quantity(Quantity.of((int) state.getQuantity()))
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
                .quantity(Quantity.of((int) state.getQuantity()))
                .build();
        cartProduct.status = state.getStatus();

        return cartProduct;
    }

    public Long getPricePolicyId() {
        return pricePolicy.getId();
    }

    public void addQuantity(Short additionalQuantity) {
        Quantity additional = Quantity.of((int) additionalQuantity);
        Quantity sum = this.quantity.add(additional);
        if (sum.getValue() > Short.MAX_VALUE) {
            throw new InvalidCartProductQuantityException("수량 한도를 초과했습니다.");
        }
        this.quantity = sum;
    }

    public void updateQuantity(Short newQuantity) {
        this.quantity = Quantity.of((int) newQuantity);
    }

    public void updatePricePolicy(PricePolicy pricePolicy) {
        this.pricePolicy = pricePolicy;
    }
}
