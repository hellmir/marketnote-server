package com.personal.marketnote.community.adapter.in.web.post.response;

import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.domain.post.PostTargetType;
import com.personal.marketnote.community.port.in.result.post.PostItemResult;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record PostItemResponse(
        Long id,
        Long userId,
        Long parentId,
        String board,
        String category,
        PostTargetType targetType,
        Long targetId,
        String productImageUrl,
        String writerName,
        String writerMaskedName,
        String title,
        String content,
        boolean isPrivate,
        boolean isPhoto,
        List<GetFileResult> images,
        boolean isMasked,
        boolean isAnswered,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        PostProductInfoResponse product,
        List<PostItemResponse> replies
) {
    public static PostItemResponse from(PostItemResult result) {
        List<PostItemResult> replies = result.getReplies();

        return PostItemResponse.builder()
                .id(result.getId())
                .userId(result.getUserId())
                .parentId(result.getParentId())
                .board(result.getBoard())
                .category(result.getCategory())
                .targetType(result.getTargetType())
                .targetId(result.getTargetId())
                .productImageUrl(result.getProductImageUrl())
                .writerName(result.getWriterName())
                .writerMaskedName(result.getWriterMaskedName())
                .title(result.getTitle())
                .content(result.getContent())
                .isPrivate(result.isPrivate())
                .isPhoto(result.isPhoto())
                .images(result.getImages())
                .isMasked(result.isMasked())
                .isAnswered(result.isAnswered())
                .createdAt(result.getCreatedAt())
                .modifiedAt(result.getModifiedAt())
                .product(PostProductInfoResponse.from(result.getProduct()))
                .replies(FormatValidator.hasValue(replies)
                        ? replies.stream()
                        .map(PostItemResponse::from)
                        .toList()
                        : List.of()
                )
                .build();
    }
}
