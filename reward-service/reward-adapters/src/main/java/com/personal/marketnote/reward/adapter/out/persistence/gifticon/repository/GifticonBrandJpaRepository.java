package com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository;

import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonBrandJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GifticonBrandJpaRepository extends JpaRepository<GifticonBrandJpaEntity, Long> {

    Optional<GifticonBrandJpaEntity> findByBrandCode(String brandCode);

    boolean existsByBrandCode(String brandCode);
}
