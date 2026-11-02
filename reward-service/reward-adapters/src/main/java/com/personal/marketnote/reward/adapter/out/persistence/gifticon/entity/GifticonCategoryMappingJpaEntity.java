package com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "gifticon_category_mapping")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class GifticonCategoryMappingJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "giftishow_category_seq", nullable = false, unique = true)
    private String giftishowCategorySeq;

    @Column(name = "gifticon_category_id", nullable = false)
    private Long gifticonCategoryId;

    public static GifticonCategoryMappingJpaEntity of(String giftishowCategorySeq, Long gifticonCategoryId) {
        return GifticonCategoryMappingJpaEntity.builder()
                .giftishowCategorySeq(giftishowCategorySeq)
                .gifticonCategoryId(gifticonCategoryId)
                .build();
    }
}
