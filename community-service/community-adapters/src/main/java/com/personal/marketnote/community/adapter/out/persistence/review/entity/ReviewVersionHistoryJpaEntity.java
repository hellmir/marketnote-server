package com.personal.marketnote.community.adapter.out.persistence.review.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseEntity;
import com.personal.marketnote.community.domain.review.ReviewVersionHistory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "review_version_history")
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ReviewVersionHistoryJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "rating", nullable = false)
    private Float rating;

    @Column(name = "content", length = 8192)
    private String content;

    @Column(name = "photo_yn", nullable = false)
    private Boolean isPhoto;

    public static ReviewVersionHistoryJpaEntity from(ReviewVersionHistory versionHistory) {
        return ReviewVersionHistoryJpaEntity.builder()
                .reviewId(versionHistory.getReviewId())
                .rating((float) versionHistory.getRating().getValue())
                .content(versionHistory.getContent())
                .isPhoto(versionHistory.getIsPhoto())
                .build();
    }
}

