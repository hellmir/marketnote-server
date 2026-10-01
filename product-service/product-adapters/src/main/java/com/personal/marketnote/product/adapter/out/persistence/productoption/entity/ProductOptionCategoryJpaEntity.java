package com.personal.marketnote.product.adapter.out.persistence.productoption.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseOrderedGeneralEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductJpaEntity;
import com.personal.marketnote.product.domain.option.ProductOptionCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.PERSIST;

@Entity
@Table(name = "product_option_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ProductOptionCategoryJpaEntity extends BaseOrderedGeneralEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_product_option_category_product"))
    private ProductJpaEntity productJpaEntity;

    @OneToMany(mappedBy = "productOptionCategoryJpaEntity", cascade = {PERSIST, MERGE}, orphanRemoval = true)
    private List<ProductOptionJpaEntity> productOptionJpaEntities;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    public static ProductOptionCategoryJpaEntity from(
            ProductOptionCategory category, ProductJpaEntity productJpaEntity
    ) {
        ProductOptionCategoryJpaEntity productOptionCategoryJpaEntity = ProductOptionCategoryJpaEntity.builder()
                .productJpaEntity(productJpaEntity)
                .name(category.getName())
                .build();

        List<ProductOptionJpaEntity> productOptionJpaEntities = category.getOptions()
                .stream()
                .map(option -> ProductOptionJpaEntity.from(productOptionCategoryJpaEntity, option))
                .toList();
        productOptionCategoryJpaEntity.productOptionJpaEntities = productOptionJpaEntities;

        return productOptionCategoryJpaEntity;
    }

    public void addOrderNum() {
        setIdToOrderNum();
        productOptionJpaEntities.forEach(ProductOptionJpaEntity::setIdToOrderNum);
    }
}
