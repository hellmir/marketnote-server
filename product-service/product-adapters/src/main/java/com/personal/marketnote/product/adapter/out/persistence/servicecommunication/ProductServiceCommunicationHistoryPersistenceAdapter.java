package com.personal.marketnote.product.adapter.out.persistence.servicecommunication;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.product.adapter.out.persistence.servicecommunication.entity.ProductServiceCommunicationHistoryJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.servicecommunication.repository.ProductServiceCommunicationHistoryJpaRepository;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationHistory;
import com.personal.marketnote.product.port.out.servicecommunication.SaveProductServiceCommunicationHistoryPort;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
public class ProductServiceCommunicationHistoryPersistenceAdapter
        implements SaveProductServiceCommunicationHistoryPort {
    private final ProductServiceCommunicationHistoryJpaRepository repository;

    @Override
    public ProductServiceCommunicationHistory save(ProductServiceCommunicationHistory history) {
        ProductServiceCommunicationHistoryJpaEntity savedEntity = repository.save(
                ProductServiceCommunicationHistoryJpaEntity.from(history)
        );
        return savedEntity.toDomain();
    }
}
