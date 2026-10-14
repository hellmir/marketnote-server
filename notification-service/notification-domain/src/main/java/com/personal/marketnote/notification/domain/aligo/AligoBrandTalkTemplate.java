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
    private String templateHeader;
    private BrandTalkWideItem mainWideItem;
    private List<BrandTalkWideItem> subWideItems;
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
                .templateHeader(state.getTemplateHeader())
                .mainWideItem(state.getMainWideItem())
                .subWideItems(state.getSubWideItems() != null ? state.getSubWideItems() : List.of())
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
                .templateHeader(state.getTemplateHeader())
                .mainWideItem(state.getMainWideItem())
                .subWideItems(state.getSubWideItems() != null ? state.getSubWideItems() : List.of())
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

    public boolean isWideItemList() {
        return templateType.isWideItemList();
    }

    private static final int MAX_TEMPLATE_HEADER_LENGTH = 20;

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
        if (state.getTemplateType().isWideItemList()) {
            validateWideItemList(state);
        }
    }

    private static void validateWideItemList(AligoBrandTalkTemplateCreateState state) {
        if (FormatValidator.hasValue(state.getTemplateHeader()) && state.getTemplateHeader().length() > MAX_TEMPLATE_HEADER_LENGTH) {
            throw new InvalidBrandTalkTemplateException("templateHeader는 최대 " + MAX_TEMPLATE_HEADER_LENGTH + "자까지 입력할 수 있습니다.");
        }
        if (FormatValidator.hasValue(state.getTemplateHeader()) && state.getTemplateHeader().contains("\n")) {
            throw new InvalidBrandTalkTemplateException("templateHeader에 줄바꿈을 포함할 수 없습니다.");
        }
        if (state.getSubWideItems() != null && (state.getSubWideItems().size() < 2 || state.getSubWideItems().size() > 3)) {
            throw new InvalidBrandTalkTemplateException("WIDE_ITEM_LIST의 subWideItems는 최소 2개, 최대 3개입니다.");
        }
    }
}
