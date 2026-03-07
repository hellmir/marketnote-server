package com.personal.marketnote.reward.adapter.out.persistence.point;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.reward.adapter.out.persistence.point.entity.UserPointHistoryJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.point.repository.UserPointHistoryJpaRepository;
import com.personal.marketnote.reward.domain.point.UserPointHistory;
import com.personal.marketnote.reward.domain.point.UserPointHistoryFilter;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.port.out.point.FindUserPointHistoryPort;
import com.personal.marketnote.reward.port.out.point.SaveUserPointHistoryPort;
import com.personal.marketnote.reward.port.out.point.UpdateUserPointHistoryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;

@PersistenceAdapter
@RequiredArgsConstructor
public class UserPointHistoryPersistenceAdapter implements SaveUserPointHistoryPort, FindUserPointHistoryPort, UpdateUserPointHistoryPort {
    private final UserPointHistoryJpaRepository repository;

    @Override
    public UserPointHistory save(UserPointHistory history) {
        UserPointHistoryJpaEntity saved = repository.save(
                Objects.requireNonNull(UserPointHistoryJpaEntity.from(history))
        );
        return saved.toDomain();
    }

    @Override
    public List<UserPointHistory> findByUserId(Long userId, UserPointHistoryFilter filter) {
        List<UserPointHistoryJpaEntity> histories = getHistories(userId, filter);

        return histories.stream()
                .map(UserPointHistoryJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<UserPointHistory> findUnreflectedByUserIdAndSource(
            Long userId, UserPointSourceType sourceType, Long sourceId
    ) {
        return repository.findByUserIdAndIsReflectedAndSourceTypeAndSourceId(
                        userId, Boolean.FALSE, sourceType, sourceId
                ).stream()
                .map(UserPointHistoryJpaEntity::toDomain)
                .toList();
    }

    @Override
    public int markAsReflected(Long userId, UserPointSourceType sourceType, Long sourceId) {
        return repository.markAsReflected(userId, sourceType, sourceId);
    }

    private List<UserPointHistoryJpaEntity> getHistories(Long userId, UserPointHistoryFilter filter) {
        if (filter.isAccrual()) {
            return repository.findByUserIdAndAmountGreaterThanOrderByAccumulatedAtDesc(userId, 0L);
        }

        if (filter.isDeduction()) {
            return repository.findByUserIdAndAmountLessThanOrderByAccumulatedAtDesc(userId, 0L);
        }

        return repository.findByUserIdOrderByAccumulatedAtDesc(userId);
    }
}
