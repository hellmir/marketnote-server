package com.personal.marketnote.fulfillment.adapter.out.persistence.servicecommunication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.personal.marketnote.fulfillment.adapter.out.persistence.servicecommunication.entity.FulfillmentServiceCommunicationHistoryJpaEntity;
import com.personal.marketnote.fulfillment.adapter.out.persistence.servicecommunication.repository.FulfillmentServiceCommunicationHistoryJpaRepository;
import com.personal.marketnote.fulfillment.domain.servicecommunication.FulfillmentServiceCommunicationHistory;
import com.personal.marketnote.fulfillment.domain.servicecommunication.FulfillmentServiceCommunicationHistoryCreateState;
import com.personal.marketnote.fulfillment.domain.servicecommunication.FulfillmentServiceCommunicationHistorySnapshotState;
import com.personal.marketnote.fulfillment.domain.servicecommunication.FulfillmentServiceCommunicationSenderType;
import com.personal.marketnote.fulfillment.domain.servicecommunication.FulfillmentServiceCommunicationTargetType;
import com.personal.marketnote.fulfillment.domain.servicecommunication.FulfillmentServiceCommunicationType;
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
@DisplayName("FulfillmentServiceCommunicationHistoryPersistenceAdapter 단위 테스트")
class FulfillmentServiceCommunicationHistoryPersistenceAdapterTest {

    @InjectMocks
    private FulfillmentServiceCommunicationHistoryPersistenceAdapter adapter;

    @Mock
    private FulfillmentServiceCommunicationHistoryJpaRepository repository;

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
            FulfillmentServiceCommunicationHistory history = FulfillmentServiceCommunicationHistory.from(
                    FulfillmentServiceCommunicationHistoryCreateState.builder()
                            .targetType(FulfillmentServiceCommunicationTargetType.COMMERCE_INVENTORY)
                            .targetId("TARGET-1")
                            .communicationType(FulfillmentServiceCommunicationType.REQUEST)
                            .sender(FulfillmentServiceCommunicationSenderType.COMMERCE)
                            .exception("ConnectionReset")
                            .payload("raw-payload")
                            .payloadJson(payloadJson)
                            .build()
            );

            FulfillmentServiceCommunicationHistoryJpaEntity persistedEntity = buildEntity(
                    100L,
                    FulfillmentServiceCommunicationTargetType.COMMERCE_INVENTORY,
                    "TARGET-1",
                    FulfillmentServiceCommunicationType.REQUEST,
                    FulfillmentServiceCommunicationSenderType.COMMERCE,
                    "ConnectionReset",
                    "raw-payload",
                    payloadJson,
                    LocalDateTime.of(2026, 4, 15, 10, 0)
            );
            when(repository.save(any(FulfillmentServiceCommunicationHistoryJpaEntity.class)))
                    .thenReturn(persistedEntity);

            // when
            FulfillmentServiceCommunicationHistory result = adapter.save(history);

            // then
            ArgumentCaptor<FulfillmentServiceCommunicationHistoryJpaEntity> captor =
                    ArgumentCaptor.forClass(FulfillmentServiceCommunicationHistoryJpaEntity.class);
            verify(repository).save(captor.capture());
            FulfillmentServiceCommunicationHistoryJpaEntity captured = captor.getValue();
            assertThat(captured.getTargetType()).isEqualTo(FulfillmentServiceCommunicationTargetType.COMMERCE_INVENTORY);
            assertThat(captured.getTargetId()).isEqualTo("TARGET-1");
            assertThat(captured.getCommunicationType()).isEqualTo(FulfillmentServiceCommunicationType.REQUEST);
            assertThat(captured.getSender()).isEqualTo(FulfillmentServiceCommunicationSenderType.COMMERCE);
            assertThat(captured.getException()).isEqualTo("ConnectionReset");
            assertThat(captured.getPayload()).isEqualTo("raw-payload");
            assertThat(captured.getPayloadJson()).isEqualTo(payloadJson);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(100L);
            assertThat(result.getTargetType()).isEqualTo(FulfillmentServiceCommunicationTargetType.COMMERCE_INVENTORY);
            assertThat(result.getTargetId()).isEqualTo("TARGET-1");
            assertThat(result.getCommunicationType()).isEqualTo(FulfillmentServiceCommunicationType.REQUEST);
            assertThat(result.getSender()).isEqualTo(FulfillmentServiceCommunicationSenderType.COMMERCE);
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
            FulfillmentServiceCommunicationHistory history = FulfillmentServiceCommunicationHistory.from(
                    FulfillmentServiceCommunicationHistoryCreateState.builder()
                            .targetType(FulfillmentServiceCommunicationTargetType.GENERAL)
                            .targetId(null)
                            .communicationType(FulfillmentServiceCommunicationType.RESPONSE)
                            .sender(FulfillmentServiceCommunicationSenderType.FULFILLMENT)
                            .exception(null)
                            .payload(null)
                            .payloadJson(null)
                            .build()
            );

            FulfillmentServiceCommunicationHistoryJpaEntity persistedEntity = buildEntity(
                    200L,
                    FulfillmentServiceCommunicationTargetType.GENERAL,
                    null,
                    FulfillmentServiceCommunicationType.RESPONSE,
                    FulfillmentServiceCommunicationSenderType.FULFILLMENT,
                    null,
                    null,
                    null,
                    LocalDateTime.of(2026, 4, 15, 11, 0)
            );
            when(repository.save(any(FulfillmentServiceCommunicationHistoryJpaEntity.class)))
                    .thenReturn(persistedEntity);

            // when
            FulfillmentServiceCommunicationHistory result = adapter.save(history);

            // then
            assertThat(result.getId()).isEqualTo(200L);
            assertThat(result.getTargetType()).isEqualTo(FulfillmentServiceCommunicationTargetType.GENERAL);
            assertThat(result.getTargetId()).isNull();
            assertThat(result.getException()).isNull();
            assertThat(result.getPayload()).isNull();
            assertThat(result.getPayloadJson()).isNull();
            verify(repository).save(any(FulfillmentServiceCommunicationHistoryJpaEntity.class));
            verifyNoMoreInteractions(repository);
        }
    }

    private FulfillmentServiceCommunicationHistoryJpaEntity buildEntity(
            Long id,
            FulfillmentServiceCommunicationTargetType targetType,
            String targetId,
            FulfillmentServiceCommunicationType communicationType,
            FulfillmentServiceCommunicationSenderType sender,
            String exception,
            String payload,
            JsonNode payloadJson,
            LocalDateTime createdAt
    ) {
        FulfillmentServiceCommunicationHistoryJpaEntity entity = FulfillmentServiceCommunicationHistoryJpaEntity.from(
                FulfillmentServiceCommunicationHistory.from(
                        FulfillmentServiceCommunicationHistorySnapshotState.builder()
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
