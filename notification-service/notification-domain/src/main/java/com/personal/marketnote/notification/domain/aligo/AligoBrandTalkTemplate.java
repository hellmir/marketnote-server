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
public class AligoBrandTalkTemplate extends BaseDomain {

    private Long id;
    private String senderKey;
    private String templateCode;
    private String templateName;
    private BrandTalkTemplateType templateType;
    private String templateContent;
    private boolean adult;
    private String imageUrl;
    private String imageLink;
    private List<BrandTalkButton> buttons;
    private BrandTalkCoupon coupon;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static AligoBrandTalkTemplate from(AligoBrandTalkTemplateCreateState state) {
        validate(state);
        AligoBrandTalkTemplate template = AligoBrandTalkTemplate.builder()
                .senderKey(state.getSenderKey())
                .templateCode(state.getTemplateCode())
                .templateName(state.getTemplateName())
                .templateType(state.getTemplateType())
                .templateContent(state.getTemplateContent())
                .adult(state.isAdult())
                .imageUrl(state.getImageUrl())
                .imageLink(state.getImageLink())
                .buttons(state.getButtons() != null ? state.getButtons() : List.of())
                .coupon(state.getCoupon())
                .build();
        template.activate();
        return template;
    }

    public static AligoBrandTalkTemplate from(AligoBrandTalkTemplateSnapshotState state) {
        AligoBrandTalkTemplate template = AligoBrandTalkTemplate.builder()
                .id(state.getId())
                .senderKey(state.getSenderKey())
                .templateCode(state.getTemplateCode())
                .templateName(state.getTemplateName())
                .templateType(state.getTemplateType())
                .templateContent(state.getTemplateContent())
                .adult(state.isAdult())
                .imageUrl(state.getImageUrl())
                .imageLink(state.getImageLink())
                .buttons(state.getButtons() != null ? state.getButtons() : List.of())
                .coupon(state.getCoupon())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
        template.status = state.getStatus();
        return template;
    }

    public boolean requiresImage() {
        return templateType == BrandTalkTemplateType.IMAGE
                || templateType == BrandTalkTemplateType.WIDE;
    }

    private static void validate(AligoBrandTalkTemplateCreateState state) {
        if (FormatValidator.hasNoValue(state.getSenderKey())) {
            throw new InvalidBrandTalkTemplateException("senderKey는 필수입니다.");
        }
        if (FormatValidator.hasNoValue(state.getTemplateCode())) {
            throw new InvalidBrandTalkTemplateException("templateCode는 필수입니다.");
        }
        if (FormatValidator.hasNoValue(state.getTemplateName())) {
            throw new InvalidBrandTalkTemplateException("templateName은 필수입니다.");
        }
        if (FormatValidator.hasNoValue(state.getTemplateContent())) {
            throw new InvalidBrandTalkTemplateException("templateContent는 필수입니다.");
        }
    }
}
