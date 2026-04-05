package com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository;

import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonGoodsJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GifticonGoodsJpaRepository extends JpaRepository<GifticonGoodsJpaEntity, Long> {

    Optional<GifticonGoodsJpaEntity> findByGoodsCode(String goodsCode);

    boolean existsByGoodsCode(String goodsCode);
}
