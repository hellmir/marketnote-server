package com.personal.marketnote.user.adapter.out.persistence.user.repository;

import com.personal.marketnote.user.adapter.out.persistence.user.entity.UserPenaltyHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPenaltyHistoryJpaRepository extends JpaRepository<UserPenaltyHistoryJpaEntity, Long> {
}
