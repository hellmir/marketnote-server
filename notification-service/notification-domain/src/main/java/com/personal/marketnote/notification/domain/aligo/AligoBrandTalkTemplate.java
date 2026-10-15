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
    private String carouselHeader;
    private List<BrandTalkCarouselItem> carouselItems;
    private List<BrandTalkCarouselCommerce> carouselCommerces;
    private BrandTalkCarouselTail carouselTail;
    private String videoUrl;
    private String videoThumbnailUrl;
    private BrandTalkTemplateStatus templateStatus;
    private BrandTalkCommerceInfo commerceInfo;
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
                .carouselHeader(state.getCarouselHeader())
                .carouselItems(state.getCarouselItems() != null ? state.getCarouselItems() : List.of())
                .carouselCommerces(state.getCarouselCommerces() != null ? state.getCarouselCommerces() : List.of())
                .carouselTail(state.getCarouselTail())
                .videoUrl(state.getVideoUrl())
                .videoThumbnailUrl(state.getVideoThumbnailUrl())
                .commerceInfo(state.getCommerceInfo())
                .templateStatus(state.getTemplateStatus())
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
                .carouselHeader(state.getCarouselHeader())
                .carouselItems(state.getCarouselItems() != null ? state.getCarouselItems() : List.of())
                .carouselCommerces(state.getCarouselCommerces() != null ? state.getCarouselCommerces() : List.of())
                .carouselTail(state.getCarouselTail())
                .videoUrl(state.getVideoUrl())
                .videoThumbnailUrl(state.getVideoThumbnailUrl())
                .templateStatus(state.getTemplateStatus())
                .commerceInfo(state.getCommerceInfo())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
        template.status = state.getStatus();
        return template;
    }

    public boolean requiresImage() {
        return templateType == BrandTalkTemplateType.IMAGE
                || templateType == BrandTalkTemplateType.WIDE
                || templateType.isCommerce();
    }

    public boolean isPremiumVideo() {
        return templateType.isPremiumVideo();
    }

    public boolean isWideItemList() {
        return templateType.isWideItemList();
    }

    public boolean isCarousel() {
        return templateType.isCarousel();
    }

    public boolean hasTemplateStatus(BrandTalkTemplateStatus templateStatus) {
        return this.templateStatus == templateStatus;
    }

    public void updateTemplateStatus(BrandTalkTemplateStatus templateStatus) {
        this.templateStatus = templateStatus;
    }

    private static final int MAX_TEMPLATE_HEADER_LENGTH = 20;
    private static final int MAX_CAROUSEL_HEADER_LENGTH = 20;
    private static final int MAX_CAROUSEL_ITEMS_WITH_INTRO = 5;
    private static final int MAX_CAROUSEL_ITEMS_WITHOUT_INTRO = 6;

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
        if (state.getTemplateType().isCarousel()) {
            validateCarousel(state);
        }
        if (state.getTemplateType().isPremiumVideo()) {
            validatePremiumVideo(state);
        }
        if (state.getTemplateType().isCommerce()) {
            validateCommerce(state);
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

    private static void validateCarousel(AligoBrandTalkTemplateCreateState state) {
        if (FormatValidator.hasValue(state.getCarouselHeader())
                && state.getCarouselHeader().length() > MAX_CAROUSEL_HEADER_LENGTH) {
            throw new InvalidBrandTalkTemplateException(
                    "carouselHeader는 최대 " + MAX_CAROUSEL_HEADER_LENGTH + "자까지 입력할 수 있습니다."
            );
        }
        if (state.getCarouselItems() == null || state.getCarouselItems().isEmpty()) {
            throw new InvalidBrandTalkTemplateException("CAROUSEL 타입에는 carouselItems가 필수입니다.");
        }
        boolean hasIntro = FormatValidator.hasValue(state.getCarouselHeader());
        int maxItems = hasIntro ? MAX_CAROUSEL_ITEMS_WITH_INTRO : MAX_CAROUSEL_ITEMS_WITHOUT_INTRO;
        if (state.getCarouselItems().size() > maxItems) {
            String introLabel = hasIntro ? "인트로 사용 시 " : "인트로 미사용 시 ";
            throw new InvalidBrandTalkTemplateException(
                    "CAROUSEL의 carouselItems는 " + introLabel + "최대 " + maxItems + "개입니다."
            );
        }
        if (state.getTemplateType().isCarouselCommerce()) {
            validateCarouselCommerce(state);
        }
    }

    private static void validateCarouselCommerce(AligoBrandTalkTemplateCreateState state) {
        if (state.getCarouselCommerces() == null || state.getCarouselCommerces().isEmpty()) {
            throw new InvalidBrandTalkTemplateException("CAROUSEL_COMMERCE 타입에는 carouselCommerces가 필수입니다.");
        }
    }

    private static void validatePremiumVideo(AligoBrandTalkTemplateCreateState state) {
        if (FormatValidator.hasNoValue(state.getVideoUrl())) {
            throw new InvalidBrandTalkTemplateException("PREMIUM_VIDEO 타입에는 videoUrl이 필수입니다.");
        }
    }

    private static void validateCommerce(AligoBrandTalkTemplateCreateState state) {
        if (state.getCommerceInfo() == null) {
            throw new InvalidBrandTalkTemplateException("COMMERCE 타입에는 commerceInfo가 필수입니다.");
        }
    }
}
