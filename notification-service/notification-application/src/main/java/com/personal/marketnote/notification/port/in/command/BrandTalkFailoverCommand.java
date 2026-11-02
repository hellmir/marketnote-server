package com.personal.marketnote.notification.port.in.command;

import java.util.List;

public record BrandTalkFailoverCommand(
        boolean enabled,
        String subject,
        String failoverType,
        List<byte[]> mmsImages,
        List<String> mmsImageFileNames
) {
}
