package com.personal.marketnote.reward.adapter.out.persistence.point.repository;

import com.personal.marketnote.reward.adapter.out.persistence.point.entity.UserPointJpaEntity;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.Optional;

public interface UserPointJpaRepository extends JpaRepository<UserPointJpaEntity, Long> {
    boolean existsByUserId(Long userId);

    boolean existsByUserKey(String userKey);

    Optional<UserPointJpaEntity> findByUserId(Long userId);

    Optional<UserPointJpaEntity> findByUserKey(String userKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    Optional<UserPointJpaEntity> findWithLockingByUserId(Long userId);
}
