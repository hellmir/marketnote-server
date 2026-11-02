package com.personal.marketnote.file.service.servicecommunication;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.file.domain.servicecommunication.FileServiceCommunicationHistory;
import com.personal.marketnote.file.mapper.FileServiceCommunicationHistoryCommandToStateMapper;
import com.personal.marketnote.file.port.in.command.servicecommunication.FileServiceCommunicationHistoryCommand;
import com.personal.marketnote.file.port.in.usecase.servicecommunication.RecordFileServiceCommunicationHistoryUseCase;
import com.personal.marketnote.file.port.out.servicecommunication.SaveFileServiceCommunicationHistoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, propagation = REQUIRES_NEW)
public class RecordFileServiceCommunicationHistoryService
        implements RecordFileServiceCommunicationHistoryUseCase {
    private final SaveFileServiceCommunicationHistoryPort saveServiceCommunicationHistoryPort;

    @Override
    public FileServiceCommunicationHistory record(FileServiceCommunicationHistoryCommand command) {
        return saveServiceCommunicationHistoryPort.save(
                FileServiceCommunicationHistory.from(
                        FileServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command)
                )
        );
    }
}
