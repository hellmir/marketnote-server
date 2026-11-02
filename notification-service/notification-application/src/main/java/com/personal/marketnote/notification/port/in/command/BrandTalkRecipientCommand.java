package com.personal.marketnote.notification.port.in.command;

import java.util.Map;

public record BrandTalkRecipientCommand(String receiver, Map<String, String> variables) {
}
