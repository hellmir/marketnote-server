package com.personal.marketnote.notification.port.in.result.aligo;

import java.util.List;

public record GetAligoTemplateListResult(
        List<AligoTemplateResult> templates
) {

    public record AligoTemplateResult(
            Long id,
            String templateCode,
            String templateName,
            String templateType,
            String inspStatus,
            String templateStatus,
            String comments
    ) {
    }
}
