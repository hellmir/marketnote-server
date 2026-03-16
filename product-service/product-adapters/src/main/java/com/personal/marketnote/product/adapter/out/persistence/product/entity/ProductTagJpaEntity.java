package com.personal.marketnote.product.adapter.out.persistence.product.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseOrderedGeneralEntity;
import com.personal.marketnote.product.domain.product.ProductTag;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_tag")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ProductTagJpaEntity extends BaseOrderedGeneralEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductJpaEntity productJpaEntity;

    @Column(name = "name", nullable = false, length = 31)
    private String name;

    public static ProductTagJpaEntity from(ProductJpaEntity productJpaEntity, ProductTag productTag) {
        return new ProductTagJpaEntity(productJpaEntity, productTag.getName());
    }

    public void changeOrderNum(Long orderNum) {
        updateOrderNum(orderNum);
    }
}
