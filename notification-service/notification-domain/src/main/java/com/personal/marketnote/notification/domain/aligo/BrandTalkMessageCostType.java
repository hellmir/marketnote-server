package com.personal.marketnote.notification.domain.aligo;

public enum BrandTalkMessageCostType {

    BWF("유료 친구톡 발송"),
    BNF("유료 알림톡 발송");

    private final String description;

    BrandTalkMessageCostType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static BrandTalkMessageCostType from(String apiType) {
        if ("BW".equals(apiType)) {
            return BWF;
        }
        if ("BN".equals(apiType)) {
            return BNF;
        }
        throw new InvalidBrandTalkCostTypeException(apiType);
    }
}
