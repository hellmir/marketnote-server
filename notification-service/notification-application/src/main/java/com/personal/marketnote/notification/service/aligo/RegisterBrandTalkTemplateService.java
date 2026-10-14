package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.aligo.*;
import com.personal.marketnote.notification.port.in.command.BrandTalkButtonCommand;
import com.personal.marketnote.notification.port.in.command.BrandTalkCouponCommand;
import com.personal.marketnote.notification.port.in.command.BrandTalkWideItemCommand;
import com.personal.marketnote.notification.port.in.command.RegisterBrandTalkTemplateCommand;
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
                template, command.imageFile(), command.imageFileName()
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
                        .build()
        );
    }

    private List<BrandTalkButton> mapButtons(List<BrandTalkButtonCommand> buttonCommands) {
        if (buttonCommands == null) {
            return List.of();
        }
        return buttonCommands.stream()
                .map(btn -> BrandTalkButton.of(
                        btn.name(), AligoTemplateButtonLinkType.from(btn.linkType()),
                        btn.linkM(), btn.linkP(), btn.linkI(), btn.linkA()
                ))
                .toList();
    }

    private BrandTalkCoupon mapCoupon(BrandTalkCouponCommand couponCommand) {
        if (couponCommand == null) {
            return null;
        }
        return BrandTalkCoupon.of(
                couponCommand.title(), couponCommand.description(),
                couponCommand.linkM(), couponCommand.linkP(),
                couponCommand.linkI(), couponCommand.linkA()
        );
    }

    private BrandTalkWideItem mapWideItem(BrandTalkWideItemCommand command) {
        if (command == null) {
            return null;
        }
        return BrandTalkWideItem.of(
                command.title(), command.imageUrl(),
                command.linkM(), command.linkP(), command.linkI(), command.linkA()
        );
    }

    private List<BrandTalkWideItem> mapWideItems(List<BrandTalkWideItemCommand> commands) {
        if (commands == null) {
            return List.of();
        }
        return commands.stream()
                .map(this::mapWideItem)
                .toList();
    }
}
