package com.personal.marketnote.product.adapter.out.persistence.image.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import com.personal.marketnote.common.utility.FormatValidator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(
            mappedBy = "imageReadModel",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<ImageReadModelResizedFileJpaEntity> resizedFiles = new ArrayList<>();

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
                .resizedFiles(new ArrayList<>())
                .build();
    }

    public void updateFrom(
            Long targetId, String targetType, String fileSort,
            String imageUrl, Integer sortOrder, List<ResizedFileInput> resizedInputs
    ) {
        this.targetId = targetId;
        this.targetType = targetType;
        this.fileSort = fileSort;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
        replaceResizedFiles(resizedInputs);
        activate();
    }

    public void replaceResizedFiles(List<ResizedFileInput> resizedInputs) {
        resizedFiles.clear();
        if (FormatValidator.hasNoValue(resizedInputs)) {
            return;
        }
        for (int i = 0; i < resizedInputs.size(); i++) {
            ResizedFileInput input = resizedInputs.get(i);
            resizedFiles.add(ImageReadModelResizedFileJpaEntity.of(this, input.size(), input.storageUrl(), i));
        }
    }

    public void markInactive() {
        deactivate();
    }

    public record ResizedFileInput(String size, String storageUrl) {
    }
}
