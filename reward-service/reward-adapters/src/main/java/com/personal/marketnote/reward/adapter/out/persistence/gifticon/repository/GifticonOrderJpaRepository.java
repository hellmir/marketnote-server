package com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository;

import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonOrderJpaEntity;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GifticonOrderJpaRepository extends JpaRepository<GifticonOrderJpaEntity, Long> {

    Optional<GifticonOrderJpaEntity> findByTrId(String trId);

    boolean existsByTrId(String trId);

    @Query("""
            SELECT o FROM GifticonOrderJpaEntity o
            WHERE o.userId = :userId
            AND o.orderStatus IN :statuses
            AND (:cursor = -1L OR o.id < :cursor)
            ORDER BY o.createdAt DESC, o.id DESC
            """)
    List<GifticonOrderJpaEntity> findByUserIdAndStatusesOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("statuses") List<GifticonOrderStatus> statuses,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT o FROM GifticonOrderJpaEntity o
            WHERE o.userId = :userId
            AND o.orderStatus IN :statuses
            ORDER BY o.validEndDate ASC, o.id ASC
            """)
    List<GifticonOrderJpaEntity> findByUserIdAndStatusesOrderByValidEndDateAsc(
            @Param("userId") Long userId,
            @Param("statuses") List<GifticonOrderStatus> statuses,
            Pageable pageable
    );

    long countByUserIdAndOrderStatusIn(Long userId, List<GifticonOrderStatus> statuses);

    Optional<GifticonOrderJpaEntity> findByIdAndUserId(Long id, Long userId);

    List<GifticonOrderJpaEntity> findAllByOrderStatus(GifticonOrderStatus orderStatus);
}
