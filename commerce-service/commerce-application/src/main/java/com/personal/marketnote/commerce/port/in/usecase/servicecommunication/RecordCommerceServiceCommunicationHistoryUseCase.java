package com.personal.marketnote.commerce.port.in.usecase.servicecommunication;

import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationHistory;
import com.personal.marketnote.commerce.port.in.command.servicecommunication.CommerceServiceCommunicationHistoryCommand;

/**
 * 커머스 서비스 간 통신 기록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-31
 * @Description 커머스 서비스 간 통신 기록 기능을 제공합니다.
 */
public interface RecordCommerceServiceCommunicationHistoryUseCase {
    /**
     * @param command 커머스 서비스 통신 기록 커맨드
     * @return 커머스 서비스 통신 기록 {@link CommerceServiceCommunicationHistory}
     * @Date 2026-02-19
     * @Author 성효빈
     * @Description 커머스 서비스 통신 기록을 저장합니다.
     */
    CommerceServiceCommunicationHistory record(CommerceServiceCommunicationHistoryCommand command);
}
