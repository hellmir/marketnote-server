package com.personal.marketnote.user.port.in.usecase.servicecommunication;

import com.personal.marketnote.user.domain.servicecommunication.UserServiceCommunicationHistory;
import com.personal.marketnote.user.port.in.command.servicecommunication.UserServiceCommunicationHistoryCommand;

/**
 * 서비스 간 통신 기록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-31
 * @Description 서비스 간 통신 기록 기능을 제공합니다.
 */
public interface RecordUserServiceCommunicationHistoryUseCase {
    /**
     * @param command 서비스 간 통신 기록 커맨드
     * @return 저장된 서비스 간 통신 기록 {@link UserServiceCommunicationHistory}
     * @Date 2026-01-31
     * @Author 성효빈
     * @Description 서비스 간 통신 기록을 저장합니다.
     */
    UserServiceCommunicationHistory record(UserServiceCommunicationHistoryCommand command);
}
