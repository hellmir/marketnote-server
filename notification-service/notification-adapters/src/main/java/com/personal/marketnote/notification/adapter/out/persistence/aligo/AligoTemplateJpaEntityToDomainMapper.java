package com.personal.marketnote.notification.adapter.out.persistence.aligo;

import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoTemplateButtonJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoTemplateJpaEntity;
import com.personal.marketnote.notification.domain.aligo.AligoTemplate;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateButton;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateSnapshotState;

import java.util.List;

public final class AligoTemplateJpaEntityToDomainMapper {

    private AligoTemplateJpaEntityToDomainMapper() {
    }

    public static AligoTemplate mapToDomain(AligoTemplateJpaEntity entity) {
        List<AligoTemplateButton> buttons = entity.getButtons().stream()
                .map(AligoTemplateJpaEntityToDomainMapper::mapButton)
                .toList();

        return AligoTemplate.from(
                AligoTemplateSnapshotState.builder()
                        .id(entity.getId())
                        .senderKey(entity.getSenderKey())
                        .templateCode(entity.getTemplateCode())
                        .templateName(entity.getTemplateName())
                        .templateContent(entity.getTemplateContent())
                        .templateTitle(entity.getTemplateTitle())
                        .templateSubtitle(entity.getTemplateSubtitle())
                        .templateType(entity.getTemplateType())
                        .templateEmType(entity.getTemplateEmType())
                        .templateSecure(entity.isTemplateSecure())
                        .templateAdvert(entity.isTemplateAdvert())
                        .imageUrl(entity.getImageUrl())
                        .imageName(entity.getImageName())
                        .inspStatus(entity.getInspStatus())
                        .templateStatus(entity.getTemplateStatus())
                        .comments(entity.getComments())
                        .buttons(buttons)
                        .status(entity.getStatus())
                        .createdAt(entity.getCreatedAt())
                        .modifiedAt(entity.getModifiedAt())
                        .build()
        );
    }

    private static AligoTemplateButton mapButton(AligoTemplateButtonJpaEntity entity) {
        return AligoTemplateButton.builder()
                .ordering(entity.getOrdering())
                .name(entity.getName())
                .linkType(entity.getLinkType())
                .linkMo(entity.getLinkMo())
                .linkPc(entity.getLinkPc())
                .linkIos(entity.getLinkIos())
                .linkAnd(entity.getLinkAnd())
                .build();
    }
}
