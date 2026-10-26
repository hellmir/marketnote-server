package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class AligoBrandTalkTemplateCreateState {

    private static final int SUB_WIDE_ITEMS_MIN_SIZE = 2;
    private static final int SUB_WIDE_ITEMS_MAX_SIZE = 3;

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
    private BrandTalkTemplateStatus templateStatus;
    private String videoUrl;
    private String videoThumbnailUrl;
    private BrandTalkCommerceInfo commerceInfo;

    public boolean hasInvalidSubWideItemsSize() {
        if (FormatValidator.hasNoValue(subWideItems)) {
            return false;
        }
        return subWideItems.size() < SUB_WIDE_ITEMS_MIN_SIZE || subWideItems.size() > SUB_WIDE_ITEMS_MAX_SIZE;
    }
}
