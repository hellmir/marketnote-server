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

    private static final String INSP_STATUS_REG = "REG";
    private static final String INSP_STATUS_REJ = "REJ";

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
                .inspStatus(INSP_STATUS_REG)
                .templateStatus(AligoTemplateStatus.R)
                .buttons(state.getButtons() != null ? state.getButtons() : List.of())
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
                .buttons(state.getButtons() != null ? state.getButtons() : List.of())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
        template.status = state.getStatus();
        return template;
    }

    public boolean canModify() {
        return FormatValidator.hasValue(inspStatus)
                && (inspStatus.equals(INSP_STATUS_REG) || inspStatus.equals(INSP_STATUS_REJ));
    }

    public boolean canDelete() {
        return canModify();
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
        this.buttons = buttons != null ? buttons : List.of();
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
        if (state.getButtons() != null && state.getButtons().size() > 5) {
            throw new InvalidAligoTemplateException("버튼은 최대 5개까지 등록할 수 있습니다.");
        }
    }
}
