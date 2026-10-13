package com.personal.marketnote.notification.adapter.in.web.aligo.response;

import com.personal.marketnote.notification.domain.aligo.AligoMessageStatus;
import com.personal.marketnote.notification.domain.aligo.AligoMessageType;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoMessageListResult;

import java.time.LocalDateTime;
import java.util.List;

public record GetAligoMessageListResponse(
        List<MessageSummary> messages,
        long totalElements,
        int totalPages,
        int currentPage
) {

    public record MessageSummary(
            Long id,
            String templateCode,
            String sender,
            AligoMessageType messageType,
            AligoMessageStatus messageStatus,
            String aligoMid,
            int successCount,
            int failCount,
            boolean failoverUsed,
            LocalDateTime requestedAt,
            LocalDateTime sentAt,
            LocalDateTime createdAt
    ) {
    }

    public static GetAligoMessageListResponse from(GetAligoMessageListResult result) {
        List<MessageSummary> summaries = result.messages().stream()
                .map(s -> new MessageSummary(
                        s.id(), s.templateCode(), s.sender(), s.messageType(),
                        s.messageStatus(), s.aligoMid(), s.successCount(), s.failCount(),
                        s.failoverUsed(), s.requestedAt(), s.sentAt(), s.createdAt()
                ))
                .toList();
        return new GetAligoMessageListResponse(
                summaries, result.totalElements(), result.totalPages(), result.currentPage()
        );
    }
}
