package com.personal.marketnote.file.port.out.servicecommunication;

import com.personal.marketnote.file.domain.servicecommunication.FileServiceCommunicationHistory;

/**
 * 파일 서비스 간 통신 기록 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-31
 * @Description 파일 서비스 간 통신 기록 저장 기능을 제공합니다.
 */
public interface SaveFileServiceCommunicationHistoryPort {
    /**
     * @param history 파일 서비스 통신 기록
     * @return 저장된 파일 서비스 통신 기록 {@link FileServiceCommunicationHistory}
     * @Date 2026-01-31
     * @Author 성효빈
     * @Description 파일 서비스 통신 기록을 저장합니다.
     */
    FileServiceCommunicationHistory save(FileServiceCommunicationHistory history);
}
