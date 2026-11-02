package com.personal.marketnote.notification.adapter.in.web.aligo.response;

import com.personal.marketnote.notification.port.in.result.aligo.RegisterBrandTalkTemplateResult;

public record RegisterBrandTalkTemplateResponse(
        Long id,
        String templateCode,
        String templateName,
        String templateType
) {
    public static RegisterBrandTalkTemplateResponse from(RegisterBrandTalkTemplateResult result) {
        return new RegisterBrandTalkTemplateResponse(
                result.id(), result.templateCode(), result.templateName(), result.templateType()
        );
    }
}
