package com.personal.marketnote.product.adapter.out.persistence.servicecommunication.repository;

import com.personal.marketnote.product.adapter.out.persistence.servicecommunication.entity.ProductServiceCommunicationHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductServiceCommunicationHistoryJpaRepository
        extends JpaRepository<ProductServiceCommunicationHistoryJpaEntity, Long> {
}
