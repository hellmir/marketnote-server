package com.personal.marketnote.product.adapter.out.persistence.productoption.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseOrderedGeneralEntity;
import com.personal.marketnote.product.domain.option.ProductOption;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "product_option")
@BatchSize(size = 100)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ProductOptionJpaEntity extends BaseOrderedGeneralEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_product_option_product_option_category"))
    private ProductOptionCategoryJpaEntity productOptionCategoryJpaEntity;

    @Column(name = "content", nullable = false, length = 511)
    private String content;

    // price/accumulated_point moved to price policy; columns removed

    public static ProductOptionJpaEntity of(ProductOptionCategoryJpaEntity productOptionCategoryJpaEntity, String content, Long price, Long accumulatedPoint) {
        return ProductOptionJpaEntity.builder()
                .productOptionCategoryJpaEntity(productOptionCategoryJpaEntity)
                .content(content)
                .build();
    }

    public static ProductOptionJpaEntity from(
            ProductOptionCategoryJpaEntity productOptionCategoryJpaEntity, ProductOption option
    ) {
        return ProductOptionJpaEntity.builder()
                .productOptionCategoryJpaEntity(productOptionCategoryJpaEntity)
                .content(option.getContent())
                .build();
    }
}


