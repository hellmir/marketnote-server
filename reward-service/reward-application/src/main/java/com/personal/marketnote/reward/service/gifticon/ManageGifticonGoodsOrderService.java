package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.domain.exception.GifticonGoodsNotExposedException;
import com.personal.marketnote.reward.domain.exception.GifticonGoodsNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonGoodsOrderCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonGoodsOrderCommand.OrderItem;
import com.personal.marketnote.reward.port.in.usecase.gifticon.ManageGifticonGoodsOrderUseCase;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort;
import com.personal.marketnote.reward.port.out.gifticon.UpdateGifticonGoodsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class ManageGifticonGoodsOrderService implements ManageGifticonGoodsOrderUseCase {

    private final FindGifticonGoodsPort findGifticonGoodsPort;
    private final UpdateGifticonGoodsPort updateGifticonGoodsPort;

    @Override
    public void manageOrder(ManageGifticonGoodsOrderCommand command) {
        for (OrderItem item : command.items()) {
            GifticonGoods goods = findGifticonGoodsPort.findByGoodsCode(item.goodsCode())
                    .orElseThrow(() -> new GifticonGoodsNotFoundException(item.goodsCode()));

            if (!goods.isExposed()) {
                throw new GifticonGoodsNotExposedException(item.goodsCode());
            }

            goods.changeOrderNum(item.orderNum());
            updateGifticonGoodsPort.update(goods);
        }
    }
}
