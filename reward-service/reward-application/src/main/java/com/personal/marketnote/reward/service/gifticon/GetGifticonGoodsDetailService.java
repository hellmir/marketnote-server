package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.exception.GifticonGoodsNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.BrandCode;
import com.personal.marketnote.reward.domain.gifticon.CategoryCode;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.domain.gifticon.ValidDays;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.port.in.command.gifticon.GetGifticonGoodsDetailCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonGoodsDetailResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetGifticonGoodsDetailUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.GetUserPointUseCase;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
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
                goods.getGoodsCode().getValue(),
                goods.getGoodsName(),
                toBrandCodeValue(goods.getBrandCode()),
                goods.getBrandName(),
                goods.getBrandImageUrl(),
                toCategoryCodeValue(goods.getCategoryCode()),
                goods.getRealPrice().getValue(),
                goods.getSalePrice().getValue(),
                goods.getCashPrice().getValue(),
                goods.getImageUrl(),
                goods.getDescription(),
                toValidDaysValue(goods.getValidDays()),
                userCashBalance
        );
    }

    private Integer toValidDaysValue(ValidDays validDays) {
        if (FormatValidator.hasNoValue(validDays)) {
            return null;
        }
        return validDays.getValue();
    }

    private String toBrandCodeValue(BrandCode brandCode) {
        if (FormatValidator.hasNoValue(brandCode)) {
            return null;
        }
        return brandCode.getValue();
    }

    private String toCategoryCodeValue(CategoryCode categoryCode) {
        if (FormatValidator.hasNoValue(categoryCode)) {
            return null;
        }
        return categoryCode.getValue();
    }

    private GifticonGoods findExposedSaleGoods(String goodsCode) {
        GifticonGoods goods = findGifticonGoodsPort.findByGoodsCode(goodsCode)
                .orElseThrow(() -> {
                    log.warn("기프티콘 상품을 찾을 수 없습니다. goodsCode={}", goodsCode);
                    return new GifticonGoodsNotFoundException();
                });

        if (!goods.isExposed() || !goods.isSale()) {
            log.warn("기프티콘 상품이 노출되지 않거나 판매 중이 아닙니다. goodsCode={}", goodsCode);
            throw new GifticonGoodsNotFoundException();
        }

        return goods;
    }

    private Long getUserCashBalance(Long userId) {
        UserPoint userPoint = getUserPointUseCase.getUserPoint(userId);
        return userPoint.getAmountValue();
    }
}
