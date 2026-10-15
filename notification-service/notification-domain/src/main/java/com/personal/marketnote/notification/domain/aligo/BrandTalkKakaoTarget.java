package com.personal.marketnote.notification.domain.aligo;

public enum BrandTalkKakaoTarget {

    M("마수동 카톡수신동의"),
    N("마수동+채널친구"),
    I("요청대상∩채널친구");

    private final String description;

    BrandTalkKakaoTarget(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static BrandTalkKakaoTarget from(String code) {
        for (BrandTalkKakaoTarget target : values()) {
            if (target.name().equals(code)) {
                return target;
            }
        }
        throw new InvalidBrandTalkKakaoTargetException(code);
    }
}
