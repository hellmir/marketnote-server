package com.personal.marketnote.notification.adapter.in.web.aligo.response;

import com.personal.marketnote.notification.port.in.result.aligo.SendBrandTalkResult;

public record SendBrandTalkResponse(
        Long messageId,
        String aligoMid,
        int successCount,
        int failCount,
        String costType,
        int unitCost,
        int totalCost
) {

    public static SendBrandTalkResponse from(SendBrandTalkResult result) {
        return new SendBrandTalkResponse(
                result.messageId(), result.aligoMid(),
                result.successCount(), result.failCount(),
                result.costType(), result.unitCost(), result.totalCost()
        );
    }
}
