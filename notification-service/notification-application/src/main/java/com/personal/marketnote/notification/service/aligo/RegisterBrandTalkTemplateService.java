package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.aligo.*;
import com.personal.marketnote.notification.port.in.command.*;
import com.personal.marketnote.notification.port.in.result.aligo.RegisterBrandTalkTemplateResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.RegisterBrandTalkTemplateUseCase;
import com.personal.marketnote.notification.port.out.aligo.RegisterBrandTalkTemplateExternalPort;
import com.personal.marketnote.notification.port.out.aligo.SaveBrandTalkTemplatePort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class RegisterBrandTalkTemplateService implements RegisterBrandTalkTemplateUseCase {

    private final RegisterBrandTalkTemplateExternalPort registerBrandTalkTemplateExternalPort;
    private final SaveBrandTalkTemplatePort saveBrandTalkTemplatePort;

    @Override
    public RegisterBrandTalkTemplateResult registerTemplate(RegisterBrandTalkTemplateCommand command) {
        AligoBrandTalkTemplate template = createTemplate(command);

        registerBrandTalkTemplateExternalPort.registerTemplate(
                template, command.imageFile(), command.imageFileName(),
                command.carouselItemImageFiles(), command.carouselItemImageFileNames()
        );

        AligoBrandTalkTemplate saved = saveBrandTalkTemplatePort.save(template);

        return new RegisterBrandTalkTemplateResult(
                saved.getId(), saved.getTemplateCode(),
                saved.getTemplateName(), saved.getTemplateType().name()
        );
    }

    private AligoBrandTalkTemplate createTemplate(RegisterBrandTalkTemplateCommand command) {
        List<BrandTalkButton> buttons = mapButtons(command.buttons());
        BrandTalkCoupon coupon = mapCoupon(command.coupon());

        return AligoBrandTalkTemplate.from(
                AligoBrandTalkTemplateCreateState.builder()
                        .senderKey(command.senderKey())
                        .templateCode(command.templateCode())
                        .templateName(command.templateName())
                        .templateType(BrandTalkTemplateType.from(command.templateType()))
                        .templateContent(command.templateContent())
                        .adult(command.adult())
                        .imageUrl(null)
                        .imageLink(command.imageLink())
                        .buttons(buttons)
                        .coupon(coupon)
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
                        .build()
        );
    }

    private List<BrandTalkButton> mapButtons(List<BrandTalkButtonCommand> buttonCommands) {
        if (FormatValidator.hasNoValue(buttonCommands)) {
            return List.of();
        }
        return buttonCommands.stream()
                .map(btn -> BrandTalkButton.from(BrandTalkButtonCreateState.builder().name(btn.name()).linkType(AligoTemplateButtonLinkType.from(btn.linkType())).linkM(btn.linkM()).linkP(btn.linkP()).linkI(btn.linkI()).linkA(btn.linkA()).build()))
                .toList();
    }

    private BrandTalkCoupon mapCoupon(BrandTalkCouponCommand couponCommand) {
        if (FormatValidator.hasNoValue(couponCommand)) {
            return null;
        }
        return BrandTalkCoupon.from(BrandTalkCouponCreateState.builder().title(couponCommand.title()).description(couponCommand.description()).linkM(couponCommand.linkM()).linkP(couponCommand.linkP()).linkI(couponCommand.linkI()).linkA(couponCommand.linkA()).build());
    }

    private BrandTalkWideItem mapWideItem(BrandTalkWideItemCommand command) {
        if (FormatValidator.hasNoValue(command)) {
            return null;
        }
        return BrandTalkWideItem.from(BrandTalkWideItemCreateState.builder().title(command.title()).imageUrl(command.imageUrl()).linkM(command.linkM()).linkP(command.linkP()).linkI(command.linkI()).linkA(command.linkA()).build());
    }

    private List<BrandTalkWideItem> mapWideItems(List<BrandTalkWideItemCommand> commands) {
        if (FormatValidator.hasNoValue(commands)) {
            return List.of();
        }
        return commands.stream()
                .map(this::mapWideItem)
                .toList();
    }

    private List<BrandTalkCarouselItem> mapCarouselItems(List<BrandTalkCarouselItemCommand> commands) {
        if (FormatValidator.hasNoValue(commands)) {
            return List.of();
        }
        return commands.stream()
                .map(cmd -> BrandTalkCarouselItem.from(BrandTalkCarouselItemCreateState.builder().header(cmd.header()).content(cmd.content()).imageUrl(null).buttons(mapButtons(cmd.buttons())).coupon(mapCoupon(cmd.coupon())).build()))
                .toList();
    }

    private List<BrandTalkCarouselCommerce> mapCarouselCommerces(List<BrandTalkCarouselCommerceCommand> commands) {
        if (FormatValidator.hasNoValue(commands)) {
            return List.of();
        }
        return commands.stream()
                .map(cmd -> BrandTalkCarouselCommerce.from(BrandTalkCarouselCommerceCreateState.builder().title(cmd.title()).regularPrice(cmd.regularPrice()).discountPrice(cmd.discountPrice()).discountRate(cmd.discountRate()).discountFixed(cmd.discountFixed()).build()))
                .toList();
    }

    private BrandTalkCarouselTail mapCarouselTail(BrandTalkCarouselTailCommand command) {
        if (FormatValidator.hasNoValue(command)) {
            return null;
        }
        return BrandTalkCarouselTail.of(command.linkM());
    }

    private BrandTalkCommerceInfo mapCommerceInfo(BrandTalkCommerceInfoCommand command) {
        if (FormatValidator.hasNoValue(command)) {
            return null;
        }
        return BrandTalkCommerceInfo.from(BrandTalkCommerceInfoCreateState.builder().title(command.title()).regularPrice(command.regularPrice()).discountPrice(command.discountPrice()).discountRate(command.discountRate()).discountFixed(command.discountFixed()).build());
    }
}
