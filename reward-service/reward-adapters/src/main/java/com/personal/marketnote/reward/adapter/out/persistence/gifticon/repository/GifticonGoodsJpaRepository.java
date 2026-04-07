package com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository;

import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonGoodsJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GifticonGoodsJpaRepository extends JpaRepository<GifticonGoodsJpaEntity, Long> {

    Optional<GifticonGoodsJpaEntity> findByGoodsCode(String goodsCode);

    boolean existsByGoodsCode(String goodsCode);

    List<GifticonGoodsJpaEntity> findAllByGoodsStatus(String goodsStatus);

    @Query("""
            SELECT DISTINCT g.brandCode, g.brandName, g.brandImageUrl
            FROM GifticonGoodsJpaEntity g
            WHERE g.exposed = true
              AND g.goodsStatus = 'SALE'
              AND g.categoryCode = :categoryCode
            ORDER BY g.brandName ASC
            """)
    List<Object[]> findDistinctBrandsByCategoryCode(@Param("categoryCode") String categoryCode);

    @Query(value = """
            SELECT g FROM GifticonGoodsJpaEntity g
            WHERE (:goodsStatus = '' OR g.goodsStatus = :goodsStatus)
            AND (:exposed IS NULL OR g.exposed = :exposed)
            AND (:keyword = '' OR g.goodsName LIKE CONCAT('%', :keyword, '%'))
            ORDER BY g.id DESC
            """,
            countQuery = """
            SELECT COUNT(g) FROM GifticonGoodsJpaEntity g
            WHERE (:goodsStatus = '' OR g.goodsStatus = :goodsStatus)
            AND (:exposed IS NULL OR g.exposed = :exposed)
            AND (:keyword = '' OR g.goodsName LIKE CONCAT('%', :keyword, '%'))
            """)
    Page<GifticonGoodsJpaEntity> findAllForAdmin(
            @Param("goodsStatus") String goodsStatus,
            @Param("exposed") Boolean exposed,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
