package com.personal.marketnote.community.port.out.servicecommunication;

import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationHistory;

/**
 * 서비스 간 통신 기록 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-31
 * @Description 서비스 간 통신 기록 저장 기능을 제공합니다.
 */
public interface SaveCommunityServiceCommunicationHistoryPort {
    /**
     * @param history 서비스 간 통신 기록
     * @return 저장된 서비스 간 통신 기록 {@link CommunityServiceCommunicationHistory}
     * @Date 2026-01-31
     * @Author 성효빈
     * @Description 서비스 간 통신 기록을 저장합니다.
     */
    CommunityServiceCommunicationHistory save(CommunityServiceCommunicationHistory history);
}
