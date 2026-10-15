package com.personal.marketnote.notification.port.in.command;

import java.util.List;

public record SendBrandTalkCommand(
        String senderKey,
        String sender,
        String templateCode,
        String kakaoTarget,
        boolean advertYn,
        List<BrandTalkRecipientCommand> recipients,
        BrandTalkFailoverCommand failover
) {
}
