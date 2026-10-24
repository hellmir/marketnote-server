package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.domain.BaseDomain;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class AligoTemplate extends BaseDomain {

    private Long id;
    private String senderKey;
    private String templateCode;
    private String templateName;
    private String templateContent;
    private String templateTitle;
    private String templateSubtitle;
    private AligoTemplateType templateType;
    private AligoTemplateEmType templateEmType;
    private boolean templateSecure;
    private boolean templateAdvert;
    private String imageUrl;
    private String imageName;
    private String inspStatus;
    private AligoTemplateStatus templateStatus;
    private String comments;
    private List<AligoTemplateButton> buttons;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static AligoTemplate from(AligoTemplateCreateState state) {
        validate(state);
        AligoTemplate template = AligoTemplate.builder()
                .senderKey(state.getSenderKey())
                .templateCode(state.getTemplateCode())
                .templateName(state.getTemplateName())
                .templateContent(state.getTemplateContent())
                .templateTitle(state.getTemplateTitle())
                .templateSubtitle(state.getTemplateSubtitle())
                .templateType(state.getTemplateType())
                .templateEmType(state.getTemplateEmType())
                .templateSecure(state.isTemplateSecure())
                .templateAdvert(state.isTemplateAdvert())
                .imageUrl(state.getImageUrl())
                .imageName(state.getImageName())
                .inspStatus(AligoTemplateInspectionStatus.REG.name())
                .templateStatus(AligoTemplateStatus.R)
                .buttons(FormatValidator.hasValue(state.getButtons()) ? state.getButtons() : List.of())
                .build();
        template.activate();
        return template;
    }

    public static AligoTemplate from(AligoTemplateSnapshotState state) {
        AligoTemplate template = AligoTemplate.builder()
                .id(state.getId())
                .senderKey(state.getSenderKey())
                .templateCode(state.getTemplateCode())
                .templateName(state.getTemplateName())
                .templateContent(state.getTemplateContent())
                .templateTitle(state.getTemplateTitle())
                .templateSubtitle(state.getTemplateSubtitle())
                .templateType(state.getTemplateType())
                .templateEmType(state.getTemplateEmType())
                .templateSecure(state.isTemplateSecure())
                .templateAdvert(state.isTemplateAdvert())
                .imageUrl(state.getImageUrl())
                .imageName(state.getImageName())
                .inspStatus(state.getInspStatus())
                .templateStatus(state.getTemplateStatus())
                .comments(state.getComments())
                .buttons(FormatValidator.hasValue(state.getButtons()) ? state.getButtons() : List.of())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
        template.status = state.getStatus();
        return template;
    }

    public boolean canModify() {
        if (FormatValidator.hasNoValue(inspStatus)) {
            return false;
        }
        return AligoTemplateInspectionStatus.from(inspStatus).canModify();
    }

    public boolean canDelete() {
        return canModify();
    }

    public boolean canRequestInspection() {
        if (FormatValidator.hasNoValue(inspStatus)) {
            return false;
        }
        return AligoTemplateInspectionStatus.from(inspStatus).canRequestInspection();
    }

    public void markAsRequested() {
        AligoTemplateInspectionStatus current = AligoTemplateInspectionStatus.from(inspStatus);
        if (!current.canTransitionTo(AligoTemplateInspectionStatus.REQ)) {
            throw new InvalidAligoTemplateTransitionException(inspStatus, AligoTemplateInspectionStatus.REQ.name());
        }
        inspStatus = AligoTemplateInspectionStatus.REQ.name();
    }

    public void markAsApproved() {
        AligoTemplateInspectionStatus current = AligoTemplateInspectionStatus.from(inspStatus);
        if (!current.canTransitionTo(AligoTemplateInspectionStatus.APR)) {
            throw new InvalidAligoTemplateTransitionException(inspStatus, AligoTemplateInspectionStatus.APR.name());
        }
        inspStatus = AligoTemplateInspectionStatus.APR.name();
    }

    public void markAsRejected(String comments) {
        AligoTemplateInspectionStatus current = AligoTemplateInspectionStatus.from(inspStatus);
        if (!current.canTransitionTo(AligoTemplateInspectionStatus.REJ)) {
            throw new InvalidAligoTemplateTransitionException(inspStatus, AligoTemplateInspectionStatus.REJ.name());
        }
        inspStatus = AligoTemplateInspectionStatus.REJ.name();
        this.comments = comments;
    }

    public void resetToRegistered() {
        AligoTemplateInspectionStatus current = AligoTemplateInspectionStatus.from(inspStatus);
        if (!current.canTransitionTo(AligoTemplateInspectionStatus.REG)) {
            throw new InvalidAligoTemplateTransitionException(inspStatus, AligoTemplateInspectionStatus.REG.name());
        }
        inspStatus = AligoTemplateInspectionStatus.REG.name();
    }

    public void updateContent(
            String templateName, String templateContent,
            String templateTitle, String templateSubtitle,
            List<AligoTemplateButton> buttons
    ) {
        if (!canModify()) {
            throw new InvalidAligoTemplateModificationException(templateCode);
        }
        this.templateName = templateName;
        this.templateContent = templateContent;
        this.templateTitle = templateTitle;
        this.templateSubtitle = templateSubtitle;
        this.buttons = FormatValidator.hasValue(buttons) ? buttons : List.of();
    }

    public void updateInspStatus(String inspStatus) {
        this.inspStatus = inspStatus;
    }

    public void updateTemplateStatus(AligoTemplateStatus templateStatus) {
        this.templateStatus = templateStatus;
    }

    public void validateDeletion() {
        if (!canDelete()) {
            throw new InvalidAligoTemplateDeletionException(templateCode);
        }
    }

    private static void validate(AligoTemplateCreateState state) {
        if (FormatValidator.hasNoValue(state.getSenderKey())) {
            throw new InvalidAligoTemplateException("senderKey는 필수입니다.");
        }
        if (FormatValidator.hasNoValue(state.getTemplateCode())) {
            throw new InvalidAligoTemplateException("templateCode는 필수입니다.");
        }
        if (FormatValidator.hasNoValue(state.getTemplateName())) {
            throw new InvalidAligoTemplateException("templateName은 필수입니다.");
        }
        if (FormatValidator.hasNoValue(state.getTemplateContent())) {
            throw new InvalidAligoTemplateException("templateContent는 필수입니다.");
        }
        if (FormatValidator.hasValue(state.getButtons()) && state.getButtons().size() > 5) {
            throw new InvalidAligoTemplateException("버튼은 최대 5개까지 등록할 수 있습니다.");
        }
    }
}
