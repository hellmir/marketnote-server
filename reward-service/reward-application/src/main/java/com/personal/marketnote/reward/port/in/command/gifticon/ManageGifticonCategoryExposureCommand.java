package com.personal.marketnote.reward.port.in.command.gifticon;

import java.util.List;

public record ManageGifticonCategoryExposureCommand(
        List<ExposureItem> items
) {

    public record ExposureItem(
            Long categoryId,
            boolean exposed
    ) {
    }
}
