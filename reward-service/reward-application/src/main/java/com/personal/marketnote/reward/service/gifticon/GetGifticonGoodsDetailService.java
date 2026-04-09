package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.domain.exception.GifticonGoodsNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.port.in.command.gifticon.GetGifticonGoodsDetailCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonGoodsDetailResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetGifticonGoodsDetailUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.GetUserPointUseCase;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetGifticonGoodsDetailService implements GetGifticonGoodsDetailUseCase {

    private final FindGifticonGoodsPort findGifticonGoodsPort;
    private final GetUserPointUseCase getUserPointUseCase;

    @Override
    public GetGifticonGoodsDetailResult getGoodsDetail(GetGifticonGoodsDetailCommand command) {
        GifticonGoods goods = findExposedSaleGoods(command.goodsCode());
        Long userCashBalance = getUserCashBalance(command.userId());

        return new GetGifticonGoodsDetailResult(
                goods.getGoodsCode(),
                goods.getGoodsName(),
                goods.getBrandCode(),
                goods.getBrandName(),
                goods.getBrandImageUrl(),
                goods.getCategoryCode(),
                goods.getRealPrice(),
                goods.getSalePrice(),
                goods.getCashPrice(),
                goods.getImageUrl(),
                goods.getDescription(),
                goods.getValidDays(),
                userCashBalance
        );
    }

    private GifticonGoods findExposedSaleGoods(String goodsCode) {
        GifticonGoods goods = findGifticonGoodsPort.findByGoodsCode(goodsCode)
                .orElseThrow(() -> new GifticonGoodsNotFoundException(goodsCode));

        if (!goods.isExposed() || !goods.isSale()) {
            throw new GifticonGoodsNotFoundException(goodsCode);
        }

        return goods;
    }

    private Long getUserCashBalance(Long userId) {
        UserPoint userPoint = getUserPointUseCase.getUserPoint(userId);
        return userPoint.getAmountValue();
    }
}
