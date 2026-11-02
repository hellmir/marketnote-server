package com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository;

import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonCategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GifticonCategoryJpaRepository extends JpaRepository<GifticonCategoryJpaEntity, Long> {

    Optional<GifticonCategoryJpaEntity> findByCategoryCode(String categoryCode);

    List<GifticonCategoryJpaEntity> findAllByOrderByOrderNumAsc();

    @Query("""
            SELECT c FROM GifticonCategoryJpaEntity c
            WHERE c.exposed = true
            ORDER BY
                CASE WHEN c.orderNum IS NULL THEN 1 ELSE 0 END,
                c.orderNum ASC
            """)
    List<GifticonCategoryJpaEntity> findAllExposed();
}
