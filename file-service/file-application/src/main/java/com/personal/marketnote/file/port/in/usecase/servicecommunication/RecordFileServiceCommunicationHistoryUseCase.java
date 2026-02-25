package com.personal.marketnote.file.port.in.usecase.servicecommunication;

import com.personal.marketnote.file.domain.servicecommunication.FileServiceCommunicationHistory;
import com.personal.marketnote.file.port.in.command.servicecommunication.FileServiceCommunicationHistoryCommand;

/**
 * 파일 서비스 간 통신 기록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-31
 * @Description 파일 서비스 간 통신 기록 기능을 제공합니다.
 */
public interface RecordFileServiceCommunicationHistoryUseCase {
    /**
     * @param command 파일 서비스 통신 기록 커맨드
     * @return 파일 서비스 통신 기록 {@link FileServiceCommunicationHistory}
     * @Date 2026-01-31
     * @Author 성효빈
     * @Description 파일 서비스 통신 기록을 저장합니다.
     */
    FileServiceCommunicationHistory record(FileServiceCommunicationHistoryCommand command);
}
