package com.personal.marketnote.file.service.servicecommunication;

import com.personal.marketnote.file.domain.servicecommunication.FileServiceCommunicationHistory;
import com.personal.marketnote.file.domain.servicecommunication.FileServiceCommunicationSenderType;
import com.personal.marketnote.file.domain.servicecommunication.FileServiceCommunicationTargetType;
import com.personal.marketnote.file.domain.servicecommunication.FileServiceCommunicationType;
import com.personal.marketnote.file.port.in.command.servicecommunication.FileServiceCommunicationHistoryCommand;
import com.personal.marketnote.file.port.out.servicecommunication.SaveFileServiceCommunicationHistoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecordFileServiceCommunicationHistoryUseCase 테스트")
class RecordFileServiceCommunicationHistoryUseCaseTest {
    @InjectMocks private RecordFileServiceCommunicationHistoryService recordService;
    @Mock private SaveFileServiceCommunicationHistoryPort saveServiceCommunicationHistoryPort;

    @Test
    @DisplayName("통신 기록 저장 시 Command를 CreateState로 매핑하여 저장하고 결과를 반환한다")
    void shouldRecordCommunicationHistorySuccessfully() {
        FileServiceCommunicationHistoryCommand command = FileServiceCommunicationHistoryCommand.builder()
                .targetType(FileServiceCommunicationTargetType.GENERAL).targetId("file-123")
                .communicationType(FileServiceCommunicationType.REQUEST).sender(FileServiceCommunicationSenderType.COMMUNITY)
                .payload("{\"fileId\": 123}").build();
        when(saveServiceCommunicationHistoryPort.save(any(FileServiceCommunicationHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        FileServiceCommunicationHistory result = recordService.record(command);
        assertThat(result.getTargetType()).isEqualTo(FileServiceCommunicationTargetType.GENERAL);
        assertThat(result.getTargetId()).isEqualTo("file-123");
        verify(saveServiceCommunicationHistoryPort).save(any(FileServiceCommunicationHistory.class));
    }
}
