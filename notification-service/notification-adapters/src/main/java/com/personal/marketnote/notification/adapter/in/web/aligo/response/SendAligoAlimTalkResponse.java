package com.personal.marketnote.notification.adapter.in.web.aligo.response;

import com.personal.marketnote.notification.port.in.result.aligo.SendAligoAlimTalkResult;

public record SendAligoAlimTalkResponse(Long messageId, String aligoMid, int successCount, int failCount) {

    public static SendAligoAlimTalkResponse from(SendAligoAlimTalkResult result) {
        return new SendAligoAlimTalkResponse(
                result.messageId(), result.aligoMid(), result.successCount(), result.failCount()
        );
    }
}
