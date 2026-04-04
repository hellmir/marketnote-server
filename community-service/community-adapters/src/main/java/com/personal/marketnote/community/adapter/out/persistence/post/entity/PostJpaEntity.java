package com.personal.marketnote.community.adapter.out.persistence.post.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseOrderedGeneralEntity;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.domain.post.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "post")
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class PostJpaEntity extends BaseOrderedGeneralEntity {
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "parent_id")
    private Long parentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "board", nullable = false, length = 31)
    private Board board;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_group_type", length = 31)
    private PostTargetGroupType targetGroupType;

    @Column(name = "target_group_id")
    private Long targetGroupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 31)
    private PostTargetType targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "category", nullable = false, length = 63)
    private String category;

    @Column(name = "product_image_url", length = 2048)
    private String productImageUrl;

    @Column(name = "writer_name", nullable = false, length = 15)
    private String writerName;

    @Column(name = "writer_masked_name", nullable = false, length = 15)
    private String writerMaskedName;

    @Column(name = "title")
    private String title;

    @Column(name = "content", nullable = false, length = 8192)
    private String content;

    @Column(name = "is_private", nullable = false)
    private boolean isPrivate;

    @Column(name = "is_photo", nullable = false)
    private boolean isPhoto;

    public static PostJpaEntity from(Post post) {
        PostCategory category = post.getCategory();

        return PostJpaEntity.builder()
                .userId(post.getUserId())
                .parentId(post.getParentId())
                .board(post.getBoard())
                .category(FormatValidator.hasValue(category) ? category.getCode() : null)
                .targetGroupType(post.getTargetGroupType())
                .targetGroupId(post.getTargetGroupId())
                .targetType(post.getTargetType())
                .targetId(post.getTargetId())
                .productImageUrl(post.getProductImageUrl())
                .writerName(post.getWriterName())
                .writerMaskedName(post.getWriterMaskedName())
                .title(post.getTitle())
                .content(post.getContent())
                .isPrivate(post.isPrivate())
                .isPhoto(post.isPhoto())
                .build();
    }

    public void updateFrom(Post post) {
        updateActivation(post);
        title = post.getTitle();
        content = post.getContent();
    }

    private void updateActivation(Post post) {
        if (post.isActive()) {
            activate();
            return;
        }

        if (post.isInactive()) {
            deactivate();
            return;
        }

        hide();
    }
}
