package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.domain.gifticon.GifticonCategory;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonCategoriesResult;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonCategoriesResult.GifticonCategoryItem;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetGifticonCategoriesUseCase;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonCategoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetGifticonCategoriesService implements GetGifticonCategoriesUseCase {

    private final FindGifticonCategoryPort findGifticonCategoryPort;

    @Override
    public GetGifticonCategoriesResult getCategories() {
        List<GifticonCategory> categories = findGifticonCategoryPort.findAllExposed();

        List<GifticonCategoryItem> items = categories.stream()
                .map(category -> new GifticonCategoryItem(
                        category.getCategoryCode(),
                        category.getEffectiveDisplayName(),
                        category.getIconUrl(),
                        category.getOrderNum()
                ))
                .toList();

        return new GetGifticonCategoriesResult(items);
    }
}
