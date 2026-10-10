package com.personal.marketnote.product.adapter.out.persistence.image.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "image_read_model_resized_files",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_image_read_model_resized_file_model_size",
                columnNames = {"image_read_model_id", "size"}
        ),
        indexes = @Index(
                name = "idx_image_read_model_resized_file_model",
                columnList = "image_read_model_id"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ImageReadModelResizedFileJpaEntity extends BaseGeneralEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "image_read_model_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_image_read_model_resized_file_image_read_model")
    )
    private ImageReadModelJpaEntity imageReadModel;

    @Column(nullable = false, length = 63)
    private String size;

    @Column(nullable = false, length = 511)
    private String storageUrl;

    @Column(nullable = false)
    private Integer sortOrder;

    public static ImageReadModelResizedFileJpaEntity of(
            ImageReadModelJpaEntity imageReadModel, String size, String storageUrl, Integer sortOrder
    ) {
        return ImageReadModelResizedFileJpaEntity.builder()
                .imageReadModel(imageReadModel)
                .size(size)
                .storageUrl(storageUrl)
                .sortOrder(sortOrder)
                .build();
    }
}
