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
            WHERE g.exposed = true
              AND g.goodsStatus = 'SALE'
              AND (:categoryCode = '' OR g.categoryCode = :categoryCode)
              AND (:brandCode = '' OR g.brandCode = :brandCode)
            ORDER BY
                CASE WHEN g.orderNum IS NULL THEN 1 ELSE 0 END,
                g.orderNum ASC,
                g.createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(g) FROM GifticonGoodsJpaEntity g
            WHERE g.exposed = true
              AND g.goodsStatus = 'SALE'
              AND (:categoryCode = '' OR g.categoryCode = :categoryCode)
              AND (:brandCode = '' OR g.brandCode = :brandCode)
            """)
    Page<GifticonGoodsJpaEntity> findAllExposed(
            @Param("categoryCode") String categoryCode,
            @Param("brandCode") String brandCode,
            Pageable pageable
    );

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

    @Query("""
            SELECT g FROM GifticonGoodsJpaEntity g
            WHERE g.popular = true
              AND g.exposed = true
              AND g.goodsStatus = 'SALE'
            ORDER BY
                CASE WHEN g.orderNum IS NULL THEN 1 ELSE 0 END,
                g.orderNum ASC
            """)
    List<GifticonGoodsJpaEntity> findAllPopularAndExposed(Pageable pageable);
}
