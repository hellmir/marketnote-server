package com.personal.marketnote.notification.port.in.result.aligo;

import java.util.List;

public record GetAligoTemplateDetailResult(
        Long id,
        String senderKey,
        String templateCode,
        String templateName,
        String templateContent,
        String templateTitle,
        String templateSubtitle,
        String templateType,
        String templateEmType,
        boolean templateSecure,
        boolean templateAdvert,
        String imageUrl,
        String imageName,
        String inspStatus,
        String templateStatus,
        String comments,
        List<ButtonResult> buttons
) {

    public record ButtonResult(
            int ordering,
            String name,
            String linkType,
            String linkMo,
            String linkPc,
            String linkIos,
            String linkAnd
    ) {
    }
}
