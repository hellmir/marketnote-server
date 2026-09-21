package com.personal.marketnote.user.adapter.out.persistence.user.repository;

import com.personal.marketnote.user.adapter.out.persistence.user.entity.UserStatusHistoryJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserStatusHistoryJpaRepository extends JpaRepository<UserStatusHistoryJpaEntity, Long> {
    @Query("SELECT h FROM UserStatusHistoryJpaEntity h WHERE h.userId = :userId")
    Page<UserStatusHistoryJpaEntity> findUserStatusHistoriesByUserId(Pageable pageable, @Param("userId") Long userId);
}
