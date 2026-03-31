package com.personal.marketnote.product.port.out.file;

public interface SaveImageReadModelPort {

    void upsert(Long imageId, Long targetId, String targetType,
                String fileSort, String imageUrl, Integer sortOrder);

    void deactivateByImageId(Long imageId);
}
