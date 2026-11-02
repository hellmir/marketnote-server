package com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository;

import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonCategoryMappingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GifticonCategoryMappingJpaRepository extends JpaRepository<GifticonCategoryMappingJpaEntity, Long> {

    Optional<GifticonCategoryMappingJpaEntity> findByGiftishowCategorySeq(String giftishowCategorySeq);
}
