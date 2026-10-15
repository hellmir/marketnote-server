package com.personal.marketnote.user.adapter.out.persistence;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.user.adapter.out.persistence.user.entity.UserFingerprintJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.user.repository.UserFingerprintJpaRepository;
import com.personal.marketnote.user.domain.user.UserFingerprint;
import com.personal.marketnote.user.domain.user.UserFingerprintSnapshotState;
import com.personal.marketnote.user.port.out.user.FindUserFingerprintPort;
import com.personal.marketnote.user.port.out.user.SaveUserFingerprintPort;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@PersistenceAdapter
@RequiredArgsConstructor
public class UserFingerprintPersistenceAdapter implements SaveUserFingerprintPort, FindUserFingerprintPort {
    private final UserFingerprintJpaRepository userFingerprintJpaRepository;

    @Override
    public void save(UserFingerprint userFingerprint) {
        Optional<UserFingerprintJpaEntity> existing = userFingerprintJpaRepository
                .findByUserIdAndFingerprint(userFingerprint.getUserId(), userFingerprint.getFingerprint());
        if (existing.isPresent()) {
            existing.get().updateLastSeenAt(userFingerprint.getLastSeenAt());
            return;
        }

        UserFingerprintJpaEntity entity = UserFingerprintJpaEntity.of(
                userFingerprint.getUserId(),
                userFingerprint.getFingerprint(),
                userFingerprint.getSource(),
                userFingerprint.getFirstSeenAt()
        );
        userFingerprintJpaRepository.save(entity);
    }

    @Override
    public Optional<UserFingerprint> findByUserIdAndFingerprint(Long userId, String fingerprint) {
        return userFingerprintJpaRepository.findByUserIdAndFingerprint(userId, fingerprint)
                .map(this::mapToDomain);
    }

    @Override
    public List<UserFingerprint> findByUserId(Long userId) {
        return userFingerprintJpaRepository.findByUserId(userId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private UserFingerprint mapToDomain(UserFingerprintJpaEntity entity) {
        return UserFingerprint.from(
                UserFingerprintSnapshotState.builder()
                        .id(entity.getId())
                        .userId(entity.getUserId())
                        .fingerprint(entity.getFingerprint())
                        .source(entity.getSource())
                        .firstSeenAt(entity.getFirstSeenAt())
                        .lastSeenAt(entity.getLastSeenAt())
                        .build()
        );
    }
}
