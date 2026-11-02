package com.personal.marketnote.notification.port.in.result.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoMessageStatus;
import com.personal.marketnote.notification.domain.aligo.AligoMessageType;

import java.time.LocalDateTime;
import java.util.List;

public record GetAligoMessageListResult(
        List<AligoMessageSummary> messages,
        long totalElements,
        int totalPages,
        int currentPage
) {

    public record AligoMessageSummary(
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
}
