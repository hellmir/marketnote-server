package com.personal.marketnote.commerce.port.in.usecase.vendorcommunication;

import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationHistory;
import com.personal.marketnote.commerce.port.in.command.vendorcommunication.CommerceVendorCommunicationHistoryCommand;

public interface RecordCommerceVendorCommunicationHistoryUseCase {
    /**
     * @param command 커머스 벤더 통신 기록 커맨드
     * @return 커머스 벤더 통신 기록 {@link CommerceVendorCommunicationHistory}
     * @Date 2026-02-25
     * @Author 성효빈
     * @Description 커머스 벤더 통신 기록을 저장합니다.
     */
    CommerceVendorCommunicationHistory record(CommerceVendorCommunicationHistoryCommand command);
}
