package com.personal.marketnote.notification.adapter.in.web.aligo.response;

import com.personal.marketnote.notification.port.in.result.aligo.RegisterAligoTemplateResult;

public record RegisterAligoTemplateResponse(
        Long id,
        String templateCode,
        String templateName
) {

    public static RegisterAligoTemplateResponse from(RegisterAligoTemplateResult result) {
        return new RegisterAligoTemplateResponse(result.id(), result.templateCode(), result.templateName());
    }
}
