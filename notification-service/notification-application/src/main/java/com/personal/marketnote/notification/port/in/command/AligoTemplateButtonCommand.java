package com.personal.marketnote.notification.port.in.command;

public record AligoTemplateButtonCommand(
        int ordering,
        String name,
        String linkType,
        String linkMo,
        String linkPc,
        String linkIos,
        String linkAnd
) {
}
