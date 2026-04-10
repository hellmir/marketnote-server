package com.personal.marketnote.reward.domain.gifticon;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class GifticonCategory {
    private Long id;
    private String categoryCode;
    private String categoryName;
    private String displayName;
    private String iconUrl;
    private boolean exposed;
    private Integer orderNum;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static GifticonCategory from(GifticonCategoryCreateState state) {
        return GifticonCategory.builder()
                .categoryCode(state.getCategoryCode())
                .categoryName(state.getCategoryName())
                .exposed(false)
                .orderNum(null)
                .build();
    }

    public static GifticonCategory from(GifticonCategorySnapshotState state) {
        return GifticonCategory.builder()
                .id(state.getId())
                .categoryCode(state.getCategoryCode())
                .categoryName(state.getCategoryName())
                .displayName(state.getDisplayName())
                .iconUrl(state.getIconUrl())
                .exposed(state.isExposed())
                .orderNum(state.getOrderNum())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
    }

    public String getEffectiveDisplayName() {
        if (FormatValidator.hasValue(displayName)) {
            return displayName;
        }
        return categoryName;
    }

    public void updateDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void updateIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void expose() {
        this.exposed = true;
    }

    public void unexpose() {
        this.exposed = false;
    }

    public void changeOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public void syncFromApi(String categoryName) {
        this.categoryName = categoryName;
    }
}
