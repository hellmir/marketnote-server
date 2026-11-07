package com.personal.marketnote.notification.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkTemplateCreateState;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkTemplateSnapshotState;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateButtonLinkType;
import com.personal.marketnote.notification.domain.aligo.BrandTalkButton;
import com.personal.marketnote.notification.domain.aligo.BrandTalkButtonCreateState;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCarouselCommerce;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCarouselCommerceCreateState;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCarouselItem;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCarouselItemCreateState;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCarouselTail;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCommerceInfo;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCommerceInfoCreateState;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCoupon;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCouponCreateState;
import com.personal.marketnote.notification.domain.aligo.BrandTalkTemplateStatus;
import com.personal.marketnote.notification.domain.aligo.BrandTalkTemplateType;
import com.personal.marketnote.notification.domain.aligo.BrandTalkWideItem;
import com.personal.marketnote.notification.domain.aligo.BrandTalkWideItemCreateState;
import com.personal.marketnote.notification.port.in.command.BrandTalkButtonCommand;
import com.personal.marketnote.notification.port.in.command.BrandTalkCarouselCommerceCommand;
import com.personal.marketnote.notification.port.in.command.BrandTalkCarouselItemCommand;
import com.personal.marketnote.notification.port.in.command.BrandTalkCarouselTailCommand;
import com.personal.marketnote.notification.port.in.command.BrandTalkCommerceInfoCommand;
import com.personal.marketnote.notification.port.in.command.BrandTalkCouponCommand;
import com.personal.marketnote.notification.port.in.command.BrandTalkWideItemCommand;
import com.personal.marketnote.notification.port.in.command.RegisterBrandTalkTemplateCommand;
import com.personal.marketnote.notification.port.in.result.aligo.GetBrandTalkTemplateListResult;

import java.util.List;

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
                .buttons(mapButtons(command.buttons()))
                .coupon(mapCoupon(command.coupon()))
                .templateHeader(command.templateHeader())
                .mainWideItem(mapWideItem(command.mainWideItem()))
                .subWideItems(mapWideItems(command.subWideItems()))
                .carouselHeader(command.carouselHeader())
                .carouselItems(mapCarouselItems(command.carouselItems()))
                .carouselCommerces(mapCarouselCommerces(command.carouselCommerces()))
                .carouselTail(mapCarouselTail(command.carouselTail()))
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

    public static List<BrandTalkButton> mapButtons(List<BrandTalkButtonCommand> buttonCommands) {
        if (FormatValidator.hasNoValue(buttonCommands)) {
            return List.of();
        }
        return buttonCommands.stream()
                .map(AligoBrandTalkTemplateCommandToStateMapper::mapButton)
                .toList();
    }

    private static BrandTalkButton mapButton(BrandTalkButtonCommand btn) {
        return BrandTalkButton.from(
                BrandTalkButtonCreateState.builder()
                        .name(btn.name())
                        .linkType(AligoTemplateButtonLinkType.from(btn.linkType()))
                        .linkM(btn.linkM())
                        .linkP(btn.linkP())
                        .linkI(btn.linkI())
                        .linkA(btn.linkA())
                        .build()
        );
    }

    public static BrandTalkCoupon mapCoupon(BrandTalkCouponCommand couponCommand) {
        if (FormatValidator.hasNoValue(couponCommand)) {
            return null;
        }
        return BrandTalkCoupon.from(
                BrandTalkCouponCreateState.builder()
                        .title(couponCommand.title())
                        .description(couponCommand.description())
                        .linkM(couponCommand.linkM())
                        .linkP(couponCommand.linkP())
                        .linkI(couponCommand.linkI())
                        .linkA(couponCommand.linkA())
                        .build()
        );
    }

    public static BrandTalkWideItem mapWideItem(BrandTalkWideItemCommand command) {
        if (FormatValidator.hasNoValue(command)) {
            return null;
        }
        return BrandTalkWideItem.from(
                BrandTalkWideItemCreateState.builder()
                        .title(command.title())
                        .imageUrl(command.imageUrl())
                        .linkM(command.linkM())
                        .linkP(command.linkP())
                        .linkI(command.linkI())
                        .linkA(command.linkA())
                        .build()
        );
    }

    public static List<BrandTalkWideItem> mapWideItems(List<BrandTalkWideItemCommand> commands) {
        if (FormatValidator.hasNoValue(commands)) {
            return List.of();
        }
        return commands.stream()
                .map(AligoBrandTalkTemplateCommandToStateMapper::mapWideItem)
                .toList();
    }

    public static List<BrandTalkCarouselItem> mapCarouselItems(List<BrandTalkCarouselItemCommand> commands) {
        if (FormatValidator.hasNoValue(commands)) {
            return List.of();
        }
        return commands.stream()
                .map(AligoBrandTalkTemplateCommandToStateMapper::mapCarouselItem)
                .toList();
    }

    private static BrandTalkCarouselItem mapCarouselItem(BrandTalkCarouselItemCommand cmd) {
        return BrandTalkCarouselItem.from(
                BrandTalkCarouselItemCreateState.builder()
                        .header(cmd.header())
                        .content(cmd.content())
                        .imageUrl(null)
                        .buttons(mapButtons(cmd.buttons()))
                        .coupon(mapCoupon(cmd.coupon()))
                        .build()
        );
    }

    public static List<BrandTalkCarouselCommerce> mapCarouselCommerces(List<BrandTalkCarouselCommerceCommand> commands) {
        if (FormatValidator.hasNoValue(commands)) {
            return List.of();
        }
        return commands.stream()
                .map(AligoBrandTalkTemplateCommandToStateMapper::mapCarouselCommerce)
                .toList();
    }

    private static BrandTalkCarouselCommerce mapCarouselCommerce(BrandTalkCarouselCommerceCommand cmd) {
        return BrandTalkCarouselCommerce.from(
                BrandTalkCarouselCommerceCreateState.builder()
                        .title(cmd.title())
                        .regularPrice(cmd.regularPrice())
                        .discountPrice(cmd.discountPrice())
                        .discountRate(cmd.discountRate())
                        .discountFixed(cmd.discountFixed())
                        .build()
        );
    }

    public static BrandTalkCarouselTail mapCarouselTail(BrandTalkCarouselTailCommand command) {
        if (FormatValidator.hasNoValue(command)) {
            return null;
        }
        return BrandTalkCarouselTail.of(command.linkM());
    }

    public static BrandTalkCommerceInfo mapCommerceInfo(BrandTalkCommerceInfoCommand command) {
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
