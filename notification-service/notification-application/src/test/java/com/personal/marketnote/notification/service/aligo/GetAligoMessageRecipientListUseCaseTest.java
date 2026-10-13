package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.domain.aligo.*;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoMessageRecipientListResult;
import com.personal.marketnote.notification.port.out.aligo.FindAligoMessagePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAligoMessageRecipientListUseCaseTest {

    @InjectMocks
    private GetAligoMessageRecipientListService getAligoMessageRecipientListService;

    @Mock
    private FindAligoMessagePort findAligoMessagePort;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 4, 16, 12, 0, 0);

    @Test
    @DisplayName("메시지의 수신자 목록을 반환한다")
    void shouldReturnRecipientListForMessage() {
        // given
        Long messageId = 100L;
        AligoMessage message = createMessageWithRecipients(messageId, List.of(
                createRecipientState(1L, "01011111111", "홍길동", AligoMessageRecipientStatus.SUCCESS,
                        "MSG_001", "0", "성공", NOW.minusMinutes(5), NOW.minusMinutes(3), NOW.minusMinutes(2)),
                createRecipientState(2L, "01022222222", "김철수", AligoMessageRecipientStatus.FAILED,
                        "MSG_002", "-1", "수신 실패", NOW.minusMinutes(5), NOW.minusMinutes(3), NOW.minusMinutes(2))
        ));
        when(findAligoMessagePort.findById(messageId)).thenReturn(Optional.of(message));

        // when
        GetAligoMessageRecipientListResult result = getAligoMessageRecipientListService.getRecipientList(messageId);

        // then
        assertThat(result.messageId()).isEqualTo(messageId);
        assertThat(result.recipients()).hasSize(2);

        GetAligoMessageRecipientListResult.RecipientDetail first = result.recipients().get(0);
        assertThat(first.receiver()).isEqualTo("01011111111");
        assertThat(first.recvName()).isEqualTo("홍길동");
        assertThat(first.recipientStatus()).isEqualTo(AligoMessageRecipientStatus.SUCCESS);
        assertThat(first.msgId()).isEqualTo("MSG_001");

        GetAligoMessageRecipientListResult.RecipientDetail second = result.recipients().get(1);
        assertThat(second.receiver()).isEqualTo("01022222222");
        assertThat(second.recipientStatus()).isEqualTo(AligoMessageRecipientStatus.FAILED);

        verify(findAligoMessagePort).findById(messageId);
    }

    @Test
    @DisplayName("존재하지 않는 메시지 ID로 조회 시 AligoMessageNotFoundException을 던진다")
    void shouldThrowExceptionWhenMessageNotFound() {
        // given
        Long messageId = 999L;
        when(findAligoMessagePort.findById(messageId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getAligoMessageRecipientListService.getRecipientList(messageId))
                .isInstanceOf(AligoMessageNotFoundException.class);
    }

    @Test
    @DisplayName("수신자가 없는 메시지의 경우 빈 수신자 목록을 반환한다")
    void shouldReturnEmptyRecipientsWhenMessageHasNone() {
        // given
        Long messageId = 200L;
        AligoMessage message = createMessageWithRecipients(messageId, List.of());
        when(findAligoMessagePort.findById(messageId)).thenReturn(Optional.of(message));

        // when
        GetAligoMessageRecipientListResult result = getAligoMessageRecipientListService.getRecipientList(messageId);

        // then
        assertThat(result.messageId()).isEqualTo(messageId);
        assertThat(result.recipients()).isEmpty();
    }

    private AligoMessageRecipientSnapshotState createRecipientState(
            Long id, String receiver, String recvName, AligoMessageRecipientStatus status,
            String msgId, String rslt, String rsltMessage,
            LocalDateTime sentAt, LocalDateTime resultAt, LocalDateTime reportAt
    ) {
        return AligoMessageRecipientSnapshotState.builder()
                .id(id)
                .receiver(receiver)
                .recvName(recvName)
                .recipientStatus(status)
                .msgId(msgId)
                .rslt(rslt)
                .rsltMessage(rsltMessage)
                .sentAt(sentAt)
                .resultAt(resultAt)
                .reportAt(reportAt)
                .build();
    }

    private AligoMessage createMessageWithRecipients(Long id, List<AligoMessageRecipientSnapshotState> recipients) {
        return AligoMessage.from(AligoMessageSnapshotState.builder()
                .id(id)
                .senderKey("test-sender-key")
                .templateCode("TPL_TEST")
                .sender("01012345678")
                .messageType(AligoMessageType.AT)
                .content("테스트 메시지")
                .title("테스트")
                .messageStatus(AligoMessageStatus.SENT)
                .aligoMid("MID_TEST")
                .successCount(1)
                .failCount(0)
                .requestedAt(NOW.minusHours(1))
                .sentAt(NOW.minusMinutes(55))
                .recipients(recipients)
                .status(EntityStatus.ACTIVE)
                .createdAt(NOW.minusHours(1))
                .modifiedAt(NOW.minusHours(1))
                .build());
    }
}
