package com.personal.marketnote.reward.domain.gifticon;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class GifticonBrand {
    private Long id;
    private String brandCode;
    private String brandName;
    private String brandImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static GifticonBrand from(GifticonBrandCreateState state) {
        return GifticonBrand.builder()
                .brandCode(state.getBrandCode())
                .brandName(state.getBrandName())
                .brandImageUrl(state.getBrandImageUrl())
                .build();
    }

    public static GifticonBrand from(GifticonBrandSnapshotState state) {
        return GifticonBrand.builder()
                .id(state.getId())
                .brandCode(state.getBrandCode())
                .brandName(state.getBrandName())
                .brandImageUrl(state.getBrandImageUrl())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
    }

    public void syncFromApi(String brandName, String brandImageUrl) {
        this.brandName = brandName;
        this.brandImageUrl = brandImageUrl;
    }
}
