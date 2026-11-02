package com.personal.marketnote.file.port.out.event;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.file.domain.file.FileDomain;
import com.personal.marketnote.file.domain.file.ResizedFile;

import java.util.List;

public record ImageEventCommand(
        Long imageId,
        Long targetId,
        String targetType,
        String fileSort,
        String imageUrl,
        Integer sortOrder,
        List<ResizedImageCommand> resizedImages
) {

    public record ResizedImageCommand(String size, String storageUrl) {
    }

    public static ImageEventCommand from(FileDomain file) {
        return from(file, List.of());
    }

    public static ImageEventCommand from(FileDomain file, List<ResizedFile> resizedFiles) {
        List<ResizedImageCommand> mapped = resizedFiles.stream()
                .filter(resized -> FormatValidator.hasValue(resized.getFileId()))
                .filter(resized -> resized.getFileId().equals(file.getId()))
                .map(resized -> new ResizedImageCommand(resized.getSize(), resized.getStorageUrl()))
                .toList();
        return new ImageEventCommand(
                file.getId(),
                file.getOwnerId(),
                file.getOwnerType().name(),
                file.getSort().name(),
                file.getStorageUrl(),
                FormatValidator.hasValue(file.getOrderNum()) ? file.getOrderNum().intValue() : 0,
                mapped
        );
    }
}
