package com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository;

import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonCategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GifticonCategoryJpaRepository extends JpaRepository<GifticonCategoryJpaEntity, Long> {

    Optional<GifticonCategoryJpaEntity> findByCategoryCode(String categoryCode);
}
