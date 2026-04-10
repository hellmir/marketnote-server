package com.personal.marketnote.reward.port.in.command.gifticon;

public record UpdateGifticonCategoryCommand(
        Long categoryId,
        String displayName,
        boolean displayNameProvided,
        String iconUrl,
        boolean iconUrlProvided
) {
}
