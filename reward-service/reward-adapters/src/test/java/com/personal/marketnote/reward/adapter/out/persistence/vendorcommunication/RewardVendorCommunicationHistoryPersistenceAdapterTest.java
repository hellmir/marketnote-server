package com.personal.marketnote.reward.adapter.out.persistence.vendorcommunication;

import com.personal.marketnote.reward.adapter.out.persistence.vendorcommunication.entity.RewardVendorCommunicationHistoryJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.vendorcommunication.repository.RewardVendorCommunicationHistoryJpaRepository;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationHistory;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationHistoryCreateState;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationSenderType;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationTargetType;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationType;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorName;
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
@DisplayName("RewardVendorCommunicationHistoryPersistenceAdapter 테스트")
class RewardVendorCommunicationHistoryPersistenceAdapterTest {

    @InjectMocks
    private RewardVendorCommunicationHistoryPersistenceAdapter adapter;

    @Mock
    private RewardVendorCommunicationHistoryJpaRepository repository;

    @Test
    @DisplayName("정상 저장 시 저장된 엔티티를 도메인 객체로 변환하여 반환한다")
    void savesAndReturnsDomain() {
        // given
        RewardVendorCommunicationHistory history = buildHistory();
        LocalDateTime savedAt = LocalDateTime.of(2026, 4, 15, 12, 30);
        given(repository.save(any(RewardVendorCommunicationHistoryJpaEntity.class)))
                .willAnswer(invocation -> {
                    RewardVendorCommunicationHistoryJpaEntity arg = invocation.getArgument(0);
                    ReflectionTestUtils.setField(arg, "id", 777L);
                    ReflectionTestUtils.setField(arg, "createdAt", savedAt);
                    return arg;
                });

        // when
        RewardVendorCommunicationHistory saved = adapter.save(history);

        // then
        assertThat(saved.getId()).isEqualTo(777L);
        assertThat(saved.getTargetType()).isEqualTo(RewardVendorCommunicationTargetType.OFFERWALL);
        assertThat(saved.getTargetId()).isEqualTo("txn-99");
        assertThat(saved.getVendorName()).isEqualTo(RewardVendorName.ADPOPCORN);
        assertThat(saved.getCommunicationType()).isEqualTo(RewardVendorCommunicationType.REQUEST);
        assertThat(saved.getSender()).isEqualTo(RewardVendorCommunicationSenderType.SERVER);
        assertThat(saved.getException()).isEqualTo("HttpTimeoutException");
        assertThat(saved.getPayload()).isEqualTo("{\"k\":\"v\"}");
        assertThat(saved.getCreatedAt()).isEqualTo(savedAt);
    }

    @Test
    @DisplayName("도메인 객체를 JPA 엔티티로 변환한 뒤 리포지토리에 위임한다")
    void delegatesSaveWithMappedEntity() {
        // given
        RewardVendorCommunicationHistory history = buildHistory();
        given(repository.save(any(RewardVendorCommunicationHistoryJpaEntity.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        adapter.save(history);

        // then
        ArgumentCaptor<RewardVendorCommunicationHistoryJpaEntity> captor =
                ArgumentCaptor.forClass(RewardVendorCommunicationHistoryJpaEntity.class);
        verify(repository).save(captor.capture());
        verifyNoMoreInteractions(repository);
        RewardVendorCommunicationHistoryJpaEntity captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(RewardVendorCommunicationTargetType.OFFERWALL);
        assertThat(captured.getTargetId()).isEqualTo("txn-99");
        assertThat(captured.getVendorName()).isEqualTo(RewardVendorName.ADPOPCORN);
        assertThat(captured.getCommunicationType()).isEqualTo(RewardVendorCommunicationType.REQUEST);
        assertThat(captured.getSender()).isEqualTo(RewardVendorCommunicationSenderType.SERVER);
        assertThat(captured.getException()).isEqualTo("HttpTimeoutException");
        assertThat(captured.getPayload()).isEqualTo("{\"k\":\"v\"}");
    }

    private RewardVendorCommunicationHistory buildHistory() {
        return RewardVendorCommunicationHistory.from(RewardVendorCommunicationHistoryCreateState.builder()
                .targetType(RewardVendorCommunicationTargetType.OFFERWALL)
                .targetId("txn-99")
                .vendorName(RewardVendorName.ADPOPCORN)
                .communicationType(RewardVendorCommunicationType.REQUEST)
                .sender(RewardVendorCommunicationSenderType.SERVER)
                .exception("HttpTimeoutException")
                .payload("{\"k\":\"v\"}")
                .payloadJson(null)
                .build());
    }
}
