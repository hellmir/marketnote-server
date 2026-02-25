package com.personal.marketnote.community.port.in.usecase.servicecommunication;

import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationHistory;
import com.personal.marketnote.community.port.in.command.servicecommunication.CommunityServiceCommunicationHistoryCommand;

/**
 * 서비스 간 통신 기록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-31
 * @Description 서비스 간 통신 실패 시 통신 기록을 저장하는 기능을 제공합니다.
 */
public interface RecordCommunityServiceCommunicationHistoryUseCase {
    /**
     * @param command 서비스 간 통신 기록 커맨드
     * @return 서비스 간 통신 기록 {@link CommunityServiceCommunicationHistory}
     * @Date 2026-01-31
     * @Author 성효빈
     * @Description 서비스 간 통신 기록을 저장합니다.
     */
    CommunityServiceCommunicationHistory record(CommunityServiceCommunicationHistoryCommand command);
}
