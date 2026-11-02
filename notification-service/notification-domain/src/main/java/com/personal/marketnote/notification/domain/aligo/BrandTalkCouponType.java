package com.personal.marketnote.notification.domain.aligo;

public enum BrandTalkCouponType {

    TYPE1("할인금액"),
    TYPE2("할인율"),
    TYPE3("배송비"),
    TYPE4("무료쿠폰"),
    TYPE5("UP쿠폰");

    private final String title;

    BrandTalkCouponType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static BrandTalkCouponType from(String code) {
        for (BrandTalkCouponType type : values()) {
            if (type.name().equals(code)) {
                return type;
            }
        }
        throw new InvalidBrandTalkCouponTypeException(code);
    }
}
