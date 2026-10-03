package com.personal.marketnote.reward.adapter.out.persistence.attendance;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.reward.adapter.out.persistence.attendance.entity.AttendancePolicyJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.attendance.repository.AttendancePolicyJpaRepository;
import com.personal.marketnote.reward.domain.attendance.AttendancePolicy;
import com.personal.marketnote.reward.domain.attendance.AttendancePolicySnapshotState;
import com.personal.marketnote.reward.domain.attendance.AttendanceRewardType;
import com.personal.marketnote.reward.domain.attendance.ContinuousPeriod;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendancePolicyPersistenceAdapter 테스트")
class AttendancePolicyPersistenceAdapterTest {

    @Mock
    private AttendancePolicyJpaRepository repository;

    @InjectMocks
    private AttendancePolicyPersistenceAdapter adapter;

    private AttendancePolicyJpaEntity buildEntity(Short id, short continuousPeriod) {
        AttendancePolicy restored = AttendancePolicy.from(AttendancePolicySnapshotState.builder()
                .id(id)
                .continuousPeriod(ContinuousPeriod.of(continuousPeriod))
                .rewardType(AttendanceRewardType.POINT)
                .rewardQuantity(RewardQuantity.of(100L))
                .attendenceDate(null)
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
        AttendancePolicyJpaEntity entity = AttendancePolicyJpaEntity.from(restored);
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    @Test
    @DisplayName("findById는 엔티티를 도메인으로 변환한다")
    void shouldFindById() {
        given(repository.findById((short) 1)).willReturn(Optional.of(buildEntity((short) 1, (short) 1)));
        Optional<AttendancePolicy> result = adapter.findById((short) 1);
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("findByIdForUpdate는 비관적 잠금 조회 결과를 도메인으로 반환한다")
    void shouldFindByIdForUpdate() {
        given(repository.findWithLockingById((short) 1))
                .willReturn(Optional.of(buildEntity((short) 1, (short) 1)));
        Optional<AttendancePolicy> result = adapter.findByIdForUpdate((short) 1);
        assertThat(result).isPresent();
        verify(repository).findWithLockingById((short) 1);
    }

    @Test
    @DisplayName("findByContinuousPeriodAndAttendenceDate는 연속일/지정일 정책을 조회한다")
    void shouldFindByContinuousPeriodAndAttendenceDate() {
        LocalDate date = LocalDate.of(2026, 4, 14);
        given(repository.findTop1ByContinuousPeriodAndAttendenceDate((short) 5, date))
                .willReturn(Optional.of(buildEntity((short) 1, (short) 5)));
        Optional<AttendancePolicy> result = adapter.findByContinuousPeriodAndAttendenceDate((short) 5, date);
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("findByContinuousPeriodAndAttendenceDateIsNull은 일반 정책을 orderNum desc로 조회한다")
    void shouldFindByContinuousPeriodAndAttendenceDateIsNull() {
        given(repository.findTop1ByContinuousPeriodAndAttendenceDateIsNullOrderByOrderNumDesc((short) 5))
                .willReturn(Optional.of(buildEntity((short) 1, (short) 5)));
        Optional<AttendancePolicy> result = adapter.findByContinuousPeriodAndAttendenceDateIsNull((short) 5);
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("save는 저장 후 setIdToOrderNum을 호출하고 도메인을 반환한다")
    void shouldSaveAndAssignOrderNum() {
        // given
        AttendancePolicyJpaEntity savedEntity = buildEntity((short) 7, (short) 1);
        given(repository.save(any(AttendancePolicyJpaEntity.class))).willReturn(savedEntity);

        // when
        AttendancePolicy result = adapter.save(savedEntity.toDomain());

        // then
        assertThat(result.getId()).isEqualTo((short) 7);
        assertThat(savedEntity.getOrderNum()).isEqualTo((short) 7);
    }

    @Nested
    @DisplayName("update")
    class UpdateTest {

        @Test
        @DisplayName("기존 엔티티가 존재하면 updateFrom으로 도메인 상태를 반영한다")
        void shouldUpdateExistingEntity() {
            AttendancePolicyJpaEntity existing = buildEntity((short) 1, (short) 1);
            given(repository.findById((short) 1)).willReturn(Optional.of(existing));
            adapter.update(existing.toDomain());
            verify(repository).findById((short) 1);
        }

        @Test
        @DisplayName("기존 엔티티가 없으면 IllegalArgumentException을 던진다")
        void shouldThrowWhenNoEntity() {
            AttendancePolicyJpaEntity sample = buildEntity((short) 99, (short) 1);
            given(repository.findById((short) 99)).willReturn(Optional.empty());
            assertThatThrownBy(() -> adapter.update(sample.toDomain()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("findAllOrderByOrderNumDesc는 정렬된 정책 리스트를 반환한다")
    void shouldFindAllOrdered() {
        given(repository.findAllByOrderByOrderNumDesc())
                .willReturn(List.of(
                        buildEntity((short) 2, (short) 2),
                        buildEntity((short) 1, (short) 1)
                ));
        List<AttendancePolicy> result = adapter.findAllOrderByOrderNumDesc();
        assertThat(result).hasSize(2);
    }
}
