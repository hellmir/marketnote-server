package com.personal.marketnote.reward.domain.attendance;

import com.personal.marketnote.common.domain.calendar.Month;
import com.personal.marketnote.common.domain.calendar.Year;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserAttendanceTest {

    @Test
    @DisplayName("UserAttendanceCreateState로 UserAttendance를 생성하면 모든 필드가 매핑된다")
    void shouldCreateUserAttendanceFromCreateState() {
        UserAttendanceCreateState state = UserAttendanceCreateState.builder()
                .userId(1L)
                .year(Year.Y2026)
                .month(Month.APRIL)
                .totalRewardQuantity(100L)
                .histories(List.of())
                .build();

        UserAttendance attendance = UserAttendance.from(state);

        assertThat(attendance.getUserId()).isEqualTo(1L);
        assertThat(attendance.getYear()).isEqualTo(Year.Y2026);
        assertThat(attendance.getMonth()).isEqualTo(Month.APRIL);
        assertThat(attendance.getTotalRewardQuantity()).isEqualTo(100L);
        assertThat(attendance.getHistories()).isEmpty();
    }

    @Test
    @DisplayName("UserAttendanceSnapshotState로 UserAttendance를 복원하면 모든 필드가 그대로 매핑된다")
    void shouldRestoreUserAttendanceFromSnapshotState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 1, 0, 0);
        UserAttendanceSnapshotState state = UserAttendanceSnapshotState.builder()
                .id(10L)
                .userId(1L)
                .year(Year.Y2026)
                .month(Month.APRIL)
                .createdAt(createdAt)
                .totalRewardQuantity(500L)
                .histories(List.of())
                .build();

        UserAttendance attendance = UserAttendance.from(state);

        assertThat(attendance.getId()).isEqualTo(10L);
        assertThat(attendance.getUserId()).isEqualTo(1L);
        assertThat(attendance.getYear()).isEqualTo(Year.Y2026);
        assertThat(attendance.getMonth()).isEqualTo(Month.APRIL);
        assertThat(attendance.getCreatedAt()).isEqualTo(createdAt);
        assertThat(attendance.getTotalRewardQuantity()).isEqualTo(500L);
    }

    @Test
    @DisplayName("withAddedReward를 호출하면 리워드가 누적된 새 인스턴스를 반환한다")
    void shouldReturnNewInstanceWithAddedReward() {
        UserAttendance original = createAttendanceWithReward(100L);

        UserAttendance updated = original.withAddedReward(50L);

        assertThat(updated.getTotalRewardQuantity()).isEqualTo(150L);
        assertThat(original.getTotalRewardQuantity()).isEqualTo(100L);
    }

    @Test
    @DisplayName("withAddedReward를 호출하면 기존 필드가 보존된다")
    void shouldPreserveFieldsWhenAddingReward() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 1, 0, 0);
        List<UserAttendanceHistory> histories = List.of();
        UserAttendance original = UserAttendance.from(UserAttendanceSnapshotState.builder()
                .id(10L)
                .userId(1L)
                .year(Year.Y2026)
                .month(Month.APRIL)
                .createdAt(createdAt)
                .totalRewardQuantity(100L)
                .histories(histories)
                .build());

        UserAttendance updated = original.withAddedReward(30L);

        assertThat(updated.getId()).isEqualTo(10L);
        assertThat(updated.getUserId()).isEqualTo(1L);
        assertThat(updated.getYear()).isEqualTo(Year.Y2026);
        assertThat(updated.getMonth()).isEqualTo(Month.APRIL);
        assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updated.getHistories()).isSameAs(histories);
    }

    private UserAttendance createAttendanceWithReward(long totalRewardQuantity) {
        return UserAttendance.from(UserAttendanceCreateState.builder()
                .userId(1L)
                .year(Year.Y2026)
                .month(Month.APRIL)
                .totalRewardQuantity(totalRewardQuantity)
                .histories(List.of())
                .build());
    }
}
