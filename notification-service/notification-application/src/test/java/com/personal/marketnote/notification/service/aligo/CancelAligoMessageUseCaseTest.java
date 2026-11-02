package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.domain.aligo.*;
import com.personal.marketnote.notification.port.out.aligo.CancelAligoMessageExternalPort;
import com.personal.marketnote.notification.port.out.aligo.FindAligoMessagePort;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoMessagePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelAligoMessageUseCaseTest {

    @InjectMocks
    private CancelAligoMessageService cancelAligoMessageService;

    @Mock
    private FindAligoMessagePort findAligoMessagePort;

    @Mock
    private CancelAligoMessageExternalPort cancelAligoMessageExternalPort;

    @Mock
    private UpdateAligoMessagePort updateAligoMessagePort;

    @Mock
    private Clock clock;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 4, 16, 12, 0, 0);
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private void setupClock() {
        Instant instant = NOW.atZone(ZONE).toInstant();
        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(ZONE);
    }

    @Test
    @DisplayName("예약 발송 취소 성공: REQUESTED 상태 + 발송 예정시각 5분 이상 전")
    void shouldCancelScheduledMessageSuccessfully() {
        // given
        setupClock();
        Long messageId = 100L;
        AligoMessage message = createRequestedMessage(messageId, "MID_001", NOW.plusHours(1));
        when(findAligoMessagePort.findById(messageId)).thenReturn(Optional.of(message));

        // when
        cancelAligoMessageService.cancelMessage(messageId);

        // then
        verify(cancelAligoMessageExternalPort).cancel("MID_001");

        ArgumentCaptor<AligoMessage> captor = ArgumentCaptor.forClass(AligoMessage.class);
        verify(updateAligoMessagePort).update(captor.capture());
        assertThat(captor.getValue().isCancelled()).isTrue();
    }

    @Test
    @DisplayName("즉시 발송 메시지 취소 성공: sendDate가 null이면 시간 검증을 건너뛴다")
    void shouldCancelImmediateMessageWhenSendDateIsNull() {
        // given
        setupClock();
        Long messageId = 200L;
        AligoMessage message = createRequestedMessage(messageId, "MID_002", null);
        when(findAligoMessagePort.findById(messageId)).thenReturn(Optional.of(message));

        // when
        cancelAligoMessageService.cancelMessage(messageId);

        // then
        verify(cancelAligoMessageExternalPort).cancel("MID_002");
        verify(updateAligoMessagePort).update(any(AligoMessage.class));
    }

    @Test
    @DisplayName("발송 예정시각 5분 이내이면 AligoCancelDeadlineExceededException을 던진다")
    void shouldThrowExceptionWhenCancelDeadlineExceeded() {
        // given
        setupClock();
        Long messageId = 300L;
        AligoMessage message = createRequestedMessage(messageId, "MID_003", NOW.plusMinutes(3));
        when(findAligoMessagePort.findById(messageId)).thenReturn(Optional.of(message));

        // when & then
        assertThatThrownBy(() -> cancelAligoMessageService.cancelMessage(messageId))
                .isInstanceOf(AligoCancelDeadlineExceededException.class);

        verifyNoInteractions(cancelAligoMessageExternalPort);
        verifyNoInteractions(updateAligoMessagePort);
    }

    @Test
    @DisplayName("이미 발송 완료(SENT→DELIVERED) 상태이면 InvalidAligoMessageStatusTransitionException을 던진다")
    void shouldThrowExceptionWhenMessageAlreadyDelivered() {
        // given
        setupClock();
        Long messageId = 400L;
        AligoMessage message = createDeliveredMessage(messageId);
        when(findAligoMessagePort.findById(messageId)).thenReturn(Optional.of(message));

        // when & then
        assertThatThrownBy(() -> cancelAligoMessageService.cancelMessage(messageId))
                .isInstanceOf(InvalidAligoMessageStatusTransitionException.class);

        verifyNoInteractions(cancelAligoMessageExternalPort);
        verifyNoInteractions(updateAligoMessagePort);
    }

    @Test
    @DisplayName("존재하지 않는 메시지 ID로 취소 시 AligoMessageNotFoundException을 던진다")
    void shouldThrowExceptionWhenMessageNotFound() {
        // given
        Long messageId = 999L;
        when(findAligoMessagePort.findById(messageId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cancelAligoMessageService.cancelMessage(messageId))
                .isInstanceOf(AligoMessageNotFoundException.class);

        verifyNoInteractions(cancelAligoMessageExternalPort);
        verifyNoInteractions(updateAligoMessagePort);
    }

    @Test
    @DisplayName("발송 예정시각 정확히 5분 전이면 취소 불가로 판단한다")
    void shouldRejectCancelWhenExactlyFiveMinutesBefore() {
        // given
        setupClock();
        Long messageId = 500L;
        AligoMessage message = createRequestedMessage(messageId, "MID_005", NOW.plusMinutes(5));
        when(findAligoMessagePort.findById(messageId)).thenReturn(Optional.of(message));

        // when & then
        assertThatThrownBy(() -> cancelAligoMessageService.cancelMessage(messageId))
                .isInstanceOf(AligoCancelDeadlineExceededException.class);

        verifyNoInteractions(cancelAligoMessageExternalPort);
    }

    private AligoMessage createRequestedMessage(Long id, String aligoMid, LocalDateTime sendDate) {
        return AligoMessage.from(AligoMessageSnapshotState.builder()
                .id(id)
                .senderKey("test-sender-key")
                .templateCode("TPL_TEST")
                .sender("01012345678")
                .messageType(AligoMessageType.AT)
                .content("테스트 메시지")
                .title("테스트")
                .messageStatus(AligoMessageStatus.REQUESTED)
                .aligoMid(aligoMid)
                .successCount(0)
                .failCount(0)
                .requestedAt(NOW.minusMinutes(10))
                .sendDate(sendDate)
                .status(EntityStatus.ACTIVE)
                .createdAt(NOW.minusMinutes(10))
                .modifiedAt(NOW.minusMinutes(10))
                .build());
    }

    private AligoMessage createDeliveredMessage(Long id) {
        return AligoMessage.from(AligoMessageSnapshotState.builder()
                .id(id)
                .senderKey("test-sender-key")
                .templateCode("TPL_TEST")
                .sender("01012345678")
                .messageType(AligoMessageType.AT)
                .content("테스트 메시지")
                .title("테스트")
                .messageStatus(AligoMessageStatus.DELIVERED)
                .aligoMid("MID_DELIVERED")
                .successCount(1)
                .failCount(0)
                .requestedAt(NOW.minusHours(1))
                .sentAt(NOW.minusMinutes(55))
                .status(EntityStatus.ACTIVE)
                .createdAt(NOW.minusHours(1))
                .modifiedAt(NOW.minusHours(1))
                .build());
    }
}
