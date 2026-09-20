package com.personal.marketnote.user.adapter.out.persistence.user.repository;

import com.personal.marketnote.user.adapter.out.persistence.user.entity.UserPenaltyHistoryJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserPenaltyHistoryJpaRepository extends JpaRepository<UserPenaltyHistoryJpaEntity, Long> {
    @Query("""
            SELECT h
            FROM UserPenaltyHistoryJpaEntity h
            WHERE h.userId = :userId
            """)
    Page<UserPenaltyHistoryJpaEntity> findUserPenaltyHistoriesByUserId(Pageable pageable, @Param("userId") Long userId);
}
