package com.personal.marketnote.file.adapter.out.persistence.servicecommunication.repository;

import com.personal.marketnote.file.adapter.out.persistence.servicecommunication.entity.FileServiceCommunicationHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileServiceCommunicationHistoryJpaRepository
        extends JpaRepository<FileServiceCommunicationHistoryJpaEntity, Long> {
}
