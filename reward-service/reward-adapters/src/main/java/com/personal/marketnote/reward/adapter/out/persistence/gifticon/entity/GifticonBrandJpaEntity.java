package com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseEntity;
import com.personal.marketnote.reward.domain.gifticon.GifticonBrand;
import com.personal.marketnote.reward.domain.gifticon.GifticonBrandSnapshotState;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "gifticon_brand")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class GifticonBrandJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "brand_code", nullable = false, unique = true)
    private String brandCode;

    @Column(name = "brand_name", nullable = false)
    private String brandName;

    @Column(name = "brand_image_url")
    private String brandImageUrl;

    public static GifticonBrandJpaEntity from(GifticonBrand domain) {
        return GifticonBrandJpaEntity.builder()
                .id(domain.getId())
                .brandCode(domain.getBrandCode())
                .brandName(domain.getBrandName())
                .brandImageUrl(domain.getBrandImageUrl())
                .build();
    }

    public GifticonBrand toDomain() {
        return GifticonBrand.from(
                GifticonBrandSnapshotState.builder()
                        .id(id)
                        .brandCode(brandCode)
                        .brandName(brandName)
                        .brandImageUrl(brandImageUrl)
                        .createdAt(getCreatedAt())
                        .modifiedAt(getModifiedAt())
                        .build()
        );
    }

    public void updateFrom(GifticonBrand domain) {
        this.brandName = domain.getBrandName();
        this.brandImageUrl = domain.getBrandImageUrl();
    }
}
