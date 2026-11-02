package com.personal.marketnote.product.port.out.file;

import java.util.List;

public interface SaveImageReadModelPort {

    void upsert(
            Long imageId, Long targetId, String targetType, String fileSort,
            String imageUrl, Integer sortOrder, List<ResizedImageInput> resizedImages
    );

    void deactivateByImageId(Long imageId);

    record ResizedImageInput(String size, String storageUrl) {
    }
}
