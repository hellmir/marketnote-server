package com.personal.marketnote.community.port.in.result.post;

import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.domain.post.Post;
import com.personal.marketnote.community.domain.post.PostTargetType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Builder
@Getter
public class PostItemResult {
    private Long id;
    private Long userId;
    private Long parentId;
    private String board;
    private String category;
    private PostTargetType targetType;
    private Long targetId;
    private String productImageUrl;
    private String writerName;
    private String writerMaskedName;
    private String title;
    private String content;
    private boolean isPrivate;
    private boolean isPhoto;
    private List<GetFileResult> images;
    private boolean isMasked;
    private boolean isAnswered;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private PostProductInfoResult product;
    private List<PostItemResult> replies;

    public static PostItemResult from(Post post, PostProductInfoResult productInfo, List<GetFileResult> images) {
        String categoryCode = null;
        if (FormatValidator.hasValue(post.getCategory())) {
            categoryCode = post.getCategory().getCode();
        }

        return PostItemResult.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .parentId(post.getParentId())
                .board(post.getBoard().name())
                .category(categoryCode)
                .targetType(post.getTargetType())
                .targetId(post.getTargetId())
                .productImageUrl(post.getProductImageUrl())
                .writerName(post.getWriterName())
                .writerMaskedName(post.getWriterMaskedName())
                .title(post.getTitle())
                .content(post.getContent())
                .isPrivate(post.isPrivate())
                .isPhoto(post.isPhoto())
                .images(images)
                .isAnswered(post.isAnswered())
                .createdAt(post.getCreatedAt())
                .modifiedAt(post.getModifiedAt())
                .product(productInfo)
                .build();
    }

    public static PostItemResult from(Post post, List<GetFileResult> images) {
        String categoryCode = null;
        if (FormatValidator.hasValue(post.getCategory())) {
            categoryCode = post.getCategory().getCode();
        }

        return PostItemResult.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .parentId(post.getParentId())
                .board(post.getBoard().name())
                .category(categoryCode)
                .targetType(post.getTargetType())
                .targetId(post.getTargetId())
                .productImageUrl(post.getProductImageUrl())
                .writerName(post.getWriterName())
                .writerMaskedName(post.getWriterMaskedName())
                .title(post.getTitle())
                .content(post.getContent())
                .isPrivate(post.isPrivate())
                .isPhoto(post.isPhoto())
                .images(images)
                .isAnswered(post.isAnswered())
                .createdAt(post.getCreatedAt())
                .modifiedAt(post.getModifiedAt())
                .build();
    }

    public void maskPrivatePost(Long userId) {
        if (isPrivate && FormatValidator.notEquals(userId, this.userId)) {
            maskContent();
            isMasked = true;
        }
    }

    private void maskContent() {
        title = null;
        content = null;
    }

    public void addReplies(Post post, Map<Long, List<GetFileResult>> postImages) {
        replies = post.getReplies()
                .stream()
                .map(reply -> PostItemResult.from(reply, postImages.get(reply.getId())))
                .toList();

        if (FormatValidator.hasValue(replies)) {
            isAnswered = true;
        }
    }
}
