package com.personal.marketnote.reward.adapter.out.persistence.point.repository;

import com.personal.marketnote.reward.adapter.out.persistence.point.entity.UserPointHistoryJpaEntity;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserPointHistoryJpaRepository extends JpaRepository<UserPointHistoryJpaEntity, Long> {
    List<UserPointHistoryJpaEntity> findByUserIdOrderByAccumulatedAtDesc(Long userId);

    List<UserPointHistoryJpaEntity> findByUserIdAndAmountGreaterThanOrderByAccumulatedAtDesc(Long userId, Long amount);

    List<UserPointHistoryJpaEntity> findByUserIdAndAmountLessThanOrderByAccumulatedAtDesc(Long userId, Long amount);

    List<UserPointHistoryJpaEntity> findByUserIdAndIsReflectedAndSourceTypeAndSourceId(
            Long userId, Boolean isReflected, UserPointSourceType sourceType, Long sourceId
    );

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE UserPointHistoryJpaEntity h
            SET h.isReflected = true
            WHERE h.userId = :userId
              AND h.sourceType = :sourceType
              AND h.sourceId = :sourceId
              AND h.isReflected = false
            """)
    int markAsReflected(
            @Param("userId") Long userId,
            @Param("sourceType") UserPointSourceType sourceType,
            @Param("sourceId") Long sourceId
    );
}
