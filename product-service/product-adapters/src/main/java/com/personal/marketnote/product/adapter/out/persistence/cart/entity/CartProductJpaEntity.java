package com.personal.marketnote.product.adapter.out.persistence.cart.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseEntity;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.adapter.out.persistence.pricepolicy.entity.PricePolicyJpaEntity;
import com.personal.marketnote.product.domain.cart.CartProduct;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "cart_product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class CartProductJpaEntity extends BaseEntity {
    @EmbeddedId
    private CartId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("pricePolicyId")
    @JoinColumn(name = "price_policy_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_product_price_policy"))
    private PricePolicyJpaEntity pricePolicyJpaEntity;

    @Column(name = "sharer_key")
    private UUID sharerKey;

    @Column(name = "image_url", length = 511)
    private String imageUrl;

    @Column(name = "quantity", nullable = false)
    private Short quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EntityStatus status;

    public static CartProductJpaEntity from(CartProduct cartProduct, PricePolicyJpaEntity pricePolicyJpaEntity) {
        return CartProductJpaEntity.builder()
                .id(new CartId(cartProduct.getUserId(), pricePolicyJpaEntity.getId()))
                .pricePolicyJpaEntity(pricePolicyJpaEntity)
                .sharerKey(cartProduct.getSharerKey())
                .imageUrl(cartProduct.getImageUrl())
                .quantity(cartProduct.getQuantity())
                .status(cartProduct.getStatus())
                .build();
    }

    public void updateFrom(CartProduct cartProduct) {
        quantity = cartProduct.getQuantity();
        status = cartProduct.getStatus();
    }
}
