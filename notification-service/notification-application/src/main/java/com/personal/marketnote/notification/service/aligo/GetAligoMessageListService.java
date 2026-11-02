package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.aligo.AligoMessage;
import com.personal.marketnote.notification.port.in.command.GetAligoMessageListCommand;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoMessageListResult;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoMessageListResult.AligoMessageSummary;
import com.personal.marketnote.notification.port.in.usecase.aligo.GetAligoMessageListUseCase;
import com.personal.marketnote.notification.port.out.aligo.FindAligoMessageListPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class GetAligoMessageListService implements GetAligoMessageListUseCase {

    private final FindAligoMessageListPort findAligoMessageListPort;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public GetAligoMessageListResult getMessageList(GetAligoMessageListCommand command) {
        List<AligoMessage> messages = findAligoMessageListPort.findMessages(
                command.startDate(), command.endDate(), command.page(), command.limit()
        );
        long totalElements = findAligoMessageListPort.countMessages(command.startDate(), command.endDate());
        int totalPages = calculateTotalPages(totalElements, command.limit());

        List<AligoMessageSummary> summaries = messages.stream()
                .map(this::toSummary)
                .toList();

        return new GetAligoMessageListResult(summaries, totalElements, totalPages, command.page());
    }

    private AligoMessageSummary toSummary(AligoMessage message) {
        return new AligoMessageSummary(
                message.getId(),
                message.getTemplateCode(),
                message.getSender(),
                message.getMessageType(),
                message.getMessageStatus(),
                message.getAligoMid(),
                message.getSuccessCount(),
                message.getFailCount(),
                message.isFailoverUsed(),
                message.getRequestedAt(),
                message.getSentAt(),
                message.getCreatedAt()
        );
    }

    private int calculateTotalPages(long totalElements, int limit) {
        if (totalElements == 0 || limit <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / limit);
    }
}
