package com.personal.marketnote.file.adapter.out.persistence.servicecommunication;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.file.adapter.out.persistence.servicecommunication.entity.FileServiceCommunicationHistoryJpaEntity;
import com.personal.marketnote.file.adapter.out.persistence.servicecommunication.repository.FileServiceCommunicationHistoryJpaRepository;
import com.personal.marketnote.file.domain.servicecommunication.FileServiceCommunicationHistory;
import com.personal.marketnote.file.port.out.servicecommunication.SaveFileServiceCommunicationHistoryPort;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
public class FileServiceCommunicationHistoryPersistenceAdapter
        implements SaveFileServiceCommunicationHistoryPort {
    private final FileServiceCommunicationHistoryJpaRepository repository;

    @Override
    public FileServiceCommunicationHistory save(FileServiceCommunicationHistory history) {
        FileServiceCommunicationHistoryJpaEntity savedEntity = repository.save(
                FileServiceCommunicationHistoryJpaEntity.from(history)
        );
        return savedEntity.toDomain();
    }
}
