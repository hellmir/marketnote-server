package com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository;

import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonOrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GifticonOrderJpaRepository extends JpaRepository<GifticonOrderJpaEntity, Long> {

    Optional<GifticonOrderJpaEntity> findByTrId(String trId);

    boolean existsByTrId(String trId);
}
