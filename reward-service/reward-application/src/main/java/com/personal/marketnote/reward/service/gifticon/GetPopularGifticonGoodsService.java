package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.port.in.result.gifticon.GetPopularGifticonGoodsResult;
import com.personal.marketnote.reward.port.in.result.gifticon.GetPopularGifticonGoodsResult.PopularGifticonGoodsItem;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetPopularGifticonGoodsUseCase;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetPopularGifticonGoodsService implements GetPopularGifticonGoodsUseCase {

    private static final int POPULAR_GOODS_LIMIT = 10;

    private final FindGifticonGoodsPort findGifticonGoodsPort;

    @Override
    public GetPopularGifticonGoodsResult getPopularGoods() {
        List<GifticonGoods> goods = findGifticonGoodsPort.findAllPopularAndExposed(POPULAR_GOODS_LIMIT);

        List<PopularGifticonGoodsItem> items = goods.stream()
                .map(this::mapToItem)
                .toList();

        return new GetPopularGifticonGoodsResult(items);
    }

    private PopularGifticonGoodsItem mapToItem(GifticonGoods goods) {
        return new PopularGifticonGoodsItem(
                goods.getGoodsCode(),
                goods.getGoodsName(),
                goods.getBrandCode(),
                goods.getBrandName(),
                goods.getBrandImageUrl(),
                goods.getSalePrice(),
                goods.getCashPrice(),
                goods.getImageUrl()
        );
    }
}
