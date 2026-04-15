package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.domain.exception.GifticonGoodsNotFoundException;
import com.personal.marketnote.reward.domain.exception.GifticonGoodsNotSaleException;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonGoodsExposureCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonGoodsExposureCommand.ExposureItem;
import com.personal.marketnote.reward.port.in.usecase.gifticon.ManageGifticonGoodsExposureUseCase;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort;
import com.personal.marketnote.reward.port.out.gifticon.UpdateGifticonGoodsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class ManageGifticonGoodsExposureService implements ManageGifticonGoodsExposureUseCase {

    private final FindGifticonGoodsPort findGifticonGoodsPort;
    private final UpdateGifticonGoodsPort updateGifticonGoodsPort;

    @Override
    public void manageExposure(ManageGifticonGoodsExposureCommand command) {
        for (ExposureItem item : command.items()) {
            GifticonGoods goods = findGifticonGoodsPort.findByGoodsCode(item.goodsCode())
                    .orElseThrow(() -> new GifticonGoodsNotFoundException(item.goodsCode()));

            applyExposure(goods, item);
            updateGifticonGoodsPort.update(goods);
        }
    }

    private void applyExposure(GifticonGoods goods, ExposureItem item) {
        if (!item.exposed()) {
            goods.unexpose();
            return;
        }
        if (!goods.isSale()) {
            throw new GifticonGoodsNotSaleException(item.goodsCode(), goods.getGoodsStatus());
        }
        goods.expose();
    }
}
