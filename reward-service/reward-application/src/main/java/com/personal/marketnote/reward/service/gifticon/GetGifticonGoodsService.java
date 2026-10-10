package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.gifticon.BrandCode;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.port.in.command.gifticon.GetGifticonGoodsCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonGoodsResult;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonGoodsResult.GifticonGoodsItem;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetGifticonGoodsUseCase;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.personal.marketnote.common.utility.ApiConstant.FIRST_PAGE_CURSOR_VALUE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetGifticonGoodsService implements GetGifticonGoodsUseCase {

    private final FindGifticonGoodsPort findGifticonGoodsPort;

    @Override
    public GetGifticonGoodsResult getGoods(GetGifticonGoodsCommand command) {
        Long cursor = command.cursor();
        int pageSize = command.pageSize();
        boolean isFirstPage = FormatValidator.equals(cursor, FIRST_PAGE_CURSOR_VALUE);

        List<GifticonGoods> fetched = findGifticonGoodsPort.findAllExposedByCursor(
                command.categoryCode(), command.brandCode(), cursor, pageSize + 1
        );

        boolean hasNext = fetched.size() > pageSize;
        List<GifticonGoods> paged = hasNext ? fetched.subList(0, pageSize) : fetched;

        Long nextCursor = null;
        if (FormatValidator.hasValue(paged)) {
            nextCursor = paged.getLast().getId();
        }

        Long totalElements = null;
        if (isFirstPage) {
            totalElements = findGifticonGoodsPort.countAllExposed(command.categoryCode(), command.brandCode());
        }

        List<GifticonGoodsItem> items = paged.stream()
                .map(this::mapToItem)
                .toList();

        return GetGifticonGoodsResult.from(totalElements, hasNext, nextCursor, items);
    }

    private GifticonGoodsItem mapToItem(GifticonGoods goods) {
        return new GifticonGoodsItem(
                goods.getGoodsCode().getValue(),
                goods.getGoodsName(),
                toBrandCodeValue(goods.getBrandCode()),
                goods.getBrandName(),
                goods.getBrandImageUrl(),
                goods.getSalePrice().getValue(),
                goods.getCashPrice().getValue(),
                goods.getImageUrl(),
                goods.getOrderNum()
        );
    }

    private String toBrandCodeValue(BrandCode brandCode) {
        if (FormatValidator.hasNoValue(brandCode)) {
            return null;
        }
        return brandCode.getValue();
    }
}
