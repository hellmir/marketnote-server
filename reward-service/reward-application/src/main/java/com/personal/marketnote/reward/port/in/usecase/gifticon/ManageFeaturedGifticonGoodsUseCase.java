package com.personal.marketnote.reward.port.in.usecase.gifticon;

import com.personal.marketnote.reward.port.in.command.gifticon.ManageFeaturedGifticonGoodsCommand;

public interface ManageFeaturedGifticonGoodsUseCase {
    void manageFeatured(ManageFeaturedGifticonGoodsCommand command);
}
