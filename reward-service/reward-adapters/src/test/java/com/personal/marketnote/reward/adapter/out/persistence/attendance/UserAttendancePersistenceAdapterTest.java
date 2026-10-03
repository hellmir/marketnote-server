package com.personal.marketnote.reward.adapter.out.persistence.attendance;

import com.personal.marketnote.common.domain.calendar.Month;
import com.personal.marketnote.common.domain.calendar.Year;
import com.personal.marketnote.reward.adapter.out.persistence.attendance.entity.UserAttendanceJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.attendance.repository.UserAttendanceJpaRepository;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.domain.attendance.UserAttendance;
import com.personal.marketnote.reward.domain.attendance.UserAttendanceSnapshotState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserAttendancePersistenceAdapter 테스트")
class UserAttendancePersistenceAdapterTest {

    private static final Long USER_ID = 1L;

    @Mock
    private UserAttendanceJpaRepository repository;

    @InjectMocks
    private UserAttendancePersistenceAdapter adapter;

    private UserAttendanceJpaEntity buildEntity(Long id) {
        UserAttendance restored = UserAttendance.from(UserAttendanceSnapshotState.builder()
                .id(id)
                .userId(USER_ID)
                .year(Year.Y2026)
                .month(Month.APRIL)
                .createdAt(LocalDateTime.now())
                .totalRewardQuantity(RewardQuantity.of(500L))
                .histories(null)
                .build());
        return UserAttendanceJpaEntity.from(restored);
    }

    @Test
    @DisplayName("save는 엔티티를 저장하고 도메인을 반환한다")
    void shouldSave() {
        // given
        UserAttendanceJpaEntity entity = buildEntity(1L);
        given(repository.save(any(UserAttendanceJpaEntity.class))).willReturn(entity);

        // when
        UserAttendance result = adapter.save(entity.toDomain());

        // then
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getTotalRewardQuantityValue()).isEqualTo(500L);
        verify(repository).save(any(UserAttendanceJpaEntity.class));
    }

    @Test
    @DisplayName("findByUserIdAndYearAndMonth는 연/월별 출석 정보를 도메인으로 반환한다")
    void shouldFindByUserIdAndYearAndMonth() {
        // given
        given(repository.findTop1ByUserIdAndYearAndMonth(USER_ID, Year.Y2026, Month.APRIL))
                .willReturn(Optional.of(buildEntity(1L)));

        // when
        Optional<UserAttendance> result = adapter.findByUserIdAndYearAndMonth(USER_ID, Year.Y2026, Month.APRIL);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getYear()).isEqualTo(Year.Y2026);
        assertThat(result.get().getMonth()).isEqualTo(Month.APRIL);
    }

    @Test
    @DisplayName("findByUserIdAndYearAndMonth는 결과가 없으면 빈 Optional을 반환한다")
    void shouldReturnEmptyWhenNoEntity() {
        given(repository.findTop1ByUserIdAndYearAndMonth(USER_ID, Year.Y2026, Month.APRIL))
                .willReturn(Optional.empty());
        assertThat(adapter.findByUserIdAndYearAndMonth(USER_ID, Year.Y2026, Month.APRIL)).isEmpty();
    }
}
