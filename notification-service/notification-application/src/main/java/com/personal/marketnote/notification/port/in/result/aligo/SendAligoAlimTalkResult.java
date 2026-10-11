package com.personal.marketnote.notification.port.in.result.aligo;

public record SendAligoAlimTalkResult(Long messageId, String aligoMid, int successCount, int failCount) {
}
