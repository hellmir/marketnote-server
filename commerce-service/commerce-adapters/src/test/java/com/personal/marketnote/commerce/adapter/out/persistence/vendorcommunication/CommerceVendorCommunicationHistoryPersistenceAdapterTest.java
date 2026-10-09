package com.personal.marketnote.commerce.adapter.out.persistence.vendorcommunication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.out.persistence.vendorcommunication.entity.CommerceVendorCommunicationHistoryJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.vendorcommunication.repository.CommerceVendorCommunicationHistoryJpaRepository;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationHistory;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationHistoryCreateState;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationSenderType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationTargetType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommerceVendorCommunicationHistoryPersistenceAdapter 테스트")
class CommerceVendorCommunicationHistoryPersistenceAdapterTest {

    @InjectMocks
    private CommerceVendorCommunicationHistoryPersistenceAdapter adapter;

    @Mock
    private CommerceVendorCommunicationHistoryJpaRepository repository;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adapter, "objectMapper", new ObjectMapper());
    }

    private CommerceVendorCommunicationHistory buildHistory(String payloadJson) {
        return CommerceVendorCommunicationHistory.from(
                CommerceVendorCommunicationHistoryCreateState.builder()
                        .targetType(CommerceVendorCommunicationTargetType.PAYMENT_APPROVAL)
                        .targetId("order-1")
                        .vendorName(CommerceVendorName.NHN_KCP)
                        .communicationType(CommerceVendorCommunicationType.REQUEST)
                        .sender(CommerceVendorCommunicationSenderType.SERVER)
                        .exception(null)
                        .payload("{\"amount\":10000}")
                        .payloadJson(payloadJson)
                        .build()
        );
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("벤더 통신 기록을 저장하면 JSON을 파싱하여 엔티티를 저장하고 도메인을 반환한다")
        void shouldSaveWithParsedPayloadJson() {
            // given
            CommerceVendorCommunicationHistory history = buildHistory("{\"amount\":10000}");
            when(repository.save(any(CommerceVendorCommunicationHistoryJpaEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            CommerceVendorCommunicationHistory saved = adapter.save(history);

            // then
            assertThat(saved).isNotNull();
            assertThat(saved.getVendorName()).isEqualTo(CommerceVendorName.NHN_KCP);
            assertThat(saved.getTargetType()).isEqualTo(CommerceVendorCommunicationTargetType.PAYMENT_APPROVAL);
            ArgumentCaptor<CommerceVendorCommunicationHistoryJpaEntity> captor =
                    ArgumentCaptor.forClass(CommerceVendorCommunicationHistoryJpaEntity.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getPayloadJson()).isNotNull();
            assertThat(captor.getValue().getPayloadJson().get("amount").asInt()).isEqualTo(10000);
        }

        @Test
        @DisplayName("payloadJson이 null이면 엔티티의 payloadJson도 null로 저장된다")
        void shouldSaveWithNullPayloadJsonWhenInputNull() {
            // given
            CommerceVendorCommunicationHistory history = buildHistory(null);
            when(repository.save(any(CommerceVendorCommunicationHistoryJpaEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            adapter.save(history);

            // then
            ArgumentCaptor<CommerceVendorCommunicationHistoryJpaEntity> captor =
                    ArgumentCaptor.forClass(CommerceVendorCommunicationHistoryJpaEntity.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getPayloadJson()).isNull();
        }

        @Test
        @DisplayName("payloadJson이 유효하지 않은 JSON이면 파싱 실패로 null이 저장된다")
        void shouldSaveWithNullPayloadJsonWhenInvalidJson() {
            // given
            CommerceVendorCommunicationHistory history = buildHistory("invalid-{");
            when(repository.save(any(CommerceVendorCommunicationHistoryJpaEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            adapter.save(history);

            // then
            ArgumentCaptor<CommerceVendorCommunicationHistoryJpaEntity> captor =
                    ArgumentCaptor.forClass(CommerceVendorCommunicationHistoryJpaEntity.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getPayloadJson()).isNull();
        }
    }
}
