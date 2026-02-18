package com.personal.marketnote.commerce.port.in.usecase.servicecommunication;

import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationHistory;
import com.personal.marketnote.commerce.port.in.command.servicecommunication.CommerceServiceCommunicationHistoryCommand;

public interface RecordCommerceServiceCommunicationHistoryUseCase {
    /**
     * @param command 커머스 서비스 통신 기록 커맨드
     * @return 커머스 서비스 통신 기록 {@link CommerceServiceCommunicationHistory}
     * @Date 2026-02-03
     * @Author 성효빈
     * @Description 커머스 서비스 통신 기록을 저장합니다.
     */
    CommerceServiceCommunicationHistory record(CommerceServiceCommunicationHistoryCommand command);
}
