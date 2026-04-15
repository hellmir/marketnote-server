package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.port.in.command.gifticon.GetGifticonGoodsCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonGoodsResult;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonGoodsResult.GifticonGoodsItem;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetGifticonGoodsUseCase;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetGifticonGoodsService implements GetGifticonGoodsUseCase {

    private final FindGifticonGoodsPort findGifticonGoodsPort;

    @Override
    public GetGifticonGoodsResult getGoods(GetGifticonGoodsCommand command) {
        long totalElements = findGifticonGoodsPort.countAllExposed(
                command.categoryCode(), command.brandCode()
        );

        List<GifticonGoods> goods = findGifticonGoodsPort.findAllExposed(
                command.categoryCode(), command.brandCode(),
                command.page(), command.pageSize()
        );

        int totalPages = calculateTotalPages(totalElements, command.pageSize());

        List<GifticonGoodsItem> items = goods.stream()
                .map(this::mapToItem)
                .toList();

        return new GetGifticonGoodsResult(
                command.page(),
                command.pageSize(),
                totalElements,
                totalPages,
                items
        );
    }

    private int calculateTotalPages(long totalElements, int pageSize) {
        if (totalElements == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / pageSize);
    }

    private GifticonGoodsItem mapToItem(GifticonGoods goods) {
        return new GifticonGoodsItem(
                goods.getGoodsCode(),
                goods.getGoodsName(),
                goods.getBrandCode(),
                goods.getBrandName(),
                goods.getBrandImageUrl(),
                goods.getSalePrice(),
                goods.getCashPrice(),
                goods.getImageUrl(),
                goods.getOrderNum()
        );
    }
}
