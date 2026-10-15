package com.personal.marketnote.notification.domain.aligo;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class AligoBrandTalkTemplateCreateState {

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
}
