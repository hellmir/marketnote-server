package com.personal.marketnote.user.adapter.out.persistence.user.repository;

import com.personal.marketnote.user.adapter.out.persistence.user.entity.UserFingerprintJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFingerprintJpaRepository extends JpaRepository<UserFingerprintJpaEntity, Long> {
    Optional<UserFingerprintJpaEntity> findByUserIdAndFingerprint(Long userId, String fingerprint);

    List<UserFingerprintJpaEntity> findByUserId(Long userId);
}
