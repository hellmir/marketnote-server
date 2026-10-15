package com.personal.marketnote.notification.adapter.out.persistence.aligo.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import com.personal.marketnote.notification.domain.aligo.BrandTalkTemplateStatus;
import com.personal.marketnote.notification.domain.aligo.BrandTalkTemplateType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "aligo_brand_talk_template")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AligoBrandTalkTemplateJpaEntity extends BaseGeneralEntity {

    @Column(nullable = false)
    private String senderKey;

    @Column(nullable = false, unique = true)
    private String templateCode;

    @Column(nullable = false)
    private String templateName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BrandTalkTemplateType templateType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String templateContent;

    @Column(nullable = false)
    private boolean adult;

    private String imageUrl;

    private String imageLink;

    @Column(columnDefinition = "TEXT")
    private String buttonsJson;

    @Column(columnDefinition = "TEXT")
    private String couponJson;

    private String templateHeader;

    @Column(columnDefinition = "TEXT")
    private String mainWideItemJson;

    @Column(columnDefinition = "TEXT")
    private String subWideItemsJson;

    private String carouselHeader;

    @Column(columnDefinition = "TEXT")
    private String carouselItemsJson;

    @Column(columnDefinition = "TEXT")
    private String carouselCommercesJson;

    @Column(columnDefinition = "TEXT")
    private String carouselTailJson;

    @Column(length = 1024)
    private String videoUrl;

    @Column(length = 1024)
    private String videoThumbnailUrl;

    @Column(columnDefinition = "TEXT")
    private String commerceInfoJson;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BrandTalkTemplateStatus templateStatus;

    public static AligoBrandTalkTemplateJpaEntity create(
            String senderKey, String templateCode, String templateName,
            BrandTalkTemplateType templateType, String templateContent,
            boolean adult, String imageUrl, String imageLink,
            String buttonsJson, String couponJson,
            String templateHeader, String mainWideItemJson, String subWideItemsJson,
            String carouselHeader, String carouselItemsJson,
            String carouselCommercesJson, String carouselTailJson,
            String videoUrl, String videoThumbnailUrl, String commerceInfoJson,
            BrandTalkTemplateStatus templateStatus
    ) {
        AligoBrandTalkTemplateJpaEntity entity = new AligoBrandTalkTemplateJpaEntity();
        entity.senderKey = senderKey;
        entity.templateCode = templateCode;
        entity.templateName = templateName;
        entity.templateType = templateType;
        entity.templateContent = templateContent;
        entity.adult = adult;
        entity.imageUrl = imageUrl;
        entity.imageLink = imageLink;
        entity.buttonsJson = buttonsJson;
        entity.couponJson = couponJson;
        entity.templateHeader = templateHeader;
        entity.mainWideItemJson = mainWideItemJson;
        entity.subWideItemsJson = subWideItemsJson;
        entity.carouselHeader = carouselHeader;
        entity.carouselItemsJson = carouselItemsJson;
        entity.carouselCommercesJson = carouselCommercesJson;
        entity.carouselTailJson = carouselTailJson;
        entity.videoUrl = videoUrl;
        entity.videoThumbnailUrl = videoThumbnailUrl;
        entity.commerceInfoJson = commerceInfoJson;
        entity.templateStatus = templateStatus;
        return entity;
    }

    public void updateTemplateStatus(BrandTalkTemplateStatus templateStatus) {
        this.templateStatus = templateStatus;
    }
}
