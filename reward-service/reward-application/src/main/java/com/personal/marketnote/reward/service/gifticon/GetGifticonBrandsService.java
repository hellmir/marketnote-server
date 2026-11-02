package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.port.in.command.gifticon.GetGifticonBrandsCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonBrandsResult;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonBrandsResult.GifticonBrandItem;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetGifticonBrandsUseCase;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort.GifticonGoodsBrandProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetGifticonBrandsService implements GetGifticonBrandsUseCase {

    private final FindGifticonGoodsPort findGifticonGoodsPort;

    @Override
    public GetGifticonBrandsResult getBrands(GetGifticonBrandsCommand command) {
        List<GifticonGoodsBrandProjection> projections =
                findGifticonGoodsPort.findDistinctBrandsByCategoryCode(command.categoryCode());

        List<GifticonBrandItem> items = projections.stream()
                .map(projection -> new GifticonBrandItem(
                        projection.brandCode(),
                        projection.brandName(),
                        projection.brandImageUrl()
                ))
                .toList();

        return new GetGifticonBrandsResult(items);
    }
}
