package com.personal.marketnote.notification.adapter.in.web.aligo.response;

import com.personal.marketnote.notification.port.in.result.aligo.GetAligoTemplateDetailResult;

import java.util.List;

public record GetAligoTemplateDetailResponse(
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
        List<ButtonResponse> buttons
) {

    public record ButtonResponse(
            int ordering,
            String name,
            String linkType,
            String linkMo,
            String linkPc,
            String linkIos,
            String linkAnd
    ) {
    }

    public static GetAligoTemplateDetailResponse from(GetAligoTemplateDetailResult result) {
        List<ButtonResponse> buttons = result.buttons().stream()
                .map(b -> new ButtonResponse(
                        b.ordering(), b.name(), b.linkType(),
                        b.linkMo(), b.linkPc(), b.linkIos(), b.linkAnd()
                ))
                .toList();

        return new GetAligoTemplateDetailResponse(
                result.id(), result.senderKey(),
                result.templateCode(), result.templateName(),
                result.templateContent(), result.templateTitle(),
                result.templateSubtitle(), result.templateType(),
                result.templateEmType(), result.templateSecure(),
                result.templateAdvert(), result.imageUrl(),
                result.imageName(), result.inspStatus(),
                result.templateStatus(), result.comments(), buttons
        );
    }
}
