package com.personal.marketnote.commerce.port.out.servicecommunication;

import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationHistory;

/**
 * 커머스 서비스 간 통신 기록 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-31
 * @Description 커머스 서비스 간 통신 기록 저장 기능을 제공합니다.
 */
public interface SaveCommerceServiceCommunicationHistoryPort {
    /**
     * @param history 커머스 서비스 통신 기록
     * @return 저장된 커머스 서비스 통신 기록 {@link CommerceServiceCommunicationHistory}
     * @Date 2026-01-31
     * @Author 성효빈
     * @Description 커머스 서비스 통신 기록을 저장합니다.
     */
    CommerceServiceCommunicationHistory save(CommerceServiceCommunicationHistory history);
}
