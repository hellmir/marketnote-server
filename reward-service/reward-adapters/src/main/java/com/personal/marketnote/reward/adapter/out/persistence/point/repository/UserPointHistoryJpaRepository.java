package com.personal.marketnote.reward.adapter.out.persistence.point.repository;

import com.personal.marketnote.reward.adapter.out.persistence.point.entity.UserPointHistoryJpaEntity;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserPointHistoryJpaRepository extends JpaRepository<UserPointHistoryJpaEntity, Long> {

    // amountFilter: 0=ALL, 1=ACCRUAL(적립, amount>0), -1=DEDUCTION(사용, amount<0)
    @Query("""
            SELECT h FROM UserPointHistoryJpaEntity h
            WHERE h.userId = :userId
              AND h.accumulatedAt >= :startDateTime
              AND h.accumulatedAt < :endDateTime
              AND (:amountFilter = 0
                OR (:amountFilter = 1 AND h.amount > 0)
                OR (:amountFilter = -1 AND h.amount < 0))
            ORDER BY h.accumulatedAt DESC
            """)
    List<UserPointHistoryJpaEntity> findByUserIdAndDateRangeAndFilter(
            @Param("userId") Long userId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("amountFilter") int amountFilter
    );

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
