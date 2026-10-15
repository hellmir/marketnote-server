package com.personal.marketnote.notification.port.in.result.aligo;

public record SendBrandTalkResult(
        Long messageId,
        String aligoMid,
        int successCount,
        int failCount,
        String costType,
        int unitCost,
        int totalCost
) {
}
