package com.personal.marketnote.notification.port.in.command;

public record AligoFailoverCommand(boolean enabled, String subject, String body) {
}
