package com.personal.marketnote.product.adapter.out.persistence.tagsync.repository;

import com.personal.marketnote.product.adapter.out.persistence.tagsync.entity.ProductTagChangeSyncLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductTagChangeSyncLogJpaRepository extends JpaRepository<ProductTagChangeSyncLogJpaEntity, Long> {

    boolean existsByEventId(UUID eventId);
}
