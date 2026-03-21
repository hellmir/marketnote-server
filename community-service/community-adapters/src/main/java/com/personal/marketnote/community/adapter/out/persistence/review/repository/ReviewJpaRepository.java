package com.personal.marketnote.community.adapter.out.persistence.review.repository;

import com.personal.marketnote.community.adapter.out.persistence.review.entity.ReviewJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewJpaRepository extends JpaRepository<ReviewJpaEntity, Long> {
    boolean existsByOrderIdAndPricePolicyId(Long orderId, Long pricePolicyId);

    @Query("""
            SELECT r
            FROM ReviewJpaEntity r
            WHERE r.productId = :productId
              AND (:cursor IS NULL OR r.id > :cursor)
              AND r.status = 'ACTIVE'
            ORDER BY r.id ASC
            """)
    List<ReviewJpaEntity> findProductReviewsByCursorOrderByIdAsc(
            @Param("productId") Long productId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT r
            FROM ReviewJpaEntity r
            WHERE r.productId = :productId
              AND (:cursor IS NULL OR r.id < :cursor)
              AND r.status = 'ACTIVE'
            ORDER BY r.id DESC
            """)
    List<ReviewJpaEntity> findProductReviewsByCursorOrderByIdDesc(
            @Param("productId") Long productId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT r
            FROM ReviewJpaEntity r
            WHERE r.productId = :productId
              AND (
                    :cursor IS NULL
                 OR r.orderNum > (SELECT r2.orderNum FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                 OR (
                        r.orderNum = (SELECT r2.orderNum FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                    AND r.id > :cursor
                 )
              )
              AND r.status = 'ACTIVE'
            ORDER BY r.orderNum ASC, r.id ASC
            """)
    List<ReviewJpaEntity> findProductReviewsByCursorOrderByOrderNumAsc(
            @Param("productId") Long productId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT r
            FROM ReviewJpaEntity r
            WHERE r.productId = :productId
              AND (
                    :cursor IS NULL
                 OR r.orderNum < (SELECT r2.orderNum FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                 OR (
                        r.orderNum = (SELECT r2.orderNum FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                    AND r.id < :cursor
                 )
              )
              AND r.status = 'ACTIVE'
            ORDER BY r.orderNum DESC, r.id DESC
            """)
    List<ReviewJpaEntity> findProductReviewsByCursorOrderByOrderNumDesc(
            @Param("productId") Long productId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT r
            FROM ReviewJpaEntity r
            WHERE r.productId = :productId
              AND (
                    :cursor IS NULL
                 OR (
                        (SELECT COUNT(l) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l
                         WHERE l.id.targetType = 'REVIEW' AND l.id.targetId = r.id)
                        > (SELECT COUNT(l2) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l2
                           WHERE l2.id.targetType = 'REVIEW' AND l2.id.targetId = :cursor)
                    OR (
                            (SELECT COUNT(l) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l
                             WHERE l.id.targetType = 'REVIEW' AND l.id.targetId = r.id)
                            = (SELECT COUNT(l2) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l2
                               WHERE l2.id.targetType = 'REVIEW' AND l2.id.targetId = :cursor)
                        AND r.rating > (SELECT r2.rating FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                    )
                    OR (
                            (SELECT COUNT(l) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l
                             WHERE l.id.targetType = 'REVIEW' AND l.id.targetId = r.id)
                            = (SELECT COUNT(l2) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l2
                               WHERE l2.id.targetType = 'REVIEW' AND l2.id.targetId = :cursor)
                        AND r.rating = (SELECT r2.rating FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                        AND r.id > :cursor
                    )
                 )
              )
              AND r.status = 'ACTIVE'
            ORDER BY
                (SELECT COUNT(l) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l
                 WHERE l.id.targetType = 'REVIEW' AND l.id.targetId = r.id) ASC,
                r.rating ASC,
                r.id ASC
            """)
    List<ReviewJpaEntity> findProductReviewsByCursorOrderByLikeCountAsc(
            @Param("productId") Long productId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT r
            FROM ReviewJpaEntity r
            WHERE r.productId = :productId
              AND (
                    :cursor IS NULL
                 OR (
                        (SELECT COUNT(l) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l
                         WHERE l.id.targetType = 'REVIEW' AND l.id.targetId = r.id)
                        < (SELECT COUNT(l2) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l2
                           WHERE l2.id.targetType = 'REVIEW' AND l2.id.targetId = :cursor)
                    OR (
                            (SELECT COUNT(l) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l
                             WHERE l.id.targetType = 'REVIEW' AND l.id.targetId = r.id)
                            = (SELECT COUNT(l2) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l2
                               WHERE l2.id.targetType = 'REVIEW' AND l2.id.targetId = :cursor)
                        AND r.rating < (SELECT r2.rating FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                    )
                    OR (
                            (SELECT COUNT(l) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l
                             WHERE l.id.targetType = 'REVIEW' AND l.id.targetId = r.id)
                            = (SELECT COUNT(l2) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l2
                               WHERE l2.id.targetType = 'REVIEW' AND l2.id.targetId = :cursor)
                        AND r.rating = (SELECT r2.rating FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                        AND r.id < :cursor
                    )
                 )
              )
              AND r.status = 'ACTIVE'
            ORDER BY
                (SELECT COUNT(l) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l
                 WHERE l.id.targetType = 'REVIEW' AND l.id.targetId = r.id) DESC,
                r.rating DESC,
                r.id DESC
            """)
    List<ReviewJpaEntity> findProductReviewsByCursorOrderByLikeCountDesc(
            @Param("productId") Long productId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT r
            FROM ReviewJpaEntity r
            WHERE r.productId = :productId
              AND (
                    :cursor IS NULL
                 OR r.rating > (SELECT r2.rating FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                 OR (
                        r.rating = (SELECT r2.rating FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                    AND r.id < :cursor
                 )
              )
              AND r.status = 'ACTIVE'
            ORDER BY r.rating ASC, r.id DESC
            """)
    List<ReviewJpaEntity> findProductReviewsByCursorOrderByRatingAsc(
            @Param("productId") Long productId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT r
            FROM ReviewJpaEntity r
            WHERE r.productId = :productId
              AND (
                    :cursor IS NULL
                 OR r.rating < (SELECT r2.rating FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                 OR (
                        r.rating = (SELECT r2.rating FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                    AND r.id < :cursor
                 )
              )
              AND r.status = 'ACTIVE'
            ORDER BY r.rating DESC, r.id DESC
            """)
    List<ReviewJpaEntity> findProductReviewsByCursorOrderByRatingDesc(
            @Param("productId") Long productId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    default List<ReviewJpaEntity> findProductReviewsByCursor(
            Long productId,
            Long cursor,
            Pageable pageable,
            String sortProperty,
            Boolean isAsc
    ) {
        if ("orderNum".equals(sortProperty)) {
            return Boolean.TRUE.equals(isAsc)
                    ? findProductReviewsByCursorOrderByOrderNumAsc(productId, cursor, pageable)
                    : findProductReviewsByCursorOrderByOrderNumDesc(productId, cursor, pageable);
        }
        if ("likeCount".equals(sortProperty)) {
            return Boolean.TRUE.equals(isAsc)
                    ? findProductReviewsByCursorOrderByLikeCountAsc(productId, cursor, pageable)
                    : findProductReviewsByCursorOrderByLikeCountDesc(productId, cursor, pageable);
        }
        if ("rating".equals(sortProperty)) {
            return Boolean.TRUE.equals(isAsc)
                    ? findProductReviewsByCursorOrderByRatingAsc(productId, cursor, pageable)
                    : findProductReviewsByCursorOrderByRatingDesc(productId, cursor, pageable);
        }
        return Boolean.TRUE.equals(isAsc)
                ? findProductReviewsByCursorOrderByIdAsc(productId, cursor, pageable)
                : findProductReviewsByCursorOrderByIdDesc(productId, cursor, pageable);
    }

    @Query("""
            SELECT r
            FROM ReviewJpaEntity r
            WHERE r.productId = :productId
              AND r.isPhoto = TRUE
              AND (:cursor IS NULL OR r.id > :cursor)
              AND r.status = 'ACTIVE'
            ORDER BY r.id ASC
            """)
    List<ReviewJpaEntity> findProductPhotoReviewsByCursorOrderByIdAsc(
            @Param("productId") Long productId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT r
            FROM ReviewJpaEntity r
            WHERE r.productId = :productId
              AND r.isPhoto = TRUE
              AND (:cursor IS NULL OR r.id < :cursor)
              AND r.status = 'ACTIVE'
            ORDER BY r.id DESC
            """)
    List<ReviewJpaEntity> findProductPhotoReviewsByCursorOrderByIdDesc(
            @Param("productId") Long productId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT r
            FROM ReviewJpaEntity r
            WHERE r.productId = :productId
              AND r.isPhoto = TRUE
              AND (
                    :cursor IS NULL
                 OR r.orderNum > (SELECT r2.orderNum FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                 OR (
                        r.orderNum = (SELECT r2.orderNum FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                    AND r.id > :cursor
                 )
              )
              AND r.status = 'ACTIVE'
            ORDER BY r.orderNum ASC, r.id ASC
            """)
    List<ReviewJpaEntity> findProductPhotoReviewsByCursorOrderByOrderNumAsc(
            @Param("productId") Long productId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT r
            FROM ReviewJpaEntity r
            WHERE r.productId = :productId
              AND r.isPhoto = TRUE
              AND (
                    :cursor IS NULL
                 OR r.orderNum < (SELECT r2.orderNum FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                 OR (
                        r.orderNum = (SELECT r2.orderNum FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                    AND r.id < :cursor
                 )
              )
              AND r.status = 'ACTIVE'
            ORDER BY r.orderNum DESC, r.id DESC
            """)
    List<ReviewJpaEntity> findProductPhotoReviewsByCursorOrderByOrderNumDesc(
            @Param("productId") Long productId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT r
            FROM ReviewJpaEntity r
            WHERE r.productId = :productId
              AND r.isPhoto = TRUE
              AND (
                    :cursor IS NULL
                 OR (
                        (SELECT COUNT(l) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l
                         WHERE l.id.targetType = 'REVIEW' AND l.id.targetId = r.id)
                        > (SELECT COUNT(l2) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l2
                           WHERE l2.id.targetType = 'REVIEW' AND l2.id.targetId = :cursor)
                    OR (
                            (SELECT COUNT(l) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l
                             WHERE l.id.targetType = 'REVIEW' AND l.id.targetId = r.id)
                            = (SELECT COUNT(l2) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l2
                               WHERE l2.id.targetType = 'REVIEW' AND l2.id.targetId = :cursor)
                        AND r.rating > (SELECT r2.rating FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                    )
                    OR (
                            (SELECT COUNT(l) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l
                             WHERE l.id.targetType = 'REVIEW' AND l.id.targetId = r.id)
                            = (SELECT COUNT(l2) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l2
                               WHERE l2.id.targetType = 'REVIEW' AND l2.id.targetId = :cursor)
                        AND r.rating = (SELECT r2.rating FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                        AND r.id > :cursor
                    )
                 )
              )
              AND r.status = 'ACTIVE'
            ORDER BY
                (SELECT COUNT(l) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l
                 WHERE l.id.targetType = 'REVIEW' AND l.id.targetId = r.id) ASC,
                r.rating ASC,
                r.id ASC
            """)
    List<ReviewJpaEntity> findProductPhotoReviewsByCursorOrderByLikeCountAsc(
            @Param("productId") Long productId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT r
            FROM ReviewJpaEntity r
            WHERE r.productId = :productId
              AND r.isPhoto = TRUE
              AND (
                    :cursor IS NULL
                 OR (
                        (SELECT COUNT(l) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l
                         WHERE l.id.targetType = 'REVIEW' AND l.id.targetId = r.id)
                        < (SELECT COUNT(l2) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l2
                           WHERE l2.id.targetType = 'REVIEW' AND l2.id.targetId = :cursor)
                    OR (
                            (SELECT COUNT(l) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l
                             WHERE l.id.targetType = 'REVIEW' AND l.id.targetId = r.id)
                            = (SELECT COUNT(l2) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l2
                               WHERE l2.id.targetType = 'REVIEW' AND l2.id.targetId = :cursor)
                        AND r.rating < (SELECT r2.rating FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                    )
                    OR (
                            (SELECT COUNT(l) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l
                             WHERE l.id.targetType = 'REVIEW' AND l.id.targetId = r.id)
                            = (SELECT COUNT(l2) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l2
                               WHERE l2.id.targetType = 'REVIEW' AND l2.id.targetId = :cursor)
                        AND r.rating = (SELECT r2.rating FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                        AND r.id < :cursor
                    )
                 )
              )
              AND r.status = 'ACTIVE'
            ORDER BY
                (SELECT COUNT(l) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l
                 WHERE l.id.targetType = 'REVIEW' AND l.id.targetId = r.id) DESC,
                r.rating DESC,
                r.id DESC
            """)
    List<ReviewJpaEntity> findProductPhotoReviewsByCursorOrderByLikeCountDesc(
            @Param("productId") Long productId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT r
            FROM ReviewJpaEntity r
            WHERE r.productId = :productId
              AND r.isPhoto = TRUE
              AND (
                    :cursor IS NULL
                 OR r.rating > (SELECT r2.rating FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                 OR (
                        r.rating = (SELECT r2.rating FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                    AND r.id < :cursor
                 )
              )
              AND r.status = 'ACTIVE'
            ORDER BY r.rating ASC, r.id DESC
            """)
    List<ReviewJpaEntity> findProductPhotoReviewsByCursorOrderByRatingAsc(
            @Param("productId") Long productId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT r
            FROM ReviewJpaEntity r
            WHERE r.productId = :productId
              AND r.isPhoto = TRUE
              AND (
                    :cursor IS NULL
                 OR r.rating < (SELECT r2.rating FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                 OR (
                        r.rating = (SELECT r2.rating FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                    AND r.id < :cursor
                 )
              )
              AND r.status = 'ACTIVE'
            ORDER BY r.rating DESC, r.id DESC
            """)
    List<ReviewJpaEntity> findProductPhotoReviewsByCursorOrderByRatingDesc(
            @Param("productId") Long productId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    default List<ReviewJpaEntity> findProductPhotoReviewsByCursor(
            Long productId,
            Long cursor,
            Pageable pageable,
            String sortProperty,
            Boolean isAsc
    ) {
        if ("orderNum".equals(sortProperty)) {
            return Boolean.TRUE.equals(isAsc)
                    ? findProductPhotoReviewsByCursorOrderByOrderNumAsc(productId, cursor, pageable)
                    : findProductPhotoReviewsByCursorOrderByOrderNumDesc(productId, cursor, pageable);
        }
        if ("likeCount".equals(sortProperty)) {
            return Boolean.TRUE.equals(isAsc)
                    ? findProductPhotoReviewsByCursorOrderByLikeCountAsc(productId, cursor, pageable)
                    : findProductPhotoReviewsByCursorOrderByLikeCountDesc(productId, cursor, pageable);
        }
        if ("rating".equals(sortProperty)) {
            return Boolean.TRUE.equals(isAsc)
                    ? findProductPhotoReviewsByCursorOrderByRatingAsc(productId, cursor, pageable)
                    : findProductPhotoReviewsByCursorOrderByRatingDesc(productId, cursor, pageable);
        }
        return Boolean.TRUE.equals(isAsc)
                ? findProductPhotoReviewsByCursorOrderByIdAsc(productId, cursor, pageable)
                : findProductPhotoReviewsByCursorOrderByIdDesc(productId, cursor, pageable);
    }

    @Query("""
            SELECT COUNT(r)
            FROM ReviewJpaEntity r
            WHERE r.productId = :productId
              AND r.isPhoto = :isPhoto
              AND r.status = 'ACTIVE'
            """)
    long countByProductIdAndIsPhoto(@Param("productId") Long productId, @Param("isPhoto") Boolean isPhoto);

    @Query("""
            SELECT COUNT(r)
            FROM ReviewJpaEntity r
            WHERE r.productId = :productId
              AND r.status = 'ACTIVE'
            """)
    long countByProductId(@Param("productId") Long productId);

    boolean existsByIdAndReviewerId(Long id, Long reviewerId);

    @Query("""
            SELECT r
            FROM ReviewJpaEntity r
            WHERE r.reviewerId = :userId
              AND (:cursor IS NULL OR r.id > :cursor)
            ORDER BY r.id ASC
            """)
    List<ReviewJpaEntity> findUserReviewsByCursorOrderByIdAsc(
            @Param("userId") Long userId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT r
            FROM ReviewJpaEntity r
            WHERE r.reviewerId = :userId
              AND (:cursor IS NULL OR r.id < :cursor)
            ORDER BY r.id DESC
            """)
    List<ReviewJpaEntity> findUserReviewsByCursorOrderByIdDesc(
            @Param("userId") Long userId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT r
            FROM ReviewJpaEntity r
            WHERE r.reviewerId = :userId
              AND (
                    :cursor IS NULL
                 OR r.orderNum > (SELECT r2.orderNum FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                 OR (
                        r.orderNum = (SELECT r2.orderNum FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                    AND r.id > :cursor
                 )
              )
            ORDER BY r.orderNum ASC, r.id ASC
            """)
    List<ReviewJpaEntity> findUserReviewsByCursorOrderByOrderNumAsc(
            @Param("userId") Long userId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT r
            FROM ReviewJpaEntity r
            WHERE r.reviewerId = :userId
              AND (
                    :cursor IS NULL
                 OR r.orderNum < (SELECT r2.orderNum FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                 OR (
                        r.orderNum = (SELECT r2.orderNum FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                    AND r.id < :cursor
                 )
              )
            ORDER BY r.orderNum DESC, r.id DESC
            """)
    List<ReviewJpaEntity> findUserReviewsByCursorOrderByOrderNumDesc(
            @Param("userId") Long userId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT r
            FROM ReviewJpaEntity r
            WHERE r.reviewerId = :userId
              AND (
                    :cursor IS NULL
                 OR (
                        (SELECT COUNT(l) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l
                         WHERE l.id.targetType = 'REVIEW' AND l.id.targetId = r.id)
                        > (SELECT COUNT(l2) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l2
                           WHERE l2.id.targetType = 'REVIEW' AND l2.id.targetId = :cursor)
                    OR (
                            (SELECT COUNT(l) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l
                             WHERE l.id.targetType = 'REVIEW' AND l.id.targetId = r.id)
                            = (SELECT COUNT(l2) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l2
                               WHERE l2.id.targetType = 'REVIEW' AND l2.id.targetId = :cursor)
                        AND r.rating > (SELECT r2.rating FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                    )
                    OR (
                            (SELECT COUNT(l) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l
                             WHERE l.id.targetType = 'REVIEW' AND l.id.targetId = r.id)
                            = (SELECT COUNT(l2) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l2
                               WHERE l2.id.targetType = 'REVIEW' AND l2.id.targetId = :cursor)
                        AND r.rating = (SELECT r2.rating FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                        AND r.id > :cursor
                    )
                 )
              )
            ORDER BY
                (SELECT COUNT(l) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l
                 WHERE l.id.targetType = 'REVIEW' AND l.id.targetId = r.id) ASC,
                r.rating ASC,
                r.id ASC
            """)
    List<ReviewJpaEntity> findUserReviewsByCursorOrderByLikeCountAsc(
            @Param("userId") Long userId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT r
            FROM ReviewJpaEntity r
            WHERE r.reviewerId = :userId
              AND (
                    :cursor IS NULL
                 OR (
                        (SELECT COUNT(l) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l
                         WHERE l.id.targetType = 'REVIEW' AND l.id.targetId = r.id)
                        < (SELECT COUNT(l2) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l2
                           WHERE l2.id.targetType = 'REVIEW' AND l2.id.targetId = :cursor)
                    OR (
                            (SELECT COUNT(l) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l
                             WHERE l.id.targetType = 'REVIEW' AND l.id.targetId = r.id)
                            = (SELECT COUNT(l2) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l2
                               WHERE l2.id.targetType = 'REVIEW' AND l2.id.targetId = :cursor)
                        AND r.rating < (SELECT r2.rating FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                    )
                    OR (
                            (SELECT COUNT(l) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l
                             WHERE l.id.targetType = 'REVIEW' AND l.id.targetId = r.id)
                            = (SELECT COUNT(l2) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l2
                               WHERE l2.id.targetType = 'REVIEW' AND l2.id.targetId = :cursor)
                        AND r.rating = (SELECT r2.rating FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                        AND r.id < :cursor
                    )
                 )
              )
            ORDER BY
                (SELECT COUNT(l) FROM com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity l
                 WHERE l.id.targetType = 'REVIEW' AND l.id.targetId = r.id) DESC,
                r.rating DESC,
                r.id DESC
            """)
    List<ReviewJpaEntity> findUserReviewsByCursorOrderByLikeCountDesc(
            @Param("userId") Long userId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT r
            FROM ReviewJpaEntity r
            WHERE r.reviewerId = :userId
              AND (
                    :cursor IS NULL
                 OR r.rating > (SELECT r2.rating FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                 OR (
                        r.rating = (SELECT r2.rating FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                    AND r.id < :cursor
                 )
              )
            ORDER BY r.rating ASC, r.id DESC
            """)
    List<ReviewJpaEntity> findUserReviewsByCursorOrderByRatingAsc(
            @Param("userId") Long userId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT r
            FROM ReviewJpaEntity r
            WHERE r.reviewerId = :userId
              AND (
                    :cursor IS NULL
                 OR r.rating < (SELECT r2.rating FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                 OR (
                        r.rating = (SELECT r2.rating FROM ReviewJpaEntity r2 WHERE r2.id = :cursor)
                    AND r.id < :cursor
                 )
              )
            ORDER BY r.rating DESC, r.id DESC
            """)
    List<ReviewJpaEntity> findUserReviewsByCursorOrderByRatingDesc(
            @Param("userId") Long userId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    default List<ReviewJpaEntity> findUserReviewsByCursor(
            Long userId,
            Long cursor,
            Pageable pageable,
            String sortProperty,
            Boolean isAsc
    ) {
        if ("orderNum".equals(sortProperty)) {
            return Boolean.TRUE.equals(isAsc)
                    ? findUserReviewsByCursorOrderByOrderNumAsc(userId, cursor, pageable)
                    : findUserReviewsByCursorOrderByOrderNumDesc(userId, cursor, pageable);
        }
        if ("likeCount".equals(sortProperty)) {
            return Boolean.TRUE.equals(isAsc)
                    ? findUserReviewsByCursorOrderByLikeCountAsc(userId, cursor, pageable)
                    : findUserReviewsByCursorOrderByLikeCountDesc(userId, cursor, pageable);
        }
        if ("rating".equals(sortProperty)) {
            return Boolean.TRUE.equals(isAsc)
                    ? findUserReviewsByCursorOrderByRatingAsc(userId, cursor, pageable)
                    : findUserReviewsByCursorOrderByRatingDesc(userId, cursor, pageable);
        }
        return Boolean.TRUE.equals(isAsc)
                ? findUserReviewsByCursorOrderByIdAsc(userId, cursor, pageable)
                : findUserReviewsByCursorOrderByIdDesc(userId, cursor, pageable);
    }

    @Query("""
            SELECT COUNT(r)
            FROM ReviewJpaEntity r
            WHERE r.reviewerId = :reviewerId
            """)
    long countByReviewerId(@Param("reviewerId") Long reviewerId);
}
