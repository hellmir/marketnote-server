package com.personal.marketnote.fulfillment.adapter.out.persistence.vendorcommunication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.personal.marketnote.fulfillment.adapter.out.persistence.vendorcommunication.entity.FulfillmentVendorCommunicationHistoryJpaEntity;
import com.personal.marketnote.fulfillment.adapter.out.persistence.vendorcommunication.repository.FulfillmentVendorCommunicationHistoryJpaRepository;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationHistory;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationHistoryCreateState;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationHistorySnapshotState;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationSenderType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationTargetType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorName;
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
@DisplayName("FulfillmentVendorCommunicationHistoryPersistenceAdapter 단위 테스트")
class FulfillmentVendorCommunicationHistoryPersistenceAdapterTest {

    @InjectMocks
    private FulfillmentVendorCommunicationHistoryPersistenceAdapter adapter;

    @Mock
    private FulfillmentVendorCommunicationHistoryJpaRepository repository;

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("도메인을 엔티티로 변환하여 저장하고 저장된 엔티티를 도메인으로 복원하여 반환한다")
        void savesDomainAsEntityAndReturnsReconstructedDomain() {
            // given
            ObjectNode payloadNode = JsonNodeFactory.instance.objectNode();
            payloadNode.put("orderId", "ORD-1");
            JsonNode payloadJson = payloadNode;

            FulfillmentVendorCommunicationHistory history = FulfillmentVendorCommunicationHistory.from(
                    FulfillmentVendorCommunicationHistoryCreateState.builder()
                            .targetType(FulfillmentVendorCommunicationTargetType.DELIVERY)
                            .targetId("SLIP-001")
                            .vendorName(FulfillmentVendorName.FASSTO)
                            .communicationType(FulfillmentVendorCommunicationType.REQUEST)
                            .sender(FulfillmentVendorCommunicationSenderType.SERVER)
                            .exception("Timeout")
                            .payload("raw-payload")
                            .payloadJson(payloadJson)
                            .build()
            );

            FulfillmentVendorCommunicationHistoryJpaEntity persistedEntity = buildEntity(
                    10L,
                    FulfillmentVendorCommunicationTargetType.DELIVERY,
                    "SLIP-001",
                    FulfillmentVendorName.FASSTO,
                    FulfillmentVendorCommunicationType.REQUEST,
                    FulfillmentVendorCommunicationSenderType.SERVER,
                    "Timeout",
                    "raw-payload",
                    payloadJson,
                    LocalDateTime.of(2026, 4, 15, 12, 0)
            );
            when(repository.save(any(FulfillmentVendorCommunicationHistoryJpaEntity.class)))
                    .thenReturn(persistedEntity);

            // when
            FulfillmentVendorCommunicationHistory result = adapter.save(history);

            // then
            ArgumentCaptor<FulfillmentVendorCommunicationHistoryJpaEntity> captor =
                    ArgumentCaptor.forClass(FulfillmentVendorCommunicationHistoryJpaEntity.class);
            verify(repository).save(captor.capture());
            FulfillmentVendorCommunicationHistoryJpaEntity captured = captor.getValue();
            assertThat(captured.getTargetType()).isEqualTo(FulfillmentVendorCommunicationTargetType.DELIVERY);
            assertThat(captured.getTargetId()).isEqualTo("SLIP-001");
            assertThat(captured.getVendorName()).isEqualTo(FulfillmentVendorName.FASSTO);
            assertThat(captured.getCommunicationType()).isEqualTo(FulfillmentVendorCommunicationType.REQUEST);
            assertThat(captured.getSender()).isEqualTo(FulfillmentVendorCommunicationSenderType.SERVER);
            assertThat(captured.getException()).isEqualTo("Timeout");
            assertThat(captured.getPayload()).isEqualTo("raw-payload");
            assertThat(captured.getPayloadJson()).isEqualTo(payloadJson);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getTargetType()).isEqualTo(FulfillmentVendorCommunicationTargetType.DELIVERY);
            assertThat(result.getTargetId()).isEqualTo("SLIP-001");
            assertThat(result.getVendorName()).isEqualTo(FulfillmentVendorName.FASSTO);
            assertThat(result.getCommunicationType()).isEqualTo(FulfillmentVendorCommunicationType.REQUEST);
            assertThat(result.getSender()).isEqualTo(FulfillmentVendorCommunicationSenderType.SERVER);
            assertThat(result.getException()).isEqualTo("Timeout");
            assertThat(result.getPayload()).isEqualTo("raw-payload");
            assertThat(result.getPayloadJson()).isEqualTo(payloadJson);
            assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 4, 15, 12, 0));
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("응답 수신 이력에서 예외/페이로드가 null이어도 정상 저장된다")
        void savesResponseHistoryWithoutOptionalFields() {
            // given
            FulfillmentVendorCommunicationHistory history = FulfillmentVendorCommunicationHistory.from(
                    FulfillmentVendorCommunicationHistoryCreateState.builder()
                            .targetType(FulfillmentVendorCommunicationTargetType.AUTHENTICATION)
                            .targetId(null)
                            .vendorName(FulfillmentVendorName.FASSTO)
                            .communicationType(FulfillmentVendorCommunicationType.RESPONSE)
                            .sender(FulfillmentVendorCommunicationSenderType.VENDOR)
                            .exception(null)
                            .payload(null)
                            .payloadJson(null)
                            .build()
            );

            FulfillmentVendorCommunicationHistoryJpaEntity persistedEntity = buildEntity(
                    20L,
                    FulfillmentVendorCommunicationTargetType.AUTHENTICATION,
                    null,
                    FulfillmentVendorName.FASSTO,
                    FulfillmentVendorCommunicationType.RESPONSE,
                    FulfillmentVendorCommunicationSenderType.VENDOR,
                    null,
                    null,
                    null,
                    LocalDateTime.of(2026, 4, 15, 13, 0)
            );
            when(repository.save(any(FulfillmentVendorCommunicationHistoryJpaEntity.class)))
                    .thenReturn(persistedEntity);

            // when
            FulfillmentVendorCommunicationHistory result = adapter.save(history);

            // then
            assertThat(result.getId()).isEqualTo(20L);
            assertThat(result.getTargetType()).isEqualTo(FulfillmentVendorCommunicationTargetType.AUTHENTICATION);
            assertThat(result.getTargetId()).isNull();
            assertThat(result.getException()).isNull();
            assertThat(result.getPayload()).isNull();
            assertThat(result.getPayloadJson()).isNull();
            assertThat(result.getSender()).isEqualTo(FulfillmentVendorCommunicationSenderType.VENDOR);
            verify(repository).save(any(FulfillmentVendorCommunicationHistoryJpaEntity.class));
            verifyNoMoreInteractions(repository);
        }
    }

    private FulfillmentVendorCommunicationHistoryJpaEntity buildEntity(
            Long id,
            FulfillmentVendorCommunicationTargetType targetType,
            String targetId,
            FulfillmentVendorName vendorName,
            FulfillmentVendorCommunicationType communicationType,
            FulfillmentVendorCommunicationSenderType sender,
            String exception,
            String payload,
            JsonNode payloadJson,
            LocalDateTime createdAt
    ) {
        FulfillmentVendorCommunicationHistoryJpaEntity entity = FulfillmentVendorCommunicationHistoryJpaEntity.from(
                FulfillmentVendorCommunicationHistory.from(
                        FulfillmentVendorCommunicationHistorySnapshotState.builder()
                                .id(id)
                                .targetType(targetType)
                                .targetId(targetId)
                                .vendorName(vendorName)
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
