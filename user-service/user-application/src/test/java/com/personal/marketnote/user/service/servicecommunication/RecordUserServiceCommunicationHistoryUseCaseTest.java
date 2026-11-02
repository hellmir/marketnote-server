package com.personal.marketnote.user.service.servicecommunication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.user.domain.servicecommunication.UserServiceCommunicationHistory;
import com.personal.marketnote.user.domain.servicecommunication.UserServiceCommunicationSenderType;
import com.personal.marketnote.user.domain.servicecommunication.UserServiceCommunicationTargetType;
import com.personal.marketnote.user.domain.servicecommunication.UserServiceCommunicationType;
import com.personal.marketnote.user.port.in.command.servicecommunication.UserServiceCommunicationHistoryCommand;
import com.personal.marketnote.user.port.out.servicecommunication.SaveUserServiceCommunicationHistoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordUserServiceCommunicationHistoryUseCaseTest {
    @InjectMocks
    private RecordUserServiceCommunicationHistoryService recordUserServiceCommunicationHistoryService;

    @Mock
    private SaveUserServiceCommunicationHistoryPort saveServiceCommunicationHistoryPort;

    @Test
    @DisplayName("서비스 간 통신 기록 커맨드를 전송하면 통신 기록을 저장하고 반환한다")
    void record_validCommand_savesAndReturnsHistory() {
        // given
        JsonNode payloadJson = new ObjectMapper().createObjectNode().put("key", "value");

        UserServiceCommunicationHistoryCommand command = UserServiceCommunicationHistoryCommand.builder()
                .targetType(UserServiceCommunicationTargetType.USER_POINT)
                .targetId("123")
                .communicationType(UserServiceCommunicationType.REQUEST)
                .sender(UserServiceCommunicationSenderType.REWARD)
                .exception(null)
                .payload("{\"key\":\"value\"}")
                .payloadJson(payloadJson)
                .build();

        ArgumentCaptor<UserServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(UserServiceCommunicationHistory.class);

        UserServiceCommunicationHistory savedHistory = mock(UserServiceCommunicationHistory.class);
        when(saveServiceCommunicationHistoryPort.save(any(UserServiceCommunicationHistory.class)))
                .thenReturn(savedHistory);

        // when
        UserServiceCommunicationHistory result = recordUserServiceCommunicationHistoryService.record(command);

        // then
        assertThat(result).isSameAs(savedHistory);

        verify(saveServiceCommunicationHistoryPort).save(captor.capture());
        UserServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(UserServiceCommunicationTargetType.USER_POINT);
        assertThat(captured.getTargetId()).isEqualTo("123");
        assertThat(captured.getCommunicationType()).isEqualTo(UserServiceCommunicationType.REQUEST);
        assertThat(captured.getSender()).isEqualTo(UserServiceCommunicationSenderType.REWARD);
        assertThat(captured.getException()).isNull();
        assertThat(captured.getPayload()).isEqualTo("{\"key\":\"value\"}");
        assertThat(captured.getPayloadJson()).isEqualTo(payloadJson);

        verifyNoMoreInteractions(saveServiceCommunicationHistoryPort);
    }

    @Test
    @DisplayName("예외 정보가 포함된 통신 기록을 저장한다")
    void record_commandWithException_savesExceptionInfo() {
        // given
        UserServiceCommunicationHistoryCommand command = UserServiceCommunicationHistoryCommand.builder()
                .targetType(UserServiceCommunicationTargetType.USER_POINT)
                .targetId("456")
                .communicationType(UserServiceCommunicationType.RESPONSE)
                .sender(UserServiceCommunicationSenderType.USER)
                .exception("java.lang.RuntimeException: 포인트 조회 실패")
                .payload(null)
                .payloadJson(null)
                .build();

        ArgumentCaptor<UserServiceCommunicationHistory> captor =
                ArgumentCaptor.forClass(UserServiceCommunicationHistory.class);

        UserServiceCommunicationHistory savedHistory = mock(UserServiceCommunicationHistory.class);
        when(saveServiceCommunicationHistoryPort.save(any(UserServiceCommunicationHistory.class)))
                .thenReturn(savedHistory);

        // when
        UserServiceCommunicationHistory result = recordUserServiceCommunicationHistoryService.record(command);

        // then
        assertThat(result).isSameAs(savedHistory);

        verify(saveServiceCommunicationHistoryPort).save(captor.capture());
        UserServiceCommunicationHistory captured = captor.getValue();
        assertThat(captured.getTargetType()).isEqualTo(UserServiceCommunicationTargetType.USER_POINT);
        assertThat(captured.getTargetId()).isEqualTo("456");
        assertThat(captured.getCommunicationType()).isEqualTo(UserServiceCommunicationType.RESPONSE);
        assertThat(captured.getSender()).isEqualTo(UserServiceCommunicationSenderType.USER);
        assertThat(captured.getException()).isEqualTo("java.lang.RuntimeException: 포인트 조회 실패");
        assertThat(captured.getPayload()).isNull();
        assertThat(captured.getPayloadJson()).isNull();

        verifyNoMoreInteractions(saveServiceCommunicationHistoryPort);
    }
}
