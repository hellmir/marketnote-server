package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.domain.money.Money;
import com.personal.marketnote.reward.domain.exception.GifticonGoodsNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonGoodsCashPriceCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonGoodsCashPriceCommand.CashPriceItem;
import com.personal.marketnote.reward.port.in.usecase.gifticon.ManageGifticonGoodsCashPriceUseCase;
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
public class ManageGifticonGoodsCashPriceService implements ManageGifticonGoodsCashPriceUseCase {

    private final FindGifticonGoodsPort findGifticonGoodsPort;
    private final UpdateGifticonGoodsPort updateGifticonGoodsPort;

    @Override
    public void manageCashPrice(ManageGifticonGoodsCashPriceCommand command) {
        for (CashPriceItem item : command.items()) {
            GifticonGoods goods = findGifticonGoodsPort.findByGoodsCode(item.goodsCode())
                    .orElseThrow(() -> {
                        log.warn("기프티콘 상품을 찾을 수 없습니다. goodsCode={}", item.goodsCode());
                        return new GifticonGoodsNotFoundException();
                    });

            goods.changeCashPrice(Money.of(item.cashPrice()));
            updateGifticonGoodsPort.update(goods);
        }
    }
}
