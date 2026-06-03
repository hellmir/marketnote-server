package com.personal.marketnote.reward.adapter.out.persistence.point;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.reward.adapter.out.persistence.point.entity.UserPointHistoryJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.point.repository.UserPointHistoryJpaRepository;
import com.personal.marketnote.reward.domain.point.UserPointHistory;
import com.personal.marketnote.reward.domain.point.UserPointHistoryFilter;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.DuplicateUserPointHistoryException;
import com.personal.marketnote.reward.domain.point.ReferralBonusTier;
import com.personal.marketnote.reward.port.out.point.CheckReferralBonusClaimedPort;
import com.personal.marketnote.reward.port.out.point.CountReferralPort;
import com.personal.marketnote.reward.port.out.point.FindUserPointHistoryPort;
import com.personal.marketnote.reward.port.out.point.SaveUserPointHistoryPort;
import com.personal.marketnote.reward.port.out.point.UpdateUserPointHistoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static com.personal.marketnote.common.utility.AccrualPointAmountConstant.REFERRAL_BONUS_REASON_PREFIX;
import static com.personal.marketnote.common.utility.AccrualPointAmountConstant.REFERRER_POINT_REASON;

@PersistenceAdapter
@RequiredArgsConstructor
public class UserPointHistoryPersistenceAdapter implements SaveUserPointHistoryPort, FindUserPointHistoryPort, UpdateUserPointHistoryPort, CountReferralPort, CheckReferralBonusClaimedPort {
    private final UserPointHistoryJpaRepository repository;

    @Override
    public UserPointHistory save(UserPointHistory history) {
        try {
            UserPointHistoryJpaEntity saved = repository.saveAndFlush(
                    Objects.requireNonNull(UserPointHistoryJpaEntity.from(history))
            );
            return saved.toDomain();
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateUserPointHistoryException(
                    history.getUserId(), history.getSourceType(),
                    history.getSourceId(), history.getReason()
            );
        }
    }

    @Override
    public List<UserPointHistory> findByUserId(Long userId, UserPointHistoryFilter filter,
                                               LocalDate startDate, LocalDate endDate,
                                               Long cursor, int pageSize) {
        List<UserPointHistoryJpaEntity> histories = repository.findByUserIdAndDateRangeAndFilter(
                userId,
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay(),
                filter.getAmountFilterValue(),
                cursor,
                PageRequest.of(0, pageSize)
        );

        return histories.stream()
                .map(UserPointHistoryJpaEntity::toDomain)
                .toList();
    }

    @Override
    public long countByUserId(Long userId, UserPointHistoryFilter filter, LocalDate startDate, LocalDate endDate) {
        return repository.countByUserIdAndDateRangeAndFilter(
                userId,
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay(),
                filter.getAmountFilterValue()
        );
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

    @Override
    public long countCompletedReferrals(Long userId) {
        return repository.countByUserIdAndSourceTypeAndReason(
                userId, UserPointSourceType.USER, REFERRER_POINT_REASON
        );
    }

    @Override
    public long sumReferralEarnedAmount(Long userId) {
        return repository.sumReferralEarnedAmount(
                userId, UserPointSourceType.USER, REFERRER_POINT_REASON, REFERRAL_BONUS_REASON_PREFIX
        );
    }

    @Override
    public boolean isAlreadyClaimed(Long userId, ReferralBonusTier tier) {
        return repository.existsByUserIdAndSourceTypeAndSourceIdAndReason(
                userId, UserPointSourceType.USER, userId, tier.getReason()
        );
    }
}
