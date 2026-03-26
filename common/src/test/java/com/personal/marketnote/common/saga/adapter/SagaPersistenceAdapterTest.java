package com.personal.marketnote.common.saga.adapter;

import com.personal.marketnote.common.saga.*;
import com.personal.marketnote.common.saga.entity.SagaInstanceJpaEntity;
import com.personal.marketnote.common.saga.entity.SagaStepJpaEntity;
import com.personal.marketnote.common.saga.exception.SagaInstanceNotFoundException;
import com.personal.marketnote.common.saga.exception.SagaStepNotFoundException;
import com.personal.marketnote.common.saga.repository.SagaInstanceJpaRepository;
import com.personal.marketnote.common.saga.repository.SagaStepJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SagaPersistenceAdapter 테스트")
class SagaPersistenceAdapterTest {

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 3, 16, 10, 0, 0);

    @InjectMocks
    private SagaPersistenceAdapter sagaPersistenceAdapter;

    @Mock
    private SagaInstanceJpaRepository sagaInstanceJpaRepository;

    @Mock
    private SagaStepJpaRepository sagaStepJpaRepository;

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("SagaInstance를 JPA 엔티티로 변환하여 저장하고 도메인 객체를 반환한다")
        void save_convertsToEntityAndReturnsDomain() {
            // given
            SagaInstance instance = createSagaInstance(null, "saga-001", "ORDER_PAYMENT",
                    SagaStatus.STARTED, 0, "{\"orderId\":1}", null);

            when(sagaInstanceJpaRepository.save(any(SagaInstanceJpaEntity.class)))
                    .thenAnswer(invocation -> {
                        SagaInstanceJpaEntity entity = invocation.getArgument(0);
                        ReflectionTestUtils.setField(entity, "id", 1L);
                        ReflectionTestUtils.setField(entity, "createdAt", FIXED_TIME);
                        ReflectionTestUtils.setField(entity, "modifiedAt", FIXED_TIME);
                        return entity;
                    });

            // when
            SagaInstance result = sagaPersistenceAdapter.save(instance);

            // then
            ArgumentCaptor<SagaInstanceJpaEntity> captor = ArgumentCaptor.forClass(SagaInstanceJpaEntity.class);
            verify(sagaInstanceJpaRepository).save(captor.capture());

            SagaInstanceJpaEntity savedEntity = captor.getValue();
            assertThat(savedEntity.getSagaId()).isEqualTo("saga-001");
            assertThat(savedEntity.getSagaType()).isEqualTo("ORDER_PAYMENT");
            assertThat(savedEntity.getStatus()).isEqualTo(SagaStatus.STARTED);
            assertThat(savedEntity.getCurrentStepIndex()).isZero();
            assertThat(savedEntity.getPayload()).isEqualTo("{\"orderId\":1}");

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getSagaId()).isEqualTo("saga-001");
            assertThat(result.getSagaType()).isEqualTo("ORDER_PAYMENT");
            assertThat(result.getStatus()).isEqualTo(SagaStatus.STARTED);

            verifyNoMoreInteractions(sagaInstanceJpaRepository);
            verifyNoInteractions(sagaStepJpaRepository);
        }
    }

    @Nested
    @DisplayName("saveStep")
    class SaveStep {

        @Test
        @DisplayName("SagaStep을 JPA 엔티티로 변환하여 저장하고 도메인 객체를 반환한다")
        void saveStep_convertsToEntityAndReturnsDomain() {
            // given
            SagaStep step = createSagaStep(null, 1L, "DEDUCT_INVENTORY", 0,
                    SagaStepStatus.PENDING, "{\"productId\":10}", null, null, null);

            when(sagaStepJpaRepository.save(any(SagaStepJpaEntity.class)))
                    .thenAnswer(invocation -> {
                        SagaStepJpaEntity entity = invocation.getArgument(0);
                        ReflectionTestUtils.setField(entity, "id", 1L);
                        ReflectionTestUtils.setField(entity, "createdAt", FIXED_TIME);
                        ReflectionTestUtils.setField(entity, "modifiedAt", FIXED_TIME);
                        return entity;
                    });

            // when
            SagaStep result = sagaPersistenceAdapter.saveStep(step);

            // then
            ArgumentCaptor<SagaStepJpaEntity> captor = ArgumentCaptor.forClass(SagaStepJpaEntity.class);
            verify(sagaStepJpaRepository).save(captor.capture());

            SagaStepJpaEntity savedEntity = captor.getValue();
            assertThat(savedEntity.getSagaInstanceId()).isEqualTo(1L);
            assertThat(savedEntity.getStepName()).isEqualTo("DEDUCT_INVENTORY");
            assertThat(savedEntity.getStepIndex()).isZero();
            assertThat(savedEntity.getStatus()).isEqualTo(SagaStepStatus.PENDING);
            assertThat(savedEntity.getRequest()).isEqualTo("{\"productId\":10}");

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getSagaInstanceId()).isEqualTo(1L);
            assertThat(result.getStepName()).isEqualTo("DEDUCT_INVENTORY");

            verifyNoMoreInteractions(sagaStepJpaRepository);
            verifyNoInteractions(sagaInstanceJpaRepository);
        }
    }

    @Nested
    @DisplayName("findBySagaId")
    class FindBySagaId {

        @Test
        @DisplayName("sagaId로 SagaInstance를 조회하면 도메인 객체를 반환한다")
        void findBySagaId_returnsDomainWhenExists() {
            // given
            SagaInstanceJpaEntity entity = createSagaInstanceEntity(1L, "saga-001",
                    "ORDER_PAYMENT", SagaStatus.PROCESSING, 2, "{\"orderId\":1}");

            when(sagaInstanceJpaRepository.findBySagaId("saga-001"))
                    .thenReturn(Optional.of(entity));

            // when
            Optional<SagaInstance> result = sagaPersistenceAdapter.findBySagaId("saga-001");

            // then
            assertThat(result).isPresent();
            SagaInstance instance = result.get();
            assertThat(instance.getId()).isEqualTo(1L);
            assertThat(instance.getSagaId()).isEqualTo("saga-001");
            assertThat(instance.getSagaType()).isEqualTo("ORDER_PAYMENT");
            assertThat(instance.getStatus()).isEqualTo(SagaStatus.PROCESSING);
            assertThat(instance.getCurrentStepIndex()).isEqualTo(2);
            assertThat(instance.getPayload()).isEqualTo("{\"orderId\":1}");

            verify(sagaInstanceJpaRepository).findBySagaId("saga-001");
            verifyNoMoreInteractions(sagaInstanceJpaRepository);
        }

        @Test
        @DisplayName("존재하지 않는 sagaId로 조회하면 빈 Optional을 반환한다")
        void findBySagaId_returnsEmptyWhenNotExists() {
            // given
            when(sagaInstanceJpaRepository.findBySagaId("non-existent"))
                    .thenReturn(Optional.empty());

            // when
            Optional<SagaInstance> result = sagaPersistenceAdapter.findBySagaId("non-existent");

            // then
            assertThat(result).isEmpty();

            verify(sagaInstanceJpaRepository).findBySagaId("non-existent");
            verifyNoMoreInteractions(sagaInstanceJpaRepository);
        }
    }

    @Nested
    @DisplayName("findStepsBySagaInstanceId")
    class FindStepsBySagaInstanceId {

        @Test
        @DisplayName("sagaInstanceId로 SagaStep 목록을 stepIndex 순으로 조회한다")
        void findStepsBySagaInstanceId_returnsSortedSteps() {
            // given
            SagaStepJpaEntity entity1 = createSagaStepEntity(1L, 1L, "DEDUCT_INVENTORY", 0,
                    SagaStepStatus.SUCCEEDED, "{\"productId\":10}", "{\"success\":true}", null, null);
            SagaStepJpaEntity entity2 = createSagaStepEntity(2L, 1L, "PROCESS_PAYMENT", 1,
                    SagaStepStatus.PROCESSING, "{\"amount\":5000}", null, null, null);

            when(sagaStepJpaRepository.findBySagaInstanceIdOrderByStepIndexAsc(1L))
                    .thenReturn(List.of(entity1, entity2));

            // when
            List<SagaStep> result = sagaPersistenceAdapter.findStepsBySagaInstanceId(1L);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getStepName()).isEqualTo("DEDUCT_INVENTORY");
            assertThat(result.get(0).getStepIndex()).isZero();
            assertThat(result.get(0).getStatus()).isEqualTo(SagaStepStatus.SUCCEEDED);
            assertThat(result.get(1).getStepName()).isEqualTo("PROCESS_PAYMENT");
            assertThat(result.get(1).getStepIndex()).isEqualTo(1);
            assertThat(result.get(1).getStatus()).isEqualTo(SagaStepStatus.PROCESSING);

            verify(sagaStepJpaRepository).findBySagaInstanceIdOrderByStepIndexAsc(1L);
            verifyNoMoreInteractions(sagaStepJpaRepository);
        }

        @Test
        @DisplayName("해당 sagaInstanceId에 SagaStep이 없으면 빈 목록을 반환한다")
        void findStepsBySagaInstanceId_returnsEmptyListWhenNoSteps() {
            // given
            when(sagaStepJpaRepository.findBySagaInstanceIdOrderByStepIndexAsc(999L))
                    .thenReturn(List.of());

            // when
            List<SagaStep> result = sagaPersistenceAdapter.findStepsBySagaInstanceId(999L);

            // then
            assertThat(result).isEmpty();

            verify(sagaStepJpaRepository).findBySagaInstanceIdOrderByStepIndexAsc(999L);
            verifyNoMoreInteractions(sagaStepJpaRepository);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("SagaInstance 업데이트 시 기존 엔티티를 조회하여 상태를 갱신한다")
        void update_findsEntityAndUpdatesState() {
            // given
            SagaInstanceJpaEntity existingEntity = createSagaInstanceEntity(1L, "saga-001",
                    "ORDER_PAYMENT", SagaStatus.STARTED, 0, "{\"orderId\":1}");

            LocalDateTime completedAt = FIXED_TIME.plusHours(1);
            SagaInstance updatedInstance = createSagaInstance(1L, "saga-001", "ORDER_PAYMENT",
                    SagaStatus.SUCCEEDED, 3, "{\"orderId\":1}", completedAt);

            when(sagaInstanceJpaRepository.findById(1L))
                    .thenReturn(Optional.of(existingEntity));

            // when
            sagaPersistenceAdapter.update(updatedInstance);

            // then
            assertThat(existingEntity.getStatus()).isEqualTo(SagaStatus.SUCCEEDED);
            assertThat(existingEntity.getCurrentStepIndex()).isEqualTo(3);
            assertThat(existingEntity.getCompletedAt()).isEqualTo(completedAt);

            verify(sagaInstanceJpaRepository).findById(1L);
            verifyNoMoreInteractions(sagaInstanceJpaRepository);
        }

        @Test
        @DisplayName("존재하지 않는 SagaInstance 업데이트 시 SagaInstanceNotFoundException이 발생한다")
        void update_throwsExceptionWhenNotFound() {
            // given
            SagaInstance instance = createSagaInstance(1L, "saga-001", "ORDER_PAYMENT",
                    SagaStatus.PROCESSING, 1, "{\"orderId\":1}", null);

            when(sagaInstanceJpaRepository.findById(1L))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sagaPersistenceAdapter.update(instance))
                    .isInstanceOf(SagaInstanceNotFoundException.class);

            verify(sagaInstanceJpaRepository).findById(1L);
            verifyNoMoreInteractions(sagaInstanceJpaRepository);
        }
    }

    @Nested
    @DisplayName("updateStep")
    class UpdateStep {

        @Test
        @DisplayName("SagaStep 업데이트 시 기존 엔티티를 조회하여 상태를 갱신한다")
        void updateStep_findsEntityAndUpdatesState() {
            // given
            SagaStepJpaEntity existingEntity = createSagaStepEntity(1L, 1L, "DEDUCT_INVENTORY", 0,
                    SagaStepStatus.PENDING, "{\"productId\":10}", null, null, null);

            SagaStep updatedStep = createSagaStep(1L, 1L, "DEDUCT_INVENTORY", 0,
                    SagaStepStatus.SUCCEEDED, "{\"productId\":10}", "{\"success\":true}",
                    null, null);

            when(sagaStepJpaRepository.findById(1L))
                    .thenReturn(Optional.of(existingEntity));

            // when
            sagaPersistenceAdapter.updateStep(updatedStep);

            // then
            assertThat(existingEntity.getStatus()).isEqualTo(SagaStepStatus.SUCCEEDED);
            assertThat(existingEntity.getResponse()).isEqualTo("{\"success\":true}");
            assertThat(existingEntity.getCompensationRequest()).isNull();
            assertThat(existingEntity.getCompensationResponse()).isNull();

            verify(sagaStepJpaRepository).findById(1L);
            verifyNoMoreInteractions(sagaStepJpaRepository);
        }

        @Test
        @DisplayName("SagaStep 보상 트랜잭션 업데이트 시 compensationRequest와 compensationResponse가 갱신된다")
        void updateStep_updatesCompensationFields() {
            // given
            SagaStepJpaEntity existingEntity = createSagaStepEntity(1L, 1L, "DEDUCT_INVENTORY", 0,
                    SagaStepStatus.SUCCEEDED, "{\"productId\":10}", "{\"success\":true}", null, null);

            SagaStep updatedStep = createSagaStep(1L, 1L, "DEDUCT_INVENTORY", 0,
                    SagaStepStatus.COMPENSATED, "{\"productId\":10}", "{\"success\":true}",
                    "{\"rollbackProductId\":10}", "{\"rollbackSuccess\":true}");

            when(sagaStepJpaRepository.findById(1L))
                    .thenReturn(Optional.of(existingEntity));

            // when
            sagaPersistenceAdapter.updateStep(updatedStep);

            // then
            assertThat(existingEntity.getStatus()).isEqualTo(SagaStepStatus.COMPENSATED);
            assertThat(existingEntity.getCompensationRequest()).isEqualTo("{\"rollbackProductId\":10}");
            assertThat(existingEntity.getCompensationResponse()).isEqualTo("{\"rollbackSuccess\":true}");

            verify(sagaStepJpaRepository).findById(1L);
            verifyNoMoreInteractions(sagaStepJpaRepository);
        }

        @Test
        @DisplayName("존재하지 않는 SagaStep 업데이트 시 SagaStepNotFoundException이 발생한다")
        void updateStep_throwsExceptionWhenNotFound() {
            // given
            SagaStep step = createSagaStep(1L, 1L, "DEDUCT_INVENTORY", 0,
                    SagaStepStatus.SUCCEEDED, "{\"productId\":10}", "{\"success\":true}",
                    null, null);

            when(sagaStepJpaRepository.findById(1L))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sagaPersistenceAdapter.updateStep(step))
                    .isInstanceOf(SagaStepNotFoundException.class);

            verify(sagaStepJpaRepository).findById(1L);
            verifyNoMoreInteractions(sagaStepJpaRepository);
        }
    }

    private SagaInstance createSagaInstance(Long id, String sagaId, String sagaType,
                                            SagaStatus status, int currentStepIndex,
                                            String payload, LocalDateTime completedAt) {
        return SagaInstance.from(new SagaInstanceSnapshotState(
                id, sagaId, sagaType, status, currentStepIndex, payload,
                FIXED_TIME, FIXED_TIME, completedAt
        ));
    }

    private SagaStep createSagaStep(Long id, Long sagaInstanceId, String stepName,
                                    int stepIndex, SagaStepStatus status, String request,
                                    String response, String compensationRequest,
                                    String compensationResponse) {
        return SagaStep.from(new SagaStepSnapshotState(
                id, sagaInstanceId, stepName, stepIndex, status, request,
                response, compensationRequest, compensationResponse,
                FIXED_TIME, FIXED_TIME
        ));
    }

    private SagaInstanceJpaEntity createSagaInstanceEntity(Long id, String sagaId, String sagaType,
                                                           SagaStatus status, int currentStepIndex,
                                                           String payload) {
        SagaInstance instance = createSagaInstance(null, sagaId, sagaType, status,
                currentStepIndex, payload, null);
        SagaInstanceJpaEntity entity = SagaInstanceJpaEntity.from(instance);
        ReflectionTestUtils.setField(entity, "id", id);
        ReflectionTestUtils.setField(entity, "createdAt", FIXED_TIME);
        ReflectionTestUtils.setField(entity, "modifiedAt", FIXED_TIME);
        return entity;
    }

    private SagaStepJpaEntity createSagaStepEntity(Long id, Long sagaInstanceId, String stepName,
                                                   int stepIndex, SagaStepStatus status,
                                                   String request, String response,
                                                   String compensationRequest,
                                                   String compensationResponse) {
        SagaStep step = createSagaStep(null, sagaInstanceId, stepName, stepIndex, status,
                request, null, null, null);
        SagaStepJpaEntity entity = SagaStepJpaEntity.from(step);
        ReflectionTestUtils.setField(entity, "id", id);
        ReflectionTestUtils.setField(entity, "createdAt", FIXED_TIME);
        ReflectionTestUtils.setField(entity, "modifiedAt", FIXED_TIME);
        ReflectionTestUtils.setField(entity, "response", response);
        ReflectionTestUtils.setField(entity, "compensationRequest", compensationRequest);
        ReflectionTestUtils.setField(entity, "compensationResponse", compensationResponse);
        return entity;
    }
}
