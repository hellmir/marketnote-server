package com.personal.marketnote.reward.adapter.out.persistence.point.repository;

import com.personal.marketnote.reward.adapter.out.persistence.point.entity.UserPointHistoryJpaEntity;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserPointHistoryJpaRepository extends JpaRepository<UserPointHistoryJpaEntity, Long> {

    @Query("""
            SELECT h FROM UserPointHistoryJpaEntity h
            WHERE h.userId = :userId
              AND h.accumulatedAt >= :startDateTime
              AND h.accumulatedAt < :endDateTime
              AND (:cursor IS NULL
                OR h.accumulatedAt < (SELECT h2.accumulatedAt FROM UserPointHistoryJpaEntity h2 WHERE h2.id = :cursor AND h2.userId = :userId)
                OR (h.accumulatedAt = (SELECT h2.accumulatedAt FROM UserPointHistoryJpaEntity h2 WHERE h2.id = :cursor AND h2.userId = :userId) AND h.id < :cursor))
            ORDER BY h.accumulatedAt DESC, h.id DESC
            """)
    List<UserPointHistoryJpaEntity> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT h FROM UserPointHistoryJpaEntity h
            WHERE h.userId = :userId
              AND h.accumulatedAt >= :startDateTime
              AND h.accumulatedAt < :endDateTime
              AND h.changeType = :changeType
              AND (:cursor IS NULL
                OR h.accumulatedAt < (SELECT h2.accumulatedAt FROM UserPointHistoryJpaEntity h2 WHERE h2.id = :cursor AND h2.userId = :userId)
                OR (h.accumulatedAt = (SELECT h2.accumulatedAt FROM UserPointHistoryJpaEntity h2 WHERE h2.id = :cursor AND h2.userId = :userId) AND h.id < :cursor))
            ORDER BY h.accumulatedAt DESC, h.id DESC
            """)
    List<UserPointHistoryJpaEntity> findByUserIdAndDateRangeAndChangeType(
            @Param("userId") Long userId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("changeType") UserPointChangeType changeType,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(h) FROM UserPointHistoryJpaEntity h
            WHERE h.userId = :userId
              AND h.accumulatedAt >= :startDateTime
              AND h.accumulatedAt < :endDateTime
            """)
    long countByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query("""
            SELECT COUNT(h) FROM UserPointHistoryJpaEntity h
            WHERE h.userId = :userId
              AND h.accumulatedAt >= :startDateTime
              AND h.accumulatedAt < :endDateTime
              AND h.changeType = :changeType
            """)
    long countByUserIdAndDateRangeAndChangeType(
            @Param("userId") Long userId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("changeType") UserPointChangeType changeType
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

    long countByUserIdAndSourceTypeAndReason(
            Long userId, UserPointSourceType sourceType, String reason
    );

    boolean existsByUserIdAndSourceTypeAndSourceIdAndReason(
            Long userId, UserPointSourceType sourceType, Long sourceId, String reason
    );

    @Query("""
            SELECT COALESCE(SUM(h.amount), 0) FROM UserPointHistoryJpaEntity h
            WHERE h.userId = :userId
              AND h.sourceType = :sourceType
              AND (h.reason = :basicReason OR h.reason LIKE :bonusReasonPattern)
            """)
    long sumReferralEarnedAmount(
            @Param("userId") Long userId,
            @Param("sourceType") UserPointSourceType sourceType,
            @Param("basicReason") String basicReason,
            @Param("bonusReasonPattern") String bonusReasonPattern
    );
}
