package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.domain.exception.GifticonGoodsNotExposedException;
import com.personal.marketnote.reward.domain.exception.GifticonGoodsNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageFeaturedGifticonGoodsCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageFeaturedGifticonGoodsCommand.FeaturedGoodsItem;
import com.personal.marketnote.reward.port.in.usecase.gifticon.ManageFeaturedGifticonGoodsUseCase;
import com.personal.marketnote.reward.port.out.gifticon.EvictGifticonGoodsCachePort;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort;
import com.personal.marketnote.reward.port.out.gifticon.UpdateGifticonGoodsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class ManageFeaturedGifticonGoodsService implements ManageFeaturedGifticonGoodsUseCase {

    private final FindGifticonGoodsPort findGifticonGoodsPort;
    private final UpdateGifticonGoodsPort updateGifticonGoodsPort;
    private final EvictGifticonGoodsCachePort evictGifticonGoodsCachePort;

    @Override
    public void manageFeatured(ManageFeaturedGifticonGoodsCommand command) {
        for (FeaturedGoodsItem item : command.items()) {
            GifticonGoods goods = findGifticonGoodsPort.findByGoodsCode(item.goodsCode())
                    .orElseThrow(() -> new GifticonGoodsNotFoundException(item.goodsCode()));

            applyFeatured(goods, item);
            updateGifticonGoodsPort.update(goods);
        }
        evictGifticonGoodsCachePort.evictFeaturedGoodsCache();
    }

    private void applyFeatured(GifticonGoods goods, FeaturedGoodsItem item) {
        if (!item.popular()) {
            goods.unmarkPopular();
            return;
        }
        if (!goods.isExposed()) {
            throw new GifticonGoodsNotExposedException(item.goodsCode());
        }
        goods.markPopular(item.popularOrderNum());
    }
}
