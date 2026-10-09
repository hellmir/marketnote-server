package com.personal.marketnote.file.adapter.out.persistence.servicecommunication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.personal.marketnote.file.adapter.out.persistence.servicecommunication.entity.FileServiceCommunicationHistoryJpaEntity;
import com.personal.marketnote.file.adapter.out.persistence.servicecommunication.repository.FileServiceCommunicationHistoryJpaRepository;
import com.personal.marketnote.file.domain.servicecommunication.FileServiceCommunicationHistory;
import com.personal.marketnote.file.domain.servicecommunication.FileServiceCommunicationHistoryCreateState;
import com.personal.marketnote.file.domain.servicecommunication.FileServiceCommunicationHistorySnapshotState;
import com.personal.marketnote.file.domain.servicecommunication.FileServiceCommunicationSenderType;
import com.personal.marketnote.file.domain.servicecommunication.FileServiceCommunicationTargetType;
import com.personal.marketnote.file.domain.servicecommunication.FileServiceCommunicationType;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileServiceCommunicationHistoryPersistenceAdapter 단위 테스트")
class FileServiceCommunicationHistoryPersistenceAdapterTest {

    @InjectMocks
    private FileServiceCommunicationHistoryPersistenceAdapter adapter;

    @Mock
    private FileServiceCommunicationHistoryJpaRepository repository;

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("도메인을 엔티티로 변환하여 저장하고 저장된 엔티티를 도메인으로 복원하여 반환한다")
        void savesDomainAsEntityAndReturnsReconstructedDomain() {
            // given
            ObjectNode payloadNode = JsonNodeFactory.instance.objectNode();
            payloadNode.put("key", "value");
            JsonNode payloadJson = payloadNode;
            FileServiceCommunicationHistory history = FileServiceCommunicationHistory.from(
                    FileServiceCommunicationHistoryCreateState.builder()
                            .targetType(FileServiceCommunicationTargetType.GENERAL)
                            .targetId("TARGET-1")
                            .communicationType(FileServiceCommunicationType.REQUEST)
                            .sender(FileServiceCommunicationSenderType.COMMERCE)
                            .exception("ConnectionReset")
                            .payload("raw-payload")
                            .payloadJson(payloadJson)
                            .build()
            );

            FileServiceCommunicationHistoryJpaEntity persistedEntity = buildEntity(
                    100L,
                    FileServiceCommunicationTargetType.GENERAL,
                    "TARGET-1",
                    FileServiceCommunicationType.REQUEST,
                    FileServiceCommunicationSenderType.COMMERCE,
                    "ConnectionReset",
                    "raw-payload",
                    payloadJson,
                    LocalDateTime.of(2026, 4, 15, 10, 0)
            );
            when(repository.save(any(FileServiceCommunicationHistoryJpaEntity.class)))
                    .thenReturn(persistedEntity);

            // when
            FileServiceCommunicationHistory result = adapter.save(history);

            // then
            ArgumentCaptor<FileServiceCommunicationHistoryJpaEntity> captor =
                    ArgumentCaptor.forClass(FileServiceCommunicationHistoryJpaEntity.class);
            verify(repository).save(captor.capture());
            FileServiceCommunicationHistoryJpaEntity captured = captor.getValue();
            assertThat(captured.getTargetType()).isEqualTo(FileServiceCommunicationTargetType.GENERAL);
            assertThat(captured.getTargetId()).isEqualTo("TARGET-1");
            assertThat(captured.getCommunicationType()).isEqualTo(FileServiceCommunicationType.REQUEST);
            assertThat(captured.getSender()).isEqualTo(FileServiceCommunicationSenderType.COMMERCE);
            assertThat(captured.getException()).isEqualTo("ConnectionReset");
            assertThat(captured.getPayload()).isEqualTo("raw-payload");
            assertThat(captured.getPayloadJson()).isEqualTo(payloadJson);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(100L);
            assertThat(result.getTargetType()).isEqualTo(FileServiceCommunicationTargetType.GENERAL);
            assertThat(result.getTargetId()).isEqualTo("TARGET-1");
            assertThat(result.getCommunicationType()).isEqualTo(FileServiceCommunicationType.REQUEST);
            assertThat(result.getSender()).isEqualTo(FileServiceCommunicationSenderType.COMMERCE);
            assertThat(result.getException()).isEqualTo("ConnectionReset");
            assertThat(result.getPayload()).isEqualTo("raw-payload");
            assertThat(result.getPayloadJson()).isEqualTo(payloadJson);
            assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 4, 15, 10, 0));
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("예외/페이로드가 null이어도 저장 요청이 정상 수행된다")
        void savesEvenWhenOptionalFieldsAreNull() {
            // given
            FileServiceCommunicationHistory history = FileServiceCommunicationHistory.from(
                    FileServiceCommunicationHistoryCreateState.builder()
                            .targetType(FileServiceCommunicationTargetType.GENERAL)
                            .targetId(null)
                            .communicationType(FileServiceCommunicationType.RESPONSE)
                            .sender(FileServiceCommunicationSenderType.FILE)
                            .exception(null)
                            .payload(null)
                            .payloadJson(null)
                            .build()
            );

            FileServiceCommunicationHistoryJpaEntity persistedEntity = buildEntity(
                    200L,
                    FileServiceCommunicationTargetType.GENERAL,
                    null,
                    FileServiceCommunicationType.RESPONSE,
                    FileServiceCommunicationSenderType.FILE,
                    null,
                    null,
                    null,
                    LocalDateTime.of(2026, 4, 15, 11, 0)
            );
            when(repository.save(any(FileServiceCommunicationHistoryJpaEntity.class)))
                    .thenReturn(persistedEntity);

            // when
            FileServiceCommunicationHistory result = adapter.save(history);

            // then
            assertThat(result.getId()).isEqualTo(200L);
            assertThat(result.getTargetType()).isEqualTo(FileServiceCommunicationTargetType.GENERAL);
            assertThat(result.getTargetId()).isNull();
            assertThat(result.getException()).isNull();
            assertThat(result.getPayload()).isNull();
            assertThat(result.getPayloadJson()).isNull();
            verify(repository).save(any(FileServiceCommunicationHistoryJpaEntity.class));
            verifyNoMoreInteractions(repository);
        }
    }

    private FileServiceCommunicationHistoryJpaEntity buildEntity(
            Long id,
            FileServiceCommunicationTargetType targetType,
            String targetId,
            FileServiceCommunicationType communicationType,
            FileServiceCommunicationSenderType sender,
            String exception,
            String payload,
            JsonNode payloadJson,
            LocalDateTime createdAt
    ) {
        FileServiceCommunicationHistoryJpaEntity entity = FileServiceCommunicationHistoryJpaEntity.from(
                FileServiceCommunicationHistory.from(
                        FileServiceCommunicationHistorySnapshotState.builder()
                                .id(id)
                                .targetType(targetType)
                                .targetId(targetId)
                                .communicationType(communicationType)
                                .sender(sender)
                                .exception(exception)
                                .payload(payload)
                                .payloadJson(payloadJson)
                                .createdAt(createdAt)
                                .build()
                )
        );
        ReflectionTestUtils.setField(entity, "id", id);
        ReflectionTestUtils.setField(entity, "createdAt", createdAt);
        return entity;
    }
}
