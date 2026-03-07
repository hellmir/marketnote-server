package com.personal.marketnote.reward.adapter.out.persistence.point;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.reward.adapter.out.persistence.point.entity.UserPointJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.point.repository.UserPointJpaRepository;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.exception.UserPointNotFoundException;
import com.personal.marketnote.reward.port.out.point.FindUserPointPort;
import com.personal.marketnote.reward.port.out.point.SaveUserPointPort;
import com.personal.marketnote.reward.port.out.point.UpdateUserPointPort;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class UserPointPersistenceAdapter implements SaveUserPointPort, FindUserPointPort, UpdateUserPointPort {
    private final UserPointJpaRepository repository;

    @Override
    public UserPoint save(UserPoint userPoint) {
        return repository.save(UserPointJpaEntity.from(userPoint))
                .toDomain();
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return repository.existsByUserId(userId);
    }

    @Override
    public boolean existsByUserKey(String userKey) {
        return repository.existsByUserKey(userKey);
    }

    @Override
    public java.util.Optional<UserPoint> findByUserId(Long userId) {
        return repository.findByUserId(userId).map(UserPointJpaEntity::toDomain);
    }

    @Override
    public Optional<UserPoint> findByUserKey(String userKey) {
        return repository.findByUserKey(userKey).map(UserPointJpaEntity::toDomain);
    }

    @Override
    public UserPoint update(UserPoint userPoint) throws UserPointNotFoundException {
        UserPointJpaEntity userPointJpaEntity = findEntityByUserId(userPoint.getUserId());
        userPointJpaEntity.updateFrom(userPoint);

        return userPoint;
    }

    private UserPointJpaEntity findEntityByUserId(Long userId) throws UserPointNotFoundException {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new UserPointNotFoundException(userId));
    }
}
