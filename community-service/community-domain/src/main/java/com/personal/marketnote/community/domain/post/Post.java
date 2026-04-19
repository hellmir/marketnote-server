package com.personal.marketnote.community.domain.post;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.ValueMasker;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class Post {
    private Long id;
    private Long userId;
    private Long parentId;
    private Board board;
    private PostCategory category;
    private PostTargetGroupType targetGroupType;
    private Long targetGroupId;
    private PostTargetType targetType;
    private Long targetId;
    private String productImageUrl;
    private String writerName;
    private String maskedWriterName;
    private String title;
    private String content;
    private boolean isPrivate;
    private boolean isPhoto;
    private boolean isAnswered;
    private EntityStatus status;

    @Builder.Default
    private List<Post> replies = Collections.emptyList();

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime modifiedAt;

    private Long orderNum;

    public static Post from(PostCreateState state) {
        return Post.builder()
                .userId(state.getUserId())
                .parentId(state.getParentId())
                .board(state.getBoard())
                .category(PostCategoryResolver.resolve(state.getBoard(), state.getCategory()))
                .targetGroupType(state.getTargetGroupType())
                .targetGroupId(state.getTargetGroupId())
                .targetType(state.getTargetType())
                .targetId(state.getTargetId())
                .productImageUrl(state.getProductImageUrl())
                .writerName(state.getWriterName())
                .maskedWriterName(resolveMaskedWriterName(state))
                .title(state.getTitle())
                .content(state.getContent())
                .isPrivate(state.isPrivate())
                .isPhoto(state.isPhoto())
                .status(EntityStatus.ACTIVE)
                .build();
    }

    private static String resolveMaskedWriterName(PostCreateState state) {
        if (state.getBoard().requiresWriterMasking()) {
            return ValueMasker.mask(state.getWriterName());
        }
        return state.getWriterName();
    }

    public static Post from(PostSnapshotState state) {
        return Post.builder()
                .id(state.getId())
                .userId(state.getUserId())
                .parentId(state.getParentId())
                .board(state.getBoard())
                .category(PostCategoryResolver.resolve(state.getBoard(), state.getCategory()))
                .targetGroupType(state.getTargetGroupType())
                .targetGroupId(state.getTargetGroupId())
                .targetType(state.getTargetType())
                .targetId(state.getTargetId())
                .productImageUrl(state.getProductImageUrl())
                .writerName(state.getWriterName())
                .maskedWriterName(state.getMaskedWriterName())
                .title(state.getTitle())
                .content(state.getContent())
                .isPrivate(state.isPrivate())
                .isPhoto(state.isPhoto())
                .status(state.getStatus())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .orderNum(state.getOrderNum())
                .build();
    }

    public boolean isActive() {
        return status.isActive();
    }

    public boolean isInactive() {
        return status.isInactive();
    }

    public boolean isStatusChanged(boolean isVisible) {
        return FormatValidator.notEquals(status.isActive(), isVisible);
    }

    public void updateReplies(List<Post> replies) {
        this.replies = replies;
        isAnswered = FormatValidator.hasValue(replies);
    }

    public boolean hasReplies() {
        return FormatValidator.hasValue(getReplies());
    }

    public void changeExposure() {
        status = EntityStatus.changeVisibility(status);
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public boolean isReply() {
        return FormatValidator.hasValue(parentId);
    }

    public boolean isProductInquiryPost() {
        return board.isProductInquery();
    }

    public boolean isEditable() {
        return board.isEditable();
    }

    public void addReplies(List<Post> replies) {
        this.replies = replies;
    }
}
