package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.exception.GifticonCategoryNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonCategory;
import com.personal.marketnote.reward.port.in.command.gifticon.UpdateGifticonCategoryCommand;
import com.personal.marketnote.reward.port.in.usecase.gifticon.UpdateGifticonCategoryUseCase;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonCategoryPort;
import com.personal.marketnote.reward.port.out.gifticon.UpdateGifticonCategoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class UpdateGifticonCategoryService implements UpdateGifticonCategoryUseCase {
    private final FindGifticonCategoryPort findGifticonCategoryPort;
    private final UpdateGifticonCategoryPort updateGifticonCategoryPort;

    @Override
    public void updateGifticonCategory(UpdateGifticonCategoryCommand command) {
        GifticonCategory category = findGifticonCategoryPort.findById(command.categoryId())
                .orElseThrow(() -> new GifticonCategoryNotFoundException(command.categoryId()));

        applyDisplayName(category, command);
        applyIconUrl(category, command);

        updateGifticonCategoryPort.update(category);
    }

    private void applyDisplayName(GifticonCategory category, UpdateGifticonCategoryCommand command) {
        if (!command.displayNameProvided()) {
            return;
        }
        if (FormatValidator.hasNoValue(command.displayName())) {
            category.updateDisplayName(null);
            return;
        }
        category.updateDisplayName(command.displayName());
    }

    private void applyIconUrl(GifticonCategory category, UpdateGifticonCategoryCommand command) {
        if (!command.iconUrlProvided()) {
            return;
        }
        category.updateIconUrl(command.iconUrl());
    }
}
