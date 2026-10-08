package com.personal.marketnote.user.adapter.out.persistence.servicecommunication;

import com.personal.marketnote.user.adapter.out.persistence.servicecommunication.entity.UserServiceCommunicationHistoryJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.servicecommunication.repository.UserServiceCommunicationHistoryJpaRepository;
import com.personal.marketnote.user.domain.servicecommunication.UserServiceCommunicationHistory;
import com.personal.marketnote.user.domain.servicecommunication.UserServiceCommunicationHistoryCreateState;
import com.personal.marketnote.user.domain.servicecommunication.UserServiceCommunicationHistorySnapshotState;
import com.personal.marketnote.user.domain.servicecommunication.UserServiceCommunicationSenderType;
import com.personal.marketnote.user.domain.servicecommunication.UserServiceCommunicationTargetType;
import com.personal.marketnote.user.domain.servicecommunication.UserServiceCommunicationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceCommunicationHistoryPersistenceAdapter 서비스 통신 기록 영속화")
class UserServiceCommunicationHistoryPersistenceAdapterTest {

    @InjectMocks
    private UserServiceCommunicationHistoryPersistenceAdapter adapter;

    @Mock
    private UserServiceCommunicationHistoryJpaRepository repository;

    @Test
    @DisplayName("도메인 객체를 엔티티로 변환하여 저장하고 저장 결과를 도메인으로 돌려준다")
    void savesHistoryAndReturnsDomain() {
        // given
        UserServiceCommunicationHistory history = UserServiceCommunicationHistory.from(
                UserServiceCommunicationHistoryCreateState.builder()
                        .targetType(UserServiceCommunicationTargetType.USER_POINT)
                        .targetId("42")
                        .communicationType(UserServiceCommunicationType.REQUEST)
                        .sender(UserServiceCommunicationSenderType.REWARD)
                        .exception("timeout")
                        .payload("{\"userId\":42}")
                        .payloadJson(null)
                        .build()
        );
        UserServiceCommunicationHistoryJpaEntity savedEntity = UserServiceCommunicationHistoryJpaEntity.from(
                UserServiceCommunicationHistory.from(
                        UserServiceCommunicationHistorySnapshotState.builder()
                                .id(99L)
                                .targetType(UserServiceCommunicationTargetType.USER_POINT)
                                .targetId("42")
                                .communicationType(UserServiceCommunicationType.REQUEST)
                                .sender(UserServiceCommunicationSenderType.REWARD)
                                .exception("timeout")
                                .payload("{\"userId\":42}")
                                .payloadJson(null)
                                .createdAt(LocalDateTime.of(2025, 3, 1, 10, 0))
                                .build()
                )
        );
        when(repository.save(any(UserServiceCommunicationHistoryJpaEntity.class))).thenReturn(savedEntity);

        // when
        UserServiceCommunicationHistory result = adapter.save(history);

        // then
        ArgumentCaptor<UserServiceCommunicationHistoryJpaEntity> captor =
                ArgumentCaptor.forClass(UserServiceCommunicationHistoryJpaEntity.class);
        verify(repository).save(captor.capture());
        UserServiceCommunicationHistoryJpaEntity captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(UserServiceCommunicationTargetType.USER_POINT);
        assertThat(captured.getTargetId()).isEqualTo("42");
        assertThat(captured.getCommunicationType()).isEqualTo(UserServiceCommunicationType.REQUEST);
        assertThat(captured.getSender()).isEqualTo(UserServiceCommunicationSenderType.REWARD);
        assertThat(captured.getException()).isEqualTo("timeout");
        assertThat(captured.getPayload()).isEqualTo("{\"userId\":42}");

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 3, 1, 10, 0));
        assertThat(result.getTargetType()).isEqualTo(UserServiceCommunicationTargetType.USER_POINT);
        verifyNoMoreInteractions(repository);
    }
}
