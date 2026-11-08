package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AligoBrandTalkTemplateSnapshotState {

    private final Long id;
    private final String senderKey;
    private final String templateCode;
    private final String templateName;
    private final BrandTalkTemplateType templateType;
    private final String templateContent;
    private final boolean adult;
    private final String imageUrl;
    private final String imageLink;
    private final List<BrandTalkButton> buttons;
    private final BrandTalkCoupon coupon;
    private final String templateHeader;
    private final BrandTalkWideItem mainWideItem;
    private final List<BrandTalkWideItem> subWideItems;
    private final String carouselHeader;
    private final List<BrandTalkCarouselItem> carouselItems;
    private final List<BrandTalkCarouselCommerce> carouselCommerces;
    private final BrandTalkCarouselTail carouselTail;
    private final String videoUrl;
    private final String videoThumbnailUrl;
    private final BrandTalkTemplateStatus templateStatus;
    private final BrandTalkCommerceInfo commerceInfo;
    private final EntityStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
}
