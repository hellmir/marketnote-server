package com.personal.marketnote.reward.adapter.out.persistence.attendance;

import com.personal.marketnote.reward.adapter.out.persistence.attendance.entity.UserAttendanceHistoryJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.attendance.repository.UserAttendanceHistoryJpaRepository;
import com.personal.marketnote.reward.domain.attendance.AttendanceRewardType;
import com.personal.marketnote.reward.domain.attendance.ContinuousPeriod;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.domain.attendance.UserAttendanceHistory;
import com.personal.marketnote.reward.domain.attendance.UserAttendanceHistorySnapshotState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserAttendanceHistoryQueryAdapter 테스트")
class UserAttendanceHistoryQueryAdapterTest {

    @InjectMocks
    private UserAttendanceHistoryQueryAdapter adapter;

    @Mock
    private UserAttendanceHistoryJpaRepository repository;

    @Nested
    @DisplayName("findLatestByUserAttendanceId")
    class FindLatestByUserAttendanceId {

        @Test
        @DisplayName("최근 출석 이력이 존재하면 도메인 객체를 반환한다")
        void returnsDomainWhenHistoryExists() {
            // given
            Long userAttendanceId = 10L;
            LocalDate attendedDate = LocalDate.of(2026, 4, 14);
            UserAttendanceHistoryJpaEntity entity = buildEntity(100L, userAttendanceId, attendedDate);
            given(repository.findTop1ByUserAttendanceIdOrderByAttendedAtDesc(userAttendanceId))
                    .willReturn(Optional.of(entity));

            // when
            Optional<UserAttendanceHistory> result = adapter.findLatestByUserAttendanceId(userAttendanceId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(100L);
            assertThat(result.get().getUserAttendanceId()).isEqualTo(userAttendanceId);
            assertThat(result.get().getAttendedDate()).isEqualTo(attendedDate);
            verify(repository).findTop1ByUserAttendanceIdOrderByAttendedAtDesc(userAttendanceId);
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("최근 출석 이력이 없으면 Optional.empty를 반환한다")
        void returnsEmptyWhenHistoryAbsent() {
            // given
            Long userAttendanceId = 99L;
            given(repository.findTop1ByUserAttendanceIdOrderByAttendedAtDesc(userAttendanceId))
                    .willReturn(Optional.empty());

            // when
            Optional<UserAttendanceHistory> result = adapter.findLatestByUserAttendanceId(userAttendanceId);

            // then
            assertThat(result).isEmpty();
            verify(repository).findTop1ByUserAttendanceIdOrderByAttendedAtDesc(userAttendanceId);
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    @DisplayName("existsByUserAttendanceIdAndAttendedAtBetween")
    class ExistsByUserAttendanceIdAndAttendedAtBetween {

        @Test
        @DisplayName("구간 내 출석 이력이 존재하면 true를 반환한다")
        void returnsTrueWhenHistoryExistsInRange() {
            // given
            Long userAttendanceId = 10L;
            LocalDateTime startInclusive = LocalDate.of(2026, 4, 14).atStartOfDay();
            LocalDateTime endExclusive = LocalDate.of(2026, 4, 15).atStartOfDay();
            given(repository.existsByUserAttendanceIdAndAttendedAtGreaterThanEqualAndAttendedAtLessThan(
                    userAttendanceId, startInclusive, endExclusive
            )).willReturn(true);

            // when
            boolean result = adapter.existsByUserAttendanceIdAndAttendedAtBetween(
                    userAttendanceId, startInclusive, endExclusive
            );

            // then
            assertThat(result).isTrue();
            verify(repository).existsByUserAttendanceIdAndAttendedAtGreaterThanEqualAndAttendedAtLessThan(
                    userAttendanceId, startInclusive, endExclusive
            );
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("구간 내 출석 이력이 없으면 false를 반환한다")
        void returnsFalseWhenHistoryAbsentInRange() {
            // given
            Long userAttendanceId = 10L;
            LocalDateTime startInclusive = LocalDate.of(2026, 4, 14).atStartOfDay();
            LocalDateTime endExclusive = LocalDate.of(2026, 4, 15).atStartOfDay();
            given(repository.existsByUserAttendanceIdAndAttendedAtGreaterThanEqualAndAttendedAtLessThan(
                    userAttendanceId, startInclusive, endExclusive
            )).willReturn(false);

            // when
            boolean result = adapter.existsByUserAttendanceIdAndAttendedAtBetween(
                    userAttendanceId, startInclusive, endExclusive
            );

            // then
            assertThat(result).isFalse();
            verify(repository).existsByUserAttendanceIdAndAttendedAtGreaterThanEqualAndAttendedAtLessThan(
                    userAttendanceId, startInclusive, endExclusive
            );
            verifyNoMoreInteractions(repository);
        }
    }

    private UserAttendanceHistoryJpaEntity buildEntity(Long id, Long userAttendanceId, LocalDate attendedDate) {
        UserAttendanceHistory domain = UserAttendanceHistory.from(UserAttendanceHistorySnapshotState.builder()
                .id(id)
                .userAttendanceId(userAttendanceId)
                .attendancePolicyId((short) 1)
                .rewardType(AttendanceRewardType.POINT)
                .rewardQuantity(RewardQuantity.of(50L))
                .continuousPeriod(ContinuousPeriod.of((short) 1))
                .rewardYn(Boolean.TRUE)
                .attendedDate(attendedDate)
                .attendedAt(attendedDate.atStartOfDay())
                .build());
        UserAttendanceHistoryJpaEntity entity = UserAttendanceHistoryJpaEntity.from(domain);
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
