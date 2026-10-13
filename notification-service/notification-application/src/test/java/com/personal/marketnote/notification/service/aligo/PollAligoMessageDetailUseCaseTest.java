package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.domain.aligo.*;
import com.personal.marketnote.notification.port.out.aligo.FindPendingAligoMessagesPort;
import com.personal.marketnote.notification.port.out.aligo.GetAligoMessageDetailExternalPort;
import com.personal.marketnote.notification.port.out.aligo.GetAligoMessageDetailExternalPort.AligoRecipientDetailResult;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PollAligoMessageDetailUseCaseTest {

    @InjectMocks
    private PollAligoMessageDetailService pollAligoMessageDetailService;

    @Mock
    private FindPendingAligoMessagesPort findPendingAligoMessagesPort;

    @Mock
    private GetAligoMessageDetailExternalPort getAligoMessageDetailExternalPort;

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
    @DisplayName("미확정 메시지가 없으면 0건을 반환한다")
    void shouldReturnZeroWhenNoPendingMessages() {
        // given
        setupClock();
        when(findPendingAligoMessagesPort.findPendingMessages(any(LocalDateTime.class)))
                .thenReturn(List.of());

        // when
        int result = pollAligoMessageDetailService.poll();

        // then
        assertThat(result).isZero();
        verifyNoInteractions(getAligoMessageDetailExternalPort);
        verifyNoInteractions(updateAligoMessagePort);
    }

    @Test
    @DisplayName("수신자 결과가 성공이면 recipient를 SUCCESS로 전이하고 메시지를 DELIVERED로 전이한다")
    void shouldTransitionRecipientToSuccessAndMessageToDelivered() {
        // given
        setupClock();
        AligoMessage message = createSentMessageWithRecipients("MID_001", List.of("01012345678"));
        when(findPendingAligoMessagesPort.findPendingMessages(any(LocalDateTime.class)))
                .thenReturn(List.of(message));

        LocalDateTime detailSentAt = NOW.minusMinutes(5);
        LocalDateTime detailResultAt = NOW.minusMinutes(3);
        LocalDateTime detailReportAt = NOW.minusMinutes(2);
        when(getAligoMessageDetailExternalPort.getMessageDetail("MID_001"))
                .thenReturn(List.of(new AligoRecipientDetailResult(
                        "MSG_001", "01012345678", "0", "성공",
                        detailSentAt, detailResultAt, detailReportAt
                )));

        // when
        int result = pollAligoMessageDetailService.poll();

        // then
        assertThat(result).isEqualTo(1);

        ArgumentCaptor<AligoMessage> captor = ArgumentCaptor.forClass(AligoMessage.class);
        verify(updateAligoMessagePort).update(captor.capture());

        AligoMessage updated = captor.getValue();
        assertThat(updated.isDelivered()).isTrue();
        assertThat(updated.getRecipients().get(0).isSuccess()).isTrue();
        assertThat(updated.getRecipients().get(0).getMsgId()).isEqualTo("MSG_001");
    }

    @Test
    @DisplayName("수신자 결과가 실패이면 recipient를 FAILED로 전이하고 메시지를 FAILED로 전이한다")
    void shouldTransitionRecipientToFailedAndMessageToFailed() {
        // given
        setupClock();
        AligoMessage message = createSentMessageWithRecipients("MID_002", List.of("01098765432"));
        when(findPendingAligoMessagesPort.findPendingMessages(any(LocalDateTime.class)))
                .thenReturn(List.of(message));
        when(getAligoMessageDetailExternalPort.getMessageDetail("MID_002"))
                .thenReturn(List.of(new AligoRecipientDetailResult(
                        "MSG_002", "01098765432", "-1", "수신 실패",
                        NOW.minusMinutes(5), NOW.minusMinutes(3), NOW.minusMinutes(2)
                )));

        // when
        int result = pollAligoMessageDetailService.poll();

        // then
        assertThat(result).isEqualTo(1);

        ArgumentCaptor<AligoMessage> captor = ArgumentCaptor.forClass(AligoMessage.class);
        verify(updateAligoMessagePort).update(captor.capture());

        AligoMessage updated = captor.getValue();
        assertThat(updated.isFailed()).isTrue();
        assertThat(updated.getRecipients().get(0).isFailed()).isTrue();
        assertThat(updated.getRecipients().get(0).getRslt()).isEqualTo("-1");
    }

    @Test
    @DisplayName("msgId가 Q prefix이면 recipient를 PENDING 상태로 유지한다")
    void shouldKeepRecipientPendingWhenMsgIdHasQPrefix() {
        // given
        setupClock();
        AligoMessage message = createSentMessageWithRecipients("MID_003", List.of("01011112222"));
        when(findPendingAligoMessagesPort.findPendingMessages(any(LocalDateTime.class)))
                .thenReturn(List.of(message));
        when(getAligoMessageDetailExternalPort.getMessageDetail("MID_003"))
                .thenReturn(List.of(new AligoRecipientDetailResult(
                        "Q12345678", "01011112222", null, null,
                        null, null, null
                )));

        // when
        int result = pollAligoMessageDetailService.poll();

        // then
        assertThat(result).isEqualTo(1);

        ArgumentCaptor<AligoMessage> captor = ArgumentCaptor.forClass(AligoMessage.class);
        verify(updateAligoMessagePort).update(captor.capture());

        AligoMessage updated = captor.getValue();
        assertThat(updated.isSent()).isTrue();
        assertThat(updated.getRecipients().get(0).isPending()).isTrue();
    }

    @Test
    @DisplayName("복수 수신자 중 일부 성공, 일부 Q prefix이면 메시지는 SENT 상태를 유지한다")
    void shouldKeepMessageSentWhenSomeRecipientsStillPending() {
        // given
        setupClock();
        AligoMessage message = createSentMessageWithRecipients("MID_004", List.of("01011111111", "01022222222"));
        when(findPendingAligoMessagesPort.findPendingMessages(any(LocalDateTime.class)))
                .thenReturn(List.of(message));
        when(getAligoMessageDetailExternalPort.getMessageDetail("MID_004"))
                .thenReturn(List.of(
                        new AligoRecipientDetailResult(
                                "MSG_010", "01011111111", "0", "성공",
                                NOW.minusMinutes(5), NOW.minusMinutes(3), NOW.minusMinutes(2)
                        ),
                        new AligoRecipientDetailResult(
                                "Q999999", "01022222222", null, null,
                                null, null, null
                        )
                ));

        // when
        int result = pollAligoMessageDetailService.poll();

        // then
        assertThat(result).isEqualTo(1);

        ArgumentCaptor<AligoMessage> captor = ArgumentCaptor.forClass(AligoMessage.class);
        verify(updateAligoMessagePort).update(captor.capture());

        AligoMessage updated = captor.getValue();
        assertThat(updated.isSent()).isTrue();
        assertThat(updated.getRecipients().get(0).isSuccess()).isTrue();
        assertThat(updated.getRecipients().get(1).isPending()).isTrue();
    }

    @Test
    @DisplayName("24시간 경과 메시지의 PENDING 수신자를 강제 FAILED 처리한다")
    void shouldForceResolveExpiredPendingRecipients() {
        // given
        setupClock();
        AligoMessage message = createSentMessageWithRecipientsAndCreatedAt(
                "MID_005", List.of("01033333333"), NOW.minusHours(25)
        );
        when(findPendingAligoMessagesPort.findPendingMessages(any(LocalDateTime.class)))
                .thenReturn(List.of(message));
        when(getAligoMessageDetailExternalPort.getMessageDetail("MID_005"))
                .thenReturn(List.of(new AligoRecipientDetailResult(
                        "Q777777", "01033333333", null, null,
                        null, null, null
                )));

        // when
        int result = pollAligoMessageDetailService.poll();

        // then
        assertThat(result).isEqualTo(1);

        ArgumentCaptor<AligoMessage> captor = ArgumentCaptor.forClass(AligoMessage.class);
        verify(updateAligoMessagePort).update(captor.capture());

        AligoMessage updated = captor.getValue();
        assertThat(updated.isFailed()).isTrue();
        assertThat(updated.getRecipients().get(0).isFailed()).isTrue();
        assertThat(updated.getRecipients().get(0).getRslt()).isEqualTo("TIMEOUT");
    }

    @Test
    @DisplayName("복수 메시지를 순차 처리하고 처리 건수를 반환한다")
    void shouldProcessMultipleMessagesAndReturnCount() {
        // given
        setupClock();
        AligoMessage message1 = createSentMessageWithRecipients("MID_A", List.of("01011111111"));
        AligoMessage message2 = createSentMessageWithRecipients("MID_B", List.of("01022222222"));
        when(findPendingAligoMessagesPort.findPendingMessages(any(LocalDateTime.class)))
                .thenReturn(List.of(message1, message2));
        when(getAligoMessageDetailExternalPort.getMessageDetail("MID_A"))
                .thenReturn(List.of(new AligoRecipientDetailResult(
                        "MSG_A1", "01011111111", "0", "성공",
                        NOW.minusMinutes(5), NOW.minusMinutes(3), NOW.minusMinutes(2)
                )));
        when(getAligoMessageDetailExternalPort.getMessageDetail("MID_B"))
                .thenReturn(List.of(new AligoRecipientDetailResult(
                        "MSG_B1", "01022222222", "0", "성공",
                        NOW.minusMinutes(4), NOW.minusMinutes(2), NOW.minusMinutes(1)
                )));

        // when
        int result = pollAligoMessageDetailService.poll();

        // then
        assertThat(result).isEqualTo(2);
        verify(updateAligoMessagePort, times(2)).update(any(AligoMessage.class));
    }

    @Test
    @DisplayName("외부 API 호출 실패 시 해당 메시지를 건너뛰고 나머지를 계속 처리한다")
    void shouldSkipMessageWhenExternalApiFailsAndContinueProcessing() {
        // given
        setupClock();
        AligoMessage message1 = createSentMessageWithRecipients("MID_FAIL", List.of("01011111111"));
        AligoMessage message2 = createSentMessageWithRecipients("MID_OK", List.of("01022222222"));
        when(findPendingAligoMessagesPort.findPendingMessages(any(LocalDateTime.class)))
                .thenReturn(List.of(message1, message2));
        when(getAligoMessageDetailExternalPort.getMessageDetail("MID_FAIL"))
                .thenThrow(new AligoApiRequestFailedException("외부 API 오류"));
        when(getAligoMessageDetailExternalPort.getMessageDetail("MID_OK"))
                .thenReturn(List.of(new AligoRecipientDetailResult(
                        "MSG_OK1", "01022222222", "0", "성공",
                        NOW.minusMinutes(5), NOW.minusMinutes(3), NOW.minusMinutes(2)
                )));

        // when
        int result = pollAligoMessageDetailService.poll();

        // then
        assertThat(result).isEqualTo(1);
        verify(updateAligoMessagePort, times(1)).update(any(AligoMessage.class));
    }

    @Test
    @DisplayName("복수 수신자 중 성공과 실패가 혼합되면 메시지를 DELIVERED로 전이한다")
    void shouldTransitionToDeliveredWhenMixedSuccessAndFailure() {
        // given
        setupClock();
        AligoMessage message = createSentMessageWithRecipients("MID_MIX", List.of("01011111111", "01022222222"));
        when(findPendingAligoMessagesPort.findPendingMessages(any(LocalDateTime.class)))
                .thenReturn(List.of(message));
        when(getAligoMessageDetailExternalPort.getMessageDetail("MID_MIX"))
                .thenReturn(List.of(
                        new AligoRecipientDetailResult(
                                "MSG_S1", "01011111111", "0", "성공",
                                NOW.minusMinutes(5), NOW.minusMinutes(3), NOW.minusMinutes(2)
                        ),
                        new AligoRecipientDetailResult(
                                "MSG_F1", "01022222222", "-1", "수신 실패",
                                NOW.minusMinutes(5), NOW.minusMinutes(3), NOW.minusMinutes(2)
                        )
                ));

        // when
        int result = pollAligoMessageDetailService.poll();

        // then
        ArgumentCaptor<AligoMessage> captor = ArgumentCaptor.forClass(AligoMessage.class);
        verify(updateAligoMessagePort).update(captor.capture());

        AligoMessage updated = captor.getValue();
        assertThat(updated.isDelivered()).isTrue();
        assertThat(updated.getRecipients().get(0).isSuccess()).isTrue();
        assertThat(updated.getRecipients().get(1).isFailed()).isTrue();
    }

    private AligoMessage createSentMessageWithRecipients(String aligoMid, List<String> receivers) {
        return createSentMessageWithRecipientsAndCreatedAt(aligoMid, receivers, NOW.minusHours(1));
    }

    private AligoMessage createSentMessageWithRecipientsAndCreatedAt(
            String aligoMid, List<String> receivers, LocalDateTime createdAt
    ) {
        List<AligoMessageRecipientSnapshotState> recipientStates = receivers.stream()
                .map(receiver -> AligoMessageRecipientSnapshotState.builder()
                        .id((long) receiver.hashCode())
                        .receiver(receiver)
                        .recvName("테스트")
                        .recipientStatus(AligoMessageRecipientStatus.PENDING)
                        .build())
                .toList();

        return AligoMessage.from(AligoMessageSnapshotState.builder()
                .id((long) aligoMid.hashCode())
                .senderKey("test-sender-key")
                .templateCode("TPL_TEST")
                .sender("01012345678")
                .messageType(AligoMessageType.AT)
                .content("테스트 메시지")
                .title("테스트")
                .messageStatus(AligoMessageStatus.SENT)
                .aligoMid(aligoMid)
                .successCount(receivers.size())
                .failCount(0)
                .requestedAt(createdAt.minusMinutes(5))
                .sentAt(createdAt)
                .recipients(recipientStates)
                .status(EntityStatus.ACTIVE)
                .createdAt(createdAt)
                .modifiedAt(createdAt)
                .build());
    }
}
