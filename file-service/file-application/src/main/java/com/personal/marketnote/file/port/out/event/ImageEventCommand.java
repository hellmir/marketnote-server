package com.personal.marketnote.file.port.out.event;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.file.domain.file.FileDomain;

public record ImageEventCommand(
        Long imageId,
        Long targetId,
        String targetType,
        String imageUrl,
        Integer sortOrder
) {

    public static ImageEventCommand from(FileDomain file) {
        return new ImageEventCommand(
                file.getId(),
                file.getOwnerId(),
                file.getOwnerType().name(),
                file.getStorageUrl(),
                FormatValidator.hasValue(file.getOrderNum()) ? file.getOrderNum().intValue() : 0
        );
    }
}
