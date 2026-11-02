package com.personal.marketnote.product.adapter.out.persistence.productcategory.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_category")
@IdClass(ProductCategoryId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ProductCategoryJpaEntity extends BaseEntity {
    @Id
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Id
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    public static ProductCategoryJpaEntity of(Long productId, Long categoryId) {
        return ProductCategoryJpaEntity.builder()
                .productId(productId)
                .categoryId(categoryId)
                .build();
    }
}
