package com.personal.marketnote.notification.adapter.out.persistence.vendorcommunication;

import com.personal.marketnote.notification.adapter.out.persistence.vendorcommunication.entity.NotificationVendorCommunicationHistoryJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.vendorcommunication.repository.NotificationVendorCommunicationHistoryJpaRepository;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationHistory;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationHistoryCreateState;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationSenderType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationTargetType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorName;
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
@DisplayName("NotificationVendorCommunicationHistoryPersistenceAdapter 테스트")
class NotificationVendorCommunicationHistoryPersistenceAdapterTest {

    @InjectMocks
    private NotificationVendorCommunicationHistoryPersistenceAdapter adapter;

    @Mock
    private NotificationVendorCommunicationHistoryJpaRepository repository;

    @Test
    @DisplayName("save 호출 시 도메인을 JPA 엔티티로 변환하여 리포지토리에 위임한다")
    void shouldDelegateSaveWithMappedEntity() {
        NotificationVendorCommunicationHistory history = buildHistory();
        given(repository.save(any(NotificationVendorCommunicationHistoryJpaEntity.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        adapter.save(history);

        ArgumentCaptor<NotificationVendorCommunicationHistoryJpaEntity> captor =
                ArgumentCaptor.forClass(NotificationVendorCommunicationHistoryJpaEntity.class);
        verify(repository).save(captor.capture());
        verifyNoMoreInteractions(repository);
        NotificationVendorCommunicationHistoryJpaEntity captured = captor.getValue();
        assertThat(captured.getTargetType())
                .isEqualTo(NotificationVendorCommunicationTargetType.PUSH_NOTIFICATION);
        assertThat(captured.getTargetId()).isEqualTo("100");
        assertThat(captured.getVendorName()).isEqualTo(NotificationVendorName.FCM);
        assertThat(captured.getCommunicationType()).isEqualTo(NotificationVendorCommunicationType.REQUEST);
        assertThat(captured.getSender()).isEqualTo(NotificationVendorCommunicationSenderType.SERVER);
        assertThat(captured.getPayload()).isEqualTo("{\"k\":\"v\"}");
    }

    @Test
    @DisplayName("저장 후 리포지토리에서 반환된 엔티티를 도메인으로 변환해 id와 created_at을 포함해 반환한다")
    void shouldReturnDomainWithIdAndCreatedAt() {
        NotificationVendorCommunicationHistory history = buildHistory();
        LocalDateTime savedAt = LocalDateTime.of(2026, 4, 15, 12, 30);
        given(repository.save(any(NotificationVendorCommunicationHistoryJpaEntity.class)))
                .willAnswer(invocation -> {
                    NotificationVendorCommunicationHistoryJpaEntity arg = invocation.getArgument(0);
                    ReflectionTestUtils.setField(arg, "id", 777L);
                    ReflectionTestUtils.setField(arg, "createdAt", savedAt);
                    return arg;
                });

        NotificationVendorCommunicationHistory saved = adapter.save(history);

        assertThat(saved.getId()).isEqualTo(777L);
        assertThat(saved.getCreatedAt()).isEqualTo(savedAt);
        assertThat(saved.getTargetType())
                .isEqualTo(NotificationVendorCommunicationTargetType.PUSH_NOTIFICATION);
        assertThat(saved.getTargetId()).isEqualTo("100");
        assertThat(saved.getVendorName()).isEqualTo(NotificationVendorName.FCM);
        assertThat(saved.getCommunicationType()).isEqualTo(NotificationVendorCommunicationType.REQUEST);
        assertThat(saved.getSender()).isEqualTo(NotificationVendorCommunicationSenderType.SERVER);
        assertThat(saved.getException()).isEqualTo("UNREGISTERED");
        assertThat(saved.getPayload()).isEqualTo("{\"k\":\"v\"}");
        assertThat(saved.isFailure()).isTrue();
    }

    private NotificationVendorCommunicationHistory buildHistory() {
        return NotificationVendorCommunicationHistory.from(
                NotificationVendorCommunicationHistoryCreateState.builder()
                        .targetType(NotificationVendorCommunicationTargetType.PUSH_NOTIFICATION)
                        .targetId("100")
                        .vendorName(NotificationVendorName.FCM)
                        .communicationType(NotificationVendorCommunicationType.REQUEST)
                        .sender(NotificationVendorCommunicationSenderType.SERVER)
                        .exception("UNREGISTERED")
                        .payload("{\"k\":\"v\"}")
                        .payloadJson(null)
                        .build()
        );
    }
}
