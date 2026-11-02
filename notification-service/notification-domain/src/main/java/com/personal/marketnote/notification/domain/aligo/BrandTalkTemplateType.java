package com.personal.marketnote.notification.domain.aligo;

public enum BrandTalkTemplateType {

    TEXT("텍스트"),
    IMAGE("이미지"),
    WIDE("와이드"),
    WIDE_ITEM_LIST("와이드 아이템 리스트"),
    CAROUSEL_FEED("캐러셀 피드"),
    PREMIUM_VIDEO("프리미엄 동영상"),
    COMMERCE("커머스"),
    CAROUSEL_COMMERCE("캐러셀 커머스");

    private final String description;

    BrandTalkTemplateType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCarousel() {
        return this == CAROUSEL_FEED || this == CAROUSEL_COMMERCE;
    }

    public boolean isPremiumVideo() {
        return this == PREMIUM_VIDEO;
    }

    public boolean isWideItemList() {
        return this == WIDE_ITEM_LIST;
    }

    public boolean isCarouselCommerce() {
        return this == CAROUSEL_COMMERCE;
    }

    public boolean isCommerce() {
        return this == COMMERCE;
    }

    public static BrandTalkTemplateType from(String code) {
        for (BrandTalkTemplateType type : values()) {
            if (type.name().equals(code)) {
                return type;
            }
        }
        throw new InvalidBrandTalkTemplateTypeException(code);
    }
}
