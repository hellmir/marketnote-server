package com.personal.marketnote.notification.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCarouselCommerce;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCarouselCommerceCreateState;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCarouselItem;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCarouselItemCreateState;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCarouselTail;
import com.personal.marketnote.notification.port.in.command.BrandTalkCarouselCommerceCommand;
import com.personal.marketnote.notification.port.in.command.BrandTalkCarouselItemCommand;
import com.personal.marketnote.notification.port.in.command.BrandTalkCarouselTailCommand;

import java.util.List;

public class BrandTalkCarouselCommandToStateMapper {

    private BrandTalkCarouselCommandToStateMapper() {
    }

    public static List<BrandTalkCarouselItem> mapCarouselItems(List<BrandTalkCarouselItemCommand> commands) {
        if (FormatValidator.hasNoValue(commands)) {
            return List.of();
        }
        return commands.stream()
                .map(BrandTalkCarouselCommandToStateMapper::mapCarouselItem)
                .toList();
    }

    static BrandTalkCarouselItem mapCarouselItem(BrandTalkCarouselItemCommand cmd) {
        return BrandTalkCarouselItem.from(
                BrandTalkCarouselItemCreateState.builder()
                        .header(cmd.header())
                        .content(cmd.content())
                        .imageUrl(null)
                        .buttons(BrandTalkButtonCommandToStateMapper.mapButtons(cmd.buttons()))
                        .coupon(BrandTalkCouponCommandToStateMapper.mapCoupon(cmd.coupon()))
                        .build()
        );
    }

    public static List<BrandTalkCarouselCommerce> mapCarouselCommerces(
            List<BrandTalkCarouselCommerceCommand> commands
    ) {
        if (FormatValidator.hasNoValue(commands)) {
            return List.of();
        }
        return commands.stream()
                .map(BrandTalkCarouselCommandToStateMapper::mapCarouselCommerce)
                .toList();
    }

    static BrandTalkCarouselCommerce mapCarouselCommerce(BrandTalkCarouselCommerceCommand cmd) {
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
}
