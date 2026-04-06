package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.port.in.result.gifticon.GetAdminGifticonCategoriesResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetAdminGifticonCategoriesUseCase;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonCategoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetAdminGifticonCategoriesService implements GetAdminGifticonCategoriesUseCase {
    private final FindGifticonCategoryPort findGifticonCategoryPort;

    @Override
    public GetAdminGifticonCategoriesResult getAdminGifticonCategories() {
        return GetAdminGifticonCategoriesResult.from(
                findGifticonCategoryPort.findAllOrderByOrderNumAsc()
        );
    }
}
