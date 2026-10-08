package com.personal.marketnote.reward.adapter.out.persistence.servicecommunication;

import com.personal.marketnote.reward.adapter.out.persistence.servicecommunication.entity.RewardServiceCommunicationHistoryJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.servicecommunication.repository.RewardServiceCommunicationHistoryJpaRepository;
import com.personal.marketnote.reward.domain.servicecommunication.RewardServiceCommunicationHistory;
import com.personal.marketnote.reward.domain.servicecommunication.RewardServiceCommunicationHistoryCreateState;
import com.personal.marketnote.reward.domain.servicecommunication.RewardServiceCommunicationSenderType;
import com.personal.marketnote.reward.domain.servicecommunication.RewardServiceCommunicationTargetType;
import com.personal.marketnote.reward.domain.servicecommunication.RewardServiceCommunicationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("RewardServiceCommunicationHistoryPersistenceAdapter 테스트")
class RewardServiceCommunicationHistoryPersistenceAdapterTest {

    @InjectMocks
    private RewardServiceCommunicationHistoryPersistenceAdapter adapter;

    @Mock
    private RewardServiceCommunicationHistoryJpaRepository repository;

    @Test
    @DisplayName("정상 저장 시 저장된 엔티티를 도메인 객체로 변환하여 반환한다")
    void savesAndReturnsDomainFromEntity() {
        // given
        RewardServiceCommunicationHistory history = buildHistory();
        LocalDateTime savedAt = LocalDateTime.of(2026, 4, 15, 10, 0);
        given(repository.save(any(RewardServiceCommunicationHistoryJpaEntity.class)))
                .willAnswer(invocation -> {
                    RewardServiceCommunicationHistoryJpaEntity arg = invocation.getArgument(0);
                    ReflectionTestUtils.setField(arg, "id", 999L);
                    ReflectionTestUtils.setField(arg, "createdAt", savedAt);
                    return arg;
                });

        // when
        RewardServiceCommunicationHistory saved = adapter.save(history);

        // then
        assertThat(saved.getId()).isEqualTo(999L);
        assertThat(saved.getTargetType()).isEqualTo(RewardServiceCommunicationTargetType.GENERAL);
        assertThat(saved.getTargetId()).isEqualTo("target-42");
        assertThat(saved.getCommunicationType()).isEqualTo(RewardServiceCommunicationType.REQUEST);
        assertThat(saved.getSender()).isEqualTo(RewardServiceCommunicationSenderType.USER);
        assertThat(saved.getException()).isEqualTo("TimeoutException");
        assertThat(saved.getPayload()).isEqualTo("payload-body");
        assertThat(saved.getCreatedAt()).isEqualTo(savedAt);
    }

    @Test
    @DisplayName("도메인 객체를 JPA 엔티티로 변환한 뒤 리포지토리에 위임한다")
    void delegatesSaveWithMappedEntity() {
        // given
        RewardServiceCommunicationHistory history = buildHistory();
        given(repository.save(any(RewardServiceCommunicationHistoryJpaEntity.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        adapter.save(history);

        // then
        ArgumentCaptor<RewardServiceCommunicationHistoryJpaEntity> captor =
                ArgumentCaptor.forClass(RewardServiceCommunicationHistoryJpaEntity.class);
        verify(repository).save(captor.capture());
        verifyNoMoreInteractions(repository);
        RewardServiceCommunicationHistoryJpaEntity captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(RewardServiceCommunicationTargetType.GENERAL);
        assertThat(captured.getTargetId()).isEqualTo("target-42");
        assertThat(captured.getCommunicationType()).isEqualTo(RewardServiceCommunicationType.REQUEST);
        assertThat(captured.getSender()).isEqualTo(RewardServiceCommunicationSenderType.USER);
        assertThat(captured.getException()).isEqualTo("TimeoutException");
        assertThat(captured.getPayload()).isEqualTo("payload-body");
    }

    private RewardServiceCommunicationHistory buildHistory() {
        return RewardServiceCommunicationHistory.from(RewardServiceCommunicationHistoryCreateState.builder()
                .targetType(RewardServiceCommunicationTargetType.GENERAL)
                .targetId("target-42")
                .communicationType(RewardServiceCommunicationType.REQUEST)
                .sender(RewardServiceCommunicationSenderType.USER)
                .exception("TimeoutException")
                .payload("payload-body")
                .payloadJson(null)
                .build());
    }

    private static <T> T any(Class<T> type) {
        return org.mockito.ArgumentMatchers.any(type);
    }
}
