package com.personal.marketnote.notification.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkTemplateCreateState;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkTemplateSnapshotState;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCommerceInfo;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCommerceInfoCreateState;
import com.personal.marketnote.notification.domain.aligo.BrandTalkTemplateStatus;
import com.personal.marketnote.notification.domain.aligo.BrandTalkTemplateType;
import com.personal.marketnote.notification.port.in.command.BrandTalkCommerceInfoCommand;
import com.personal.marketnote.notification.port.in.command.RegisterBrandTalkTemplateCommand;
import com.personal.marketnote.notification.port.in.result.aligo.GetBrandTalkTemplateListResult;

public class AligoBrandTalkTemplateCommandToStateMapper {

    private AligoBrandTalkTemplateCommandToStateMapper() {
    }

    public static AligoBrandTalkTemplateCreateState mapToCreateState(RegisterBrandTalkTemplateCommand command) {
        return AligoBrandTalkTemplateCreateState.builder()
                .senderKey(command.senderKey())
                .templateCode(command.templateCode())
                .templateName(command.templateName())
                .templateType(BrandTalkTemplateType.from(command.templateType()))
                .templateContent(command.templateContent())
                .adult(command.adult())
                .imageUrl(null)
                .imageLink(command.imageLink())
                .buttons(BrandTalkButtonCommandToStateMapper.mapButtons(command.buttons()))
                .coupon(BrandTalkCouponCommandToStateMapper.mapCoupon(command.coupon()))
                .templateHeader(command.templateHeader())
                .mainWideItem(BrandTalkWideItemCommandToStateMapper.mapWideItem(command.mainWideItem()))
                .subWideItems(BrandTalkWideItemCommandToStateMapper.mapWideItems(command.subWideItems()))
                .carouselHeader(command.carouselHeader())
                .carouselItems(BrandTalkCarouselCommandToStateMapper.mapCarouselItems(command.carouselItems()))
                .carouselCommerces(
                        BrandTalkCarouselCommandToStateMapper.mapCarouselCommerces(command.carouselCommerces())
                )
                .carouselTail(BrandTalkCarouselCommandToStateMapper.mapCarouselTail(command.carouselTail()))
                .videoUrl(command.videoUrl())
                .videoThumbnailUrl(command.videoThumbnailUrl())
                .commerceInfo(mapCommerceInfo(command.commerceInfo()))
                .build();
    }

    public static AligoBrandTalkTemplateSnapshotState mapToSnapshotState(
            String senderKey, GetBrandTalkTemplateListResult.ExternalTemplateItem external
    ) {
        return AligoBrandTalkTemplateSnapshotState.builder()
                .senderKey(senderKey)
                .templateCode(external.templateCode())
                .templateName(external.templateName())
                .templateType(BrandTalkTemplateType.from(external.templateType()))
                .templateContent("")
                .templateStatus(BrandTalkTemplateStatus.from(external.templateStatus()))
                .build();
    }

    private static BrandTalkCommerceInfo mapCommerceInfo(BrandTalkCommerceInfoCommand command) {
        if (FormatValidator.hasNoValue(command)) {
            return null;
        }
        return BrandTalkCommerceInfo.from(
                BrandTalkCommerceInfoCreateState.builder()
                        .title(command.title())
                        .regularPrice(command.regularPrice())
                        .discountPrice(command.discountPrice())
                        .discountRate(command.discountRate())
                        .discountFixed(command.discountFixed())
                        .build()
        );
    }
}
