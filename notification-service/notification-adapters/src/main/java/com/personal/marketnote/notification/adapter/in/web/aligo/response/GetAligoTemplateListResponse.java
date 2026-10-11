package com.personal.marketnote.notification.adapter.in.web.aligo.response;

import com.personal.marketnote.notification.port.in.result.aligo.GetAligoTemplateListResult;

import java.util.List;

public record GetAligoTemplateListResponse(
        List<AligoTemplateResponse> templates
) {

    public record AligoTemplateResponse(
            Long id,
            String templateCode,
            String templateName,
            String templateType,
            String inspStatus,
            String templateStatus,
            String comments
    ) {
    }

    public static GetAligoTemplateListResponse from(GetAligoTemplateListResult result) {
        List<AligoTemplateResponse> templates = result.templates().stream()
                .map(t -> new AligoTemplateResponse(
                        t.id(), t.templateCode(), t.templateName(),
                        t.templateType(), t.inspStatus(), t.templateStatus(), t.comments()
                ))
                .toList();
        return new GetAligoTemplateListResponse(templates);
    }
}
