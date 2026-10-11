package com.personal.marketnote.notification.adapter.in.web.aligo.response;

import com.personal.marketnote.notification.port.in.result.aligo.UpdateAligoTemplateResult;

public record UpdateAligoTemplateResponse(
        Long id,
        String templateCode,
        String templateName
) {

    public static UpdateAligoTemplateResponse from(UpdateAligoTemplateResult result) {
        return new UpdateAligoTemplateResponse(result.id(), result.templateCode(), result.templateName());
    }
}
