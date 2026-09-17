package com.personal.marketnote.commerce.domain.quickpayment;

import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class QuickPaymentCardTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 4, 11, 10, 0);

    @Test
    @DisplayName("CreateState로 빠른결제 카드를 생성하면 ACTIVE 상태이다")
    void shouldCreateQuickPaymentCardWithActiveStatus() {
        // given
        QuickPaymentCardCreateState state = QuickPaymentCardCreateState.builder()
                .userId(1L)
                .batchKey("batch-key-001")
                .groupId("group-001")
                .cardCode("CARD01")
                .cardName("테스트카드")
                .maskedCardNumber("1234-****-****-5678")
                .cardBinType01("CREDIT")
                .cardBinType02("PERSONAL")
                .build();

        // when
        QuickPaymentCard card = QuickPaymentCard.from(state);

        // then
        assertThat(card.getUserId()).isEqualTo(1L);
        assertThat(card.getBatchKey()).isEqualTo("batch-key-001");
        assertThat(card.getCardName()).isEqualTo("테스트카드");
        assertThat(card.getMaskedCardNumber()).isEqualTo("1234-****-****-5678");
        assertThat(card.isActive()).isTrue();
    }

    @Test
    @DisplayName("SnapshotState로 빠른결제 카드를 복원한다")
    void shouldRestoreQuickPaymentCardFromSnapshotState() {
        // given
        QuickPaymentCardSnapshotState state = QuickPaymentCardSnapshotState.builder()
                .id(10L)
                .userId(1L)
                .batchKey("batch-key-001")
                .groupId("group-001")
                .cardCode("CARD01")
                .cardName("테스트카드")
                .maskedCardNumber("1234-****-****-5678")
                .cardBinType01("CREDIT")
                .cardBinType02("PERSONAL")
                .status(EntityStatus.INACTIVE)
                .createdAt(NOW)
                .modifiedAt(NOW)
                .build();

        // when
        QuickPaymentCard card = QuickPaymentCard.from(state);

        // then
        assertThat(card.getId()).isEqualTo(10L);
        assertThat(card.getUserId()).isEqualTo(1L);
        assertThat(card.isInactive()).isTrue();
        assertThat(card.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("deactivate 호출 시 상태가 INACTIVE로 변경된다")
    void shouldChangeStatusToInactiveWhenDeactivated() {
        // given
        QuickPaymentCard card = QuickPaymentCard.from(QuickPaymentCardCreateState.builder()
                .userId(1L)
                .batchKey("batch-key-001")
                .groupId("group-001")
                .cardCode("CARD01")
                .cardName("테스트카드")
                .maskedCardNumber("1234-****-****-5678")
                .cardBinType01("CREDIT")
                .cardBinType02("PERSONAL")
                .build());
        assertThat(card.isActive()).isTrue();

        // when
        card.deactivate();

        // then
        assertThat(card.isInactive()).isTrue();
        assertThat(card.isActive()).isFalse();
    }
}
