package com.personal.marketnote.reward.adapter.out.persistence.point.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.reward.adapter.out.persistence.point.entity.UserPointHistoryJpaEntity;
import com.personal.marketnote.reward.domain.point.PointAmount;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointHistory;
import com.personal.marketnote.reward.domain.point.UserPointHistoryCreateState;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static com.personal.marketnote.common.utility.AccrualPointAmountConstant.REFERRAL_BONUS_REASON_PREFIX;
import static com.personal.marketnote.common.utility.AccrualPointAmountConstant.REFERRER_POINT_REASON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import(AuditConfig.class)
@DisplayName("UserPointHistoryJpaRepository 테스트")
class UserPointHistoryJpaRepositoryTest {

    @Autowired
    private UserPointHistoryJpaRepository repository;

    @PersistenceContext
    private EntityManager em;

    private UserPointHistoryJpaEntity persistHistory(
            Long userId, UserPointChangeType changeType, long amount,
            UserPointSourceType sourceType, Long sourceId, String reason,
            boolean isReflected, LocalDateTime accumulatedAt
    ) {
        UserPointHistory domain = UserPointHistory.from(UserPointHistoryCreateState.builder()
                .userId(userId)
                .changeType(changeType)
                .amount(PointAmount.of(amount))
                .isReflected(isReflected)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .reason(reason)
                .accumulatedAt(accumulatedAt)
                .build());
        UserPointHistoryJpaEntity entity = UserPointHistoryJpaEntity.from(domain);
        em.persist(entity);
        em.flush();
        return entity;
    }

    @Test
    @DisplayName("findByUserIdAndDateRange는 기간 내 모든 이력을 accumulatedAt desc 정렬로 반환한다")
    void shouldFindByUserIdAndDateRange() {
        persistHistory(1L, UserPointChangeType.ACCRUAL, 1000L, UserPointSourceType.ORDER, 100L,
                "주문 적립", true, LocalDateTime.of(2026, 4, 10, 12, 0));
        persistHistory(1L, UserPointChangeType.DEDUCTION, 500L, UserPointSourceType.ORDER, 101L,
                "주문 사용", true, LocalDateTime.of(2026, 4, 11, 12, 0));
        em.clear();

        List<UserPointHistoryJpaEntity> result = repository.findByUserIdAndDateRange(
                1L,
                LocalDateTime.of(2026, 4, 1, 0, 0),
                LocalDateTime.of(2026, 4, 30, 0, 0),
                null,
                PageRequest.of(0, 10)
        );

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findByUserIdAndDateRangeAndChangeType는 changeType 조건이 추가되어 필터링된다")
    void shouldFindByUserIdAndDateRangeAndChangeType() {
        persistHistory(1L, UserPointChangeType.ACCRUAL, 1000L, UserPointSourceType.ORDER, 100L,
                "적립", true, LocalDateTime.of(2026, 4, 10, 12, 0));
        persistHistory(1L, UserPointChangeType.DEDUCTION, 500L, UserPointSourceType.ORDER, 101L,
                "사용", true, LocalDateTime.of(2026, 4, 11, 12, 0));
        em.clear();

        List<UserPointHistoryJpaEntity> result = repository.findByUserIdAndDateRangeAndChangeType(
                1L,
                LocalDateTime.of(2026, 4, 1, 0, 0),
                LocalDateTime.of(2026, 4, 30, 0, 0),
                UserPointChangeType.ACCRUAL,
                null,
                PageRequest.of(0, 10)
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getChangeType()).isEqualTo(UserPointChangeType.ACCRUAL);
    }

    @Test
    @DisplayName("countByUserIdAndDateRange는 기간 내 이력 수를 반환한다")
    void shouldCountByUserIdAndDateRange() {
        persistHistory(1L, UserPointChangeType.ACCRUAL, 1000L, UserPointSourceType.ORDER, 100L,
                "적립", true, LocalDateTime.of(2026, 4, 10, 12, 0));
        persistHistory(1L, UserPointChangeType.DEDUCTION, 500L, UserPointSourceType.ORDER, 101L,
                "사용", true, LocalDateTime.of(2026, 4, 11, 12, 0));
        em.clear();

        long count = repository.countByUserIdAndDateRange(
                1L,
                LocalDateTime.of(2026, 4, 1, 0, 0),
                LocalDateTime.of(2026, 4, 30, 0, 0)
        );
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("countByUserIdAndDateRangeAndChangeType는 changeType 조건의 이력 수를 반환한다")
    void shouldCountByUserIdAndDateRangeAndChangeType() {
        persistHistory(1L, UserPointChangeType.ACCRUAL, 1000L, UserPointSourceType.ORDER, 100L,
                "적립", true, LocalDateTime.of(2026, 4, 10, 12, 0));
        persistHistory(1L, UserPointChangeType.DEDUCTION, 500L, UserPointSourceType.ORDER, 101L,
                "사용", true, LocalDateTime.of(2026, 4, 11, 12, 0));
        em.clear();

        long count = repository.countByUserIdAndDateRangeAndChangeType(
                1L,
                LocalDateTime.of(2026, 4, 1, 0, 0),
                LocalDateTime.of(2026, 4, 30, 0, 0),
                UserPointChangeType.DEDUCTION
        );
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("findByUserIdAndIsReflectedAndSourceTypeAndSourceId는 미반영 이력만 조회한다")
    void shouldFindUnreflected() {
        persistHistory(1L, UserPointChangeType.ACCRUAL, 1000L, UserPointSourceType.ORDER, 100L,
                "적립", false, LocalDateTime.now());
        persistHistory(1L, UserPointChangeType.ACCRUAL, 2000L, UserPointSourceType.ORDER, 100L,
                "적립 보너스", true, LocalDateTime.now());
        em.clear();

        List<UserPointHistoryJpaEntity> result = repository.findByUserIdAndIsReflectedAndSourceTypeAndSourceId(
                1L, Boolean.FALSE, UserPointSourceType.ORDER, 100L
        );
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("markAsReflected는 미반영 이력만 반영 처리한 갱신 건수를 반환한다")
    void shouldMarkAsReflected() {
        persistHistory(1L, UserPointChangeType.ACCRUAL, 1000L, UserPointSourceType.ORDER, 100L,
                "적립1", false, LocalDateTime.now());
        persistHistory(1L, UserPointChangeType.ACCRUAL, 2000L, UserPointSourceType.ORDER, 100L,
                "적립2", false, LocalDateTime.now());

        int updated = repository.markAsReflected(1L, UserPointSourceType.ORDER, 100L);
        assertThat(updated).isEqualTo(2);
    }

    @Test
    @DisplayName("countByUserIdAndSourceTypeAndReason는 USER 소스 + 추천 적립 사유 이력 수를 반환한다")
    void shouldCountReferrals() {
        persistHistory(1L, UserPointChangeType.ACCRUAL, 1000L, UserPointSourceType.USER, 2L,
                REFERRER_POINT_REASON, true, LocalDateTime.now());
        persistHistory(1L, UserPointChangeType.ACCRUAL, 1000L, UserPointSourceType.USER, 3L,
                REFERRER_POINT_REASON, true, LocalDateTime.now());
        em.clear();

        long count = repository.countByUserIdAndSourceTypeAndReason(
                1L, UserPointSourceType.USER, REFERRER_POINT_REASON);
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("existsByUserIdAndSourceTypeAndSourceIdAndReason는 동일 사유의 이력 존재 여부를 반환한다")
    void shouldExistsByUserIdSourceReason() {
        persistHistory(1L, UserPointChangeType.ACCRUAL, 1000L, UserPointSourceType.USER, 1L,
                "친구 초대 누적 보너스 (5명)", true, LocalDateTime.now());
        em.clear();

        assertThat(repository.existsByUserIdAndSourceTypeAndSourceIdAndReason(
                1L, UserPointSourceType.USER, 1L, "친구 초대 누적 보너스 (5명)")).isTrue();
    }

    @Test
    @DisplayName("sumReferralEarnedAmount는 기본 사유와 누적 보너스 사유 합계를 반환한다")
    void shouldSumReferralEarnedAmount() {
        persistHistory(1L, UserPointChangeType.ACCRUAL, 1000L, UserPointSourceType.USER, 2L,
                REFERRER_POINT_REASON, true, LocalDateTime.now());
        persistHistory(1L, UserPointChangeType.ACCRUAL, 5000L, UserPointSourceType.USER, 1L,
                "친구 초대 누적 보너스 (5명)", true, LocalDateTime.now());
        em.clear();

        long sum = repository.sumReferralEarnedAmount(
                1L, UserPointSourceType.USER, REFERRER_POINT_REASON, REFERRAL_BONUS_REASON_PREFIX);
        assertThat(sum).isEqualTo(6000L);
    }
}
