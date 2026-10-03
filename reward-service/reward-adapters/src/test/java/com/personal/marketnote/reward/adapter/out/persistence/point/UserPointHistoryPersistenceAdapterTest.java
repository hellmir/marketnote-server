package com.personal.marketnote.reward.adapter.out.persistence.point;

import com.personal.marketnote.reward.adapter.out.persistence.point.entity.UserPointHistoryJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.point.repository.UserPointHistoryJpaRepository;
import com.personal.marketnote.reward.domain.point.PointAmount;
import com.personal.marketnote.reward.domain.point.ReferralBonusTier;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointHistory;
import com.personal.marketnote.reward.domain.point.UserPointHistoryCreateState;
import com.personal.marketnote.reward.domain.point.UserPointHistoryFilter;
import com.personal.marketnote.reward.domain.point.UserPointHistorySnapshotState;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.DuplicateUserPointHistoryException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.personal.marketnote.common.utility.AccrualPointAmountConstant.REFERRAL_BONUS_REASON_PREFIX;
import static com.personal.marketnote.common.utility.AccrualPointAmountConstant.REFERRER_POINT_REASON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserPointHistoryPersistenceAdapter 테스트")
class UserPointHistoryPersistenceAdapterTest {

    private static final Long USER_ID = 1L;

    @Mock
    private UserPointHistoryJpaRepository repository;

    @InjectMocks
    private UserPointHistoryPersistenceAdapter adapter;

    private UserPointHistoryJpaEntity buildEntity(Long id) {
        UserPointHistory restored = UserPointHistory.from(UserPointHistorySnapshotState.builder()
                .id(id)
                .userId(USER_ID)
                .changeType(UserPointChangeType.ACCRUAL)
                .amount(PointAmount.of(1_000L))
                .isReflected(true)
                .sourceType(UserPointSourceType.ORDER)
                .sourceId(100L + id)
                .reason("주문 적립")
                .accumulatedAt(LocalDateTime.of(2026, 4, 10, 12, 0))
                .createdAt(LocalDateTime.of(2026, 4, 10, 12, 0))
                .build());
        return UserPointHistoryJpaEntity.from(restored);
    }

    @Nested
    @DisplayName("save")
    class SaveTest {

        @Test
        @DisplayName("정상 저장 시 도메인 객체를 반환한다")
        void shouldReturnDomainWhenSaveSucceeds() {
            // given
            UserPointHistory history = UserPointHistory.from(UserPointHistoryCreateState.builder()
                    .userId(USER_ID)
                    .changeType(UserPointChangeType.ACCRUAL)
                    .amount(PointAmount.of(1_000L))
                    .isReflected(true)
                    .sourceType(UserPointSourceType.ORDER)
                    .sourceId(100L)
                    .reason("주문 적립")
                    .accumulatedAt(LocalDateTime.of(2026, 4, 10, 12, 0))
                    .build());
            given(repository.saveAndFlush(any(UserPointHistoryJpaEntity.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            UserPointHistory saved = adapter.save(history);

            // then
            assertThat(saved.getUserId()).isEqualTo(USER_ID);
            assertThat(saved.isAccrual()).isTrue();
            assertThat(saved.getAmountValue()).isEqualTo(1_000L);
            verify(repository).saveAndFlush(any(UserPointHistoryJpaEntity.class));
        }

        @Test
        @DisplayName("DataIntegrityViolationException 발생 시 DuplicateUserPointHistoryException을 던진다")
        void shouldThrowDuplicateExceptionWhenDataIntegrityViolation() {
            // given
            UserPointHistory history = UserPointHistory.from(UserPointHistoryCreateState.builder()
                    .userId(USER_ID)
                    .changeType(UserPointChangeType.ACCRUAL)
                    .amount(PointAmount.of(1_000L))
                    .isReflected(true)
                    .sourceType(UserPointSourceType.ORDER)
                    .sourceId(100L)
                    .reason("주문 적립")
                    .accumulatedAt(LocalDateTime.now())
                    .build());
            given(repository.saveAndFlush(any(UserPointHistoryJpaEntity.class)))
                    .willThrow(new DataIntegrityViolationException("dup"));

            // when & then
            assertThatThrownBy(() -> adapter.save(history))
                    .isInstanceOf(DuplicateUserPointHistoryException.class);
        }
    }

    @Nested
    @DisplayName("findByUserId")
    class FindByUserIdTest {

        @Test
        @DisplayName("ALL 필터일 때 changeType 분기 없는 조회를 호출한다")
        void shouldUseAllFilterQuery() {
            // given
            given(repository.findByUserIdAndDateRange(eq(USER_ID), any(), any(), any(), any(Pageable.class)))
                    .willReturn(List.of(buildEntity(1L), buildEntity(2L)));

            // when
            List<UserPointHistory> result = adapter.findByUserId(
                    USER_ID, UserPointHistoryFilter.ALL,
                    LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                    null, 10
            );

            // then
            assertThat(result).hasSize(2);
            verify(repository).findByUserIdAndDateRange(eq(USER_ID), any(), any(), any(), any(Pageable.class));
        }

        @Test
        @DisplayName("ACCRUAL 필터일 때 changeType 조건이 포함된 조회를 호출한다")
        void shouldUseChangeTypeFilterQuery() {
            // given
            given(repository.findByUserIdAndDateRangeAndChangeType(
                    eq(USER_ID), any(), any(), eq(UserPointChangeType.ACCRUAL), any(), any(Pageable.class)))
                    .willReturn(List.of(buildEntity(1L)));

            // when
            List<UserPointHistory> result = adapter.findByUserId(
                    USER_ID, UserPointHistoryFilter.ACCRUAL,
                    LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30),
                    null, 10
            );

            // then
            assertThat(result).hasSize(1);
            verify(repository).findByUserIdAndDateRangeAndChangeType(
                    eq(USER_ID), any(), any(), eq(UserPointChangeType.ACCRUAL), any(), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("countByUserId")
    class CountByUserIdTest {

        @Test
        @DisplayName("ALL 필터일 때 changeType 분기 없는 카운트를 호출한다")
        void shouldUseAllFilterCount() {
            given(repository.countByUserIdAndDateRange(eq(USER_ID), any(), any())).willReturn(5L);
            long count = adapter.countByUserId(USER_ID, UserPointHistoryFilter.ALL,
                    LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));
            assertThat(count).isEqualTo(5L);
        }

        @Test
        @DisplayName("DEDUCTION 필터일 때 changeType 조건이 포함된 카운트를 호출한다")
        void shouldUseChangeTypeFilterCount() {
            given(repository.countByUserIdAndDateRangeAndChangeType(
                    eq(USER_ID), any(), any(), eq(UserPointChangeType.DEDUCTION))).willReturn(3L);
            long count = adapter.countByUserId(USER_ID, UserPointHistoryFilter.DEDUCTION,
                    LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));
            assertThat(count).isEqualTo(3L);
        }
    }

    @Test
    @DisplayName("findUnreflectedByUserIdAndSource는 isReflected=false 조건으로 조회한다")
    void shouldFindUnreflected() {
        given(repository.findByUserIdAndIsReflectedAndSourceTypeAndSourceId(
                USER_ID, Boolean.FALSE, UserPointSourceType.ORDER, 100L))
                .willReturn(List.of(buildEntity(1L)));
        List<UserPointHistory> result = adapter.findUnreflectedByUserIdAndSource(
                USER_ID, UserPointSourceType.ORDER, 100L);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("markAsReflected는 리포지토리 결과를 그대로 반환한다")
    void shouldDelegateMarkAsReflected() {
        given(repository.markAsReflected(USER_ID, UserPointSourceType.ORDER, 100L)).willReturn(2);
        assertThat(adapter.markAsReflected(USER_ID, UserPointSourceType.ORDER, 100L)).isEqualTo(2);
    }

    @Test
    @DisplayName("countCompletedReferrals는 USER 소스 + 추천인 적립 사유로 카운트한다")
    void shouldCountCompletedReferrals() {
        given(repository.countByUserIdAndSourceTypeAndReason(USER_ID, UserPointSourceType.USER, REFERRER_POINT_REASON))
                .willReturn(7L);
        assertThat(adapter.countCompletedReferrals(USER_ID)).isEqualTo(7L);
    }

    @Test
    @DisplayName("sumReferralEarnedAmount는 기본 사유와 누적 보너스 사유 패턴 합계를 반환한다")
    void shouldSumReferralEarnedAmount() {
        given(repository.sumReferralEarnedAmount(
                USER_ID, UserPointSourceType.USER, REFERRER_POINT_REASON, REFERRAL_BONUS_REASON_PREFIX))
                .willReturn(15_000L);
        assertThat(adapter.sumReferralEarnedAmount(USER_ID)).isEqualTo(15_000L);
    }

    @Test
    @DisplayName("isAlreadyClaimed는 USER 소스+사용자 ID+티어 사유 존재 여부를 반환한다")
    void shouldCheckAlreadyClaimed() {
        given(repository.existsByUserIdAndSourceTypeAndSourceIdAndReason(
                USER_ID, UserPointSourceType.USER, USER_ID, ReferralBonusTier.TIER_1.getReason()))
                .willReturn(true);
        assertThat(adapter.isAlreadyClaimed(USER_ID, ReferralBonusTier.TIER_1)).isTrue();
    }
}
