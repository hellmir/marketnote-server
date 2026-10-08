package com.personal.marketnote.community.adapter.out.persistence.servicecommunication;

import com.personal.marketnote.community.adapter.out.persistence.servicecommunication.entity.CommunityServiceCommunicationHistoryJpaEntity;
import com.personal.marketnote.community.adapter.out.persistence.servicecommunication.repository.CommunityServiceCommunicationHistoryJpaRepository;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationHistory;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationHistoryCreateState;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationSenderType;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationTargetType;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationType;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommunityServiceCommunicationHistoryPersistenceAdapter")
class CommunityServiceCommunicationHistoryPersistenceAdapterTest {

    @InjectMocks
    private CommunityServiceCommunicationHistoryPersistenceAdapter adapter;

    @Mock
    private CommunityServiceCommunicationHistoryJpaRepository repository;

    @Test
    @DisplayName("저장된 엔티티를 도메인 객체로 변환해 반환한다")
    void savesAndReturnsDomainFromEntity() {
        CommunityServiceCommunicationHistory history = buildHistory();
        LocalDateTime savedAt = LocalDateTime.of(2026, 4, 15, 10, 0);
        given(repository.save(any(CommunityServiceCommunicationHistoryJpaEntity.class)))
                .willAnswer(invocation -> {
                    CommunityServiceCommunicationHistoryJpaEntity arg = invocation.getArgument(0);
                    ReflectionTestUtils.setField(arg, "id", 999L);
                    ReflectionTestUtils.setField(arg, "createdAt", savedAt);
                    return arg;
                });

        CommunityServiceCommunicationHistory saved = adapter.save(history);

        assertThat(saved.getId()).isEqualTo(999L);
        assertThat(saved.getTargetType()).isEqualTo(CommunityServiceCommunicationTargetType.PRODUCT_INFO);
        assertThat(saved.getTargetId()).isEqualTo("target-42");
        assertThat(saved.getCommunicationType()).isEqualTo(CommunityServiceCommunicationType.REQUEST);
        assertThat(saved.getSender()).isEqualTo(CommunityServiceCommunicationSenderType.PRODUCT);
        assertThat(saved.getException()).isEqualTo("TimeoutException");
        assertThat(saved.getPayload()).isEqualTo("payload-body");
        assertThat(saved.getCreatedAt()).isEqualTo(savedAt);
    }

    @Test
    @DisplayName("도메인 객체를 JPA 엔티티로 변환한 뒤 리포지토리에 위임한다")
    void delegatesSaveWithMappedEntity() {
        CommunityServiceCommunicationHistory history = buildHistory();
        given(repository.save(any(CommunityServiceCommunicationHistoryJpaEntity.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        adapter.save(history);

        ArgumentCaptor<CommunityServiceCommunicationHistoryJpaEntity> captor =
                ArgumentCaptor.forClass(CommunityServiceCommunicationHistoryJpaEntity.class);
        verify(repository).save(captor.capture());
        verifyNoMoreInteractions(repository);
        CommunityServiceCommunicationHistoryJpaEntity captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(CommunityServiceCommunicationTargetType.PRODUCT_INFO);
        assertThat(captured.getTargetId()).isEqualTo("target-42");
        assertThat(captured.getCommunicationType()).isEqualTo(CommunityServiceCommunicationType.REQUEST);
        assertThat(captured.getSender()).isEqualTo(CommunityServiceCommunicationSenderType.PRODUCT);
        assertThat(captured.getException()).isEqualTo("TimeoutException");
        assertThat(captured.getPayload()).isEqualTo("payload-body");
    }

    private CommunityServiceCommunicationHistory buildHistory() {
        return CommunityServiceCommunicationHistory.from(
                CommunityServiceCommunicationHistoryCreateState.builder()
                        .targetType(CommunityServiceCommunicationTargetType.PRODUCT_INFO)
                        .targetId("target-42")
                        .communicationType(CommunityServiceCommunicationType.REQUEST)
                        .sender(CommunityServiceCommunicationSenderType.PRODUCT)
                        .exception("TimeoutException")
                        .payload("payload-body")
                        .payloadJson(null)
                        .build()
        );
    }
}
