package com.personal.marketnote.product.adapter.out.mapper;

import com.personal.marketnote.product.adapter.out.persistence.cart.entity.CartProductJpaEntity;
import com.personal.marketnote.product.domain.cart.CartProduct;
import com.personal.marketnote.product.domain.cart.CartProductSnapshotState;

import java.util.Optional;

public class CartJpaEntityToDomainMapper {
    public static Optional<CartProduct> mapToDomain(CartProductJpaEntity cartProductJpaEntity) {
        return Optional.ofNullable(cartProductJpaEntity)
                .map(
                        entity -> CartProduct.from(
                                CartProductSnapshotState.builder()
                                        .userId(cartProductJpaEntity.getId().getUserId())
                                        .sharerKey(cartProductJpaEntity.getSharerKey())
                                        .pricePolicy(
                                                PricePolicyJpaEntityToDomainMapper.mapToDomainWithOptions(
                                                                cartProductJpaEntity.getPricePolicyJpaEntity()
                                                        )
                                                        .orElse(null)
                                        )
                                        .imageUrl(cartProductJpaEntity.getImageUrl())
                                        .quantity(cartProductJpaEntity.getQuantity())
                                        .status(cartProductJpaEntity.getStatus())
                                        .build()
                        )
                );
    }
}
