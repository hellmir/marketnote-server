package com.personal.marketnote.user.port.out.servicecommunication;

import com.personal.marketnote.user.domain.servicecommunication.UserServiceCommunicationHistory;

/**
 * 서비스 간 통신 기록 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-31
 * @Description 서비스 간 통신 기록 저장 기능을 제공합니다.
 */
public interface SaveUserServiceCommunicationHistoryPort {
    /**
     * @param history 서비스 간 통신 기록
     * @return 저장된 서비스 간 통신 기록 {@link UserServiceCommunicationHistory}
     * @Date 2026-01-31
     * @Author 성효빈
     * @Description 서비스 간 통신 기록을 저장합니다.
     */
    UserServiceCommunicationHistory save(UserServiceCommunicationHistory history);
}
