package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.domain.exception.GifticonCategoryNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonCategory;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonCategoryExposureCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonCategoryExposureCommand.ExposureItem;
import com.personal.marketnote.reward.port.in.usecase.gifticon.ManageGifticonCategoryExposureUseCase;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonCategoryPort;
import com.personal.marketnote.reward.port.out.gifticon.UpdateGifticonCategoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class ManageGifticonCategoryExposureService implements ManageGifticonCategoryExposureUseCase {
    private final FindGifticonCategoryPort findGifticonCategoryPort;
    private final UpdateGifticonCategoryPort updateGifticonCategoryPort;

    @Override
    public void manageExposure(ManageGifticonCategoryExposureCommand command) {
        for (ExposureItem item : command.items()) {
            GifticonCategory category = findGifticonCategoryPort.findById(item.categoryId())
                    .orElseThrow(() -> new GifticonCategoryNotFoundException(item.categoryId()));

            applyExposure(category, item.exposed());
            updateGifticonCategoryPort.update(category);
        }
    }

    private void applyExposure(GifticonCategory category, boolean exposed) {
        if (exposed) {
            category.expose();
            return;
        }
        category.unexpose();
    }
}
