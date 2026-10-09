package com.personal.marketnote.commerce.adapter.out.persistence.servicecommunication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.out.persistence.servicecommunication.entity.CommerceServiceCommunicationHistoryJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.servicecommunication.repository.CommerceServiceCommunicationHistoryJpaRepository;
import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationHistory;
import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationHistoryCreateState;
import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationSenderType;
import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationTargetType;
import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommerceServiceCommunicationHistoryPersistenceAdapter 테스트")
class CommerceServiceCommunicationHistoryPersistenceAdapterTest {

    @InjectMocks
    private CommerceServiceCommunicationHistoryPersistenceAdapter adapter;

    @Mock
    private CommerceServiceCommunicationHistoryJpaRepository repository;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        org.springframework.test.util.ReflectionTestUtils.setField(adapter, "objectMapper", objectMapper);
    }

    private CommerceServiceCommunicationHistory buildHistory(String payloadJson) {
        return CommerceServiceCommunicationHistory.from(
                CommerceServiceCommunicationHistoryCreateState.builder()
                        .targetType(CommerceServiceCommunicationTargetType.PRODUCT_INFO)
                        .targetId("123")
                        .communicationType(CommerceServiceCommunicationType.REQUEST)
                        .sender(CommerceServiceCommunicationSenderType.COMMERCE)
                        .exception(null)
                        .payload("{\"key\":\"value\"}")
                        .payloadJson(payloadJson)
                        .build()
        );
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("서비스 통신 기록을 저장하면 JSON을 파싱하여 엔티티를 저장하고 도메인을 반환한다")
        void shouldSaveWithParsedPayloadJson() {
            // given
            CommerceServiceCommunicationHistory history = buildHistory("{\"key\":\"value\"}");
            when(repository.save(any(CommerceServiceCommunicationHistoryJpaEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            CommerceServiceCommunicationHistory saved = adapter.save(history);

            // then
            assertThat(saved).isNotNull();
            assertThat(saved.getTargetType()).isEqualTo(CommerceServiceCommunicationTargetType.PRODUCT_INFO);
            assertThat(saved.getTargetId()).isEqualTo("123");
            ArgumentCaptor<CommerceServiceCommunicationHistoryJpaEntity> captor =
                    ArgumentCaptor.forClass(CommerceServiceCommunicationHistoryJpaEntity.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getPayloadJson()).isNotNull();
            assertThat(captor.getValue().getPayloadJson().get("key").asText()).isEqualTo("value");
        }

        @Test
        @DisplayName("payloadJson이 null이면 엔티티의 payloadJson도 null로 저장된다")
        void shouldSaveWithNullPayloadJsonWhenInputNull() {
            // given
            CommerceServiceCommunicationHistory history = buildHistory(null);
            when(repository.save(any(CommerceServiceCommunicationHistoryJpaEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            adapter.save(history);

            // then
            ArgumentCaptor<CommerceServiceCommunicationHistoryJpaEntity> captor =
                    ArgumentCaptor.forClass(CommerceServiceCommunicationHistoryJpaEntity.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getPayloadJson()).isNull();
        }

        @Test
        @DisplayName("payloadJson이 유효하지 않은 JSON이면 파싱 실패로 null이 저장된다")
        void shouldSaveWithNullPayloadJsonWhenInvalidJson() {
            // given
            CommerceServiceCommunicationHistory history = buildHistory("invalid-json-{");
            when(repository.save(any(CommerceServiceCommunicationHistoryJpaEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            adapter.save(history);

            // then
            ArgumentCaptor<CommerceServiceCommunicationHistoryJpaEntity> captor =
                    ArgumentCaptor.forClass(CommerceServiceCommunicationHistoryJpaEntity.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getPayloadJson()).isNull();
        }
    }
}
