package com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository;

import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonGoodsJpaEntity;
import com.personal.marketnote.reward.domain.gifticon.GoodsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GifticonGoodsQueryRepository {

    List<Object[]> findDistinctBrandsByCategoryCode(String categoryCode, GoodsStatus saleStatus);

    List<GifticonGoodsJpaEntity> findExposedByAnchor(
            String categoryCode,
            String brandCode,
            GoodsStatus saleStatus,
            Integer anchorOrderNum,
            Long anchorId,
            Pageable pageable
    );

    long countAllExposed(String categoryCode, String brandCode, GoodsStatus saleStatus);

    Page<GifticonGoodsJpaEntity> findAllForAdmin(
            GoodsStatus goodsStatus,
            Boolean exposed,
            String keyword,
            Pageable pageable
    );

    List<GifticonGoodsJpaEntity> findAllPopularAndExposed(GoodsStatus saleStatus, Pageable pageable);
}
