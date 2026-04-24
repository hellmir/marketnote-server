package com.personal.marketnote.user.port.in.command.remotearea;

public record RegisterRemoteAreaCommand(
        String province,
        String district,
        String village,
        String subarea
) {
}
