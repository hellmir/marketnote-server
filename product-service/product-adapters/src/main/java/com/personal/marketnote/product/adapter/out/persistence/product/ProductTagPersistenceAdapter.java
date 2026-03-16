package com.personal.marketnote.product.adapter.out.persistence.product;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.adapter.out.persistence.product.entity.ProductTagJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.product.repository.ProductTagJpaRepository;
import com.personal.marketnote.product.port.out.product.UpdateProductTagPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@PersistenceAdapter
@RequiredArgsConstructor
public class ProductTagPersistenceAdapter implements UpdateProductTagPort {
    private final ProductTagJpaRepository productTagJpaRepository;

    @Override
    @CacheEvict(value = "product:detail", key = "#productId")
    public void updateOrderNums(Long productId, Map<Long, Long> tagIdToOrderNumMap) {
        List<ProductTagJpaEntity> tagEntities = productTagJpaRepository
                .findAllByProductJpaEntityIdAndIdIn(productId, new ArrayList<>(tagIdToOrderNumMap.keySet()));

        for (ProductTagJpaEntity tagEntity : tagEntities) {
            Long newOrderNum = tagIdToOrderNumMap.get(tagEntity.getId());
            if (FormatValidator.hasValue(newOrderNum)) {
                tagEntity.changeOrderNum(newOrderNum);
            }
        }
    }
}
