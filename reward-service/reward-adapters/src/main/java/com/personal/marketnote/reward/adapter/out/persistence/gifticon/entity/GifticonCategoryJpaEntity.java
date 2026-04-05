package com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseEntity;
import com.personal.marketnote.reward.domain.gifticon.GifticonCategory;
import com.personal.marketnote.reward.domain.gifticon.GifticonCategorySnapshotState;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "gifticon_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class GifticonCategoryJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_code", nullable = false, unique = true)
    private String categoryCode;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "exposed", nullable = false)
    private boolean exposed;

    @Column(name = "order_num")
    private Integer orderNum;

    public static GifticonCategoryJpaEntity from(GifticonCategory domain) {
        return GifticonCategoryJpaEntity.builder()
                .id(domain.getId())
                .categoryCode(domain.getCategoryCode())
                .categoryName(domain.getCategoryName())
                .displayName(domain.getDisplayName())
                .iconUrl(domain.getIconUrl())
                .exposed(domain.isExposed())
                .orderNum(domain.getOrderNum())
                .build();
    }

    public GifticonCategory toDomain() {
        return GifticonCategory.from(
                GifticonCategorySnapshotState.builder()
                        .id(id)
                        .categoryCode(categoryCode)
                        .categoryName(categoryName)
                        .displayName(displayName)
                        .iconUrl(iconUrl)
                        .exposed(exposed)
                        .orderNum(orderNum)
                        .createdAt(getCreatedAt())
                        .modifiedAt(getModifiedAt())
                        .build()
        );
    }

    public void updateFrom(GifticonCategory domain) {
        this.categoryName = domain.getCategoryName();
        this.displayName = domain.getDisplayName();
        this.iconUrl = domain.getIconUrl();
        this.exposed = domain.isExposed();
        this.orderNum = domain.getOrderNum();
    }
}
