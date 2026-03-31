package com.personal.marketnote.product.adapter.out.persistence.image.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "image_read_models",
        uniqueConstraints = @UniqueConstraint(name = "uk_image_read_model_image_id", columnNames = "imageId"),
        indexes = @Index(name = "idx_image_read_model_target_sort", columnList = "targetId, fileSort, status")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ImageReadModelJpaEntity extends BaseGeneralEntity {

    @Column(nullable = false, unique = true)
    private Long imageId;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false, length = 31)
    private String targetType;

    @Column(nullable = false, length = 63)
    private String fileSort;

    @Column(nullable = false, length = 511)
    private String imageUrl;

    @Column(nullable = false)
    private Integer sortOrder;

    public static ImageReadModelJpaEntity of(
            Long imageId, Long targetId, String targetType,
            String fileSort, String imageUrl, Integer sortOrder
    ) {
        return ImageReadModelJpaEntity.builder()
                .imageId(imageId)
                .targetId(targetId)
                .targetType(targetType)
                .fileSort(fileSort)
                .imageUrl(imageUrl)
                .sortOrder(sortOrder)
                .build();
    }

    public void updateFrom(Long targetId, String targetType, String fileSort,
                           String imageUrl, Integer sortOrder) {
        this.targetId = targetId;
        this.targetType = targetType;
        this.fileSort = fileSort;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
        activate();
    }

    public void markInactive() {
        deactivate();
    }
}
