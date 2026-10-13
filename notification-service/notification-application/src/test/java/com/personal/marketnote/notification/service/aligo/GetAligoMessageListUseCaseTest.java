package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.domain.aligo.*;
import com.personal.marketnote.notification.port.in.command.GetAligoMessageListCommand;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoMessageListResult;
import com.personal.marketnote.notification.port.out.aligo.FindAligoMessageListPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAligoMessageListUseCaseTest {

    @InjectMocks
    private GetAligoMessageListService getAligoMessageListService;

    @Mock
    private FindAligoMessageListPort findAligoMessageListPort;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 4, 16, 12, 0, 0);

    @Test
    @DisplayName("메시지 목록을 페이지네이션과 함께 반환한다")
    void shouldReturnMessageListWithPagination() {
        // given
        LocalDateTime startDate = NOW.minusDays(7);
        LocalDateTime endDate = NOW;
        GetAligoMessageListCommand command = new GetAligoMessageListCommand(1, 10, startDate, endDate);

        AligoMessage message = createSentMessage(1L, "MID_001");
        when(findAligoMessageListPort.findMessages(startDate, endDate, 1, 10))
                .thenReturn(List.of(message));
        when(findAligoMessageListPort.countMessages(startDate, endDate))
                .thenReturn(1L);

        // when
        GetAligoMessageListResult result = getAligoMessageListService.getMessageList(command);

        // then
        assertThat(result.messages()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.currentPage()).isEqualTo(1);

        GetAligoMessageListResult.AligoMessageSummary summary = result.messages().get(0);
        assertThat(summary.id()).isEqualTo(1L);
        assertThat(summary.aligoMid()).isEqualTo("MID_001");
        assertThat(summary.messageStatus()).isEqualTo(AligoMessageStatus.SENT);

        verify(findAligoMessageListPort).findMessages(startDate, endDate, 1, 10);
        verify(findAligoMessageListPort).countMessages(startDate, endDate);
    }

    @Test
    @DisplayName("메시지가 없으면 빈 목록을 반환한다")
    void shouldReturnEmptyListWhenNoMessages() {
        // given
        LocalDateTime startDate = NOW.minusDays(7);
        LocalDateTime endDate = NOW;
        GetAligoMessageListCommand command = new GetAligoMessageListCommand(1, 10, startDate, endDate);

        when(findAligoMessageListPort.findMessages(startDate, endDate, 1, 10))
                .thenReturn(List.of());
        when(findAligoMessageListPort.countMessages(startDate, endDate))
                .thenReturn(0L);

        // when
        GetAligoMessageListResult result = getAligoMessageListService.getMessageList(command);

        // then
        assertThat(result.messages()).isEmpty();
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();
    }

    @Test
    @DisplayName("totalPages를 limit 기반으로 올림 계산한다")
    void shouldCalculateTotalPagesWithCeiling() {
        // given
        LocalDateTime startDate = NOW.minusDays(7);
        LocalDateTime endDate = NOW;
        GetAligoMessageListCommand command = new GetAligoMessageListCommand(1, 10, startDate, endDate);

        List<AligoMessage> messages = List.of(
                createSentMessage(1L, "MID_001"),
                createSentMessage(2L, "MID_002")
        );
        when(findAligoMessageListPort.findMessages(startDate, endDate, 1, 10))
                .thenReturn(messages);
        when(findAligoMessageListPort.countMessages(startDate, endDate))
                .thenReturn(25L);

        // when
        GetAligoMessageListResult result = getAligoMessageListService.getMessageList(command);

        // then
        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.totalElements()).isEqualTo(25L);
    }

    private AligoMessage createSentMessage(Long id, String aligoMid) {
        return AligoMessage.from(AligoMessageSnapshotState.builder()
                .id(id)
                .senderKey("test-sender-key")
                .templateCode("TPL_TEST")
                .sender("01012345678")
                .messageType(AligoMessageType.AT)
                .content("테스트 메시지")
                .title("테스트")
                .messageStatus(AligoMessageStatus.SENT)
                .aligoMid(aligoMid)
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
