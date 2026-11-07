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
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
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
                    .orElseThrow(() -> {
                        log.warn("기프티콘 상품을 찾을 수 없습니다. goodsCode={}", item.goodsCode());
                        return new GifticonGoodsNotFoundException();
                    });

            if (!goods.isExposed()) {
                log.warn("노출되지 않은 상품에는 정렬 순서를 설정할 수 없습니다. goodsCode={}", item.goodsCode());
                throw new GifticonGoodsNotExposedException();
            }

            goods.changeOrderNum(item.orderNum());
            updateGifticonGoodsPort.update(goods);
        }
    }
}
