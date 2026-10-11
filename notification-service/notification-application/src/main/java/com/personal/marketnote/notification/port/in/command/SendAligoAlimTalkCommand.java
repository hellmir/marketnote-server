package com.personal.marketnote.notification.port.in.command;

import java.util.List;
import java.util.Map;

public record SendAligoAlimTalkCommand(
        String templateCode,
        String sender,
        Map<String, String> variables,
        List<AligoAlimTalkRecipientCommand> recipients
) {
}
