package com.personal.marketnote.community.adapter.in.web.post.response;

import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.domain.post.PostTargetType;
import com.personal.marketnote.community.port.in.result.post.UserProductInquiryPostItemResult;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record UserProductInquiryPostItemResponse(
        Long id,
        Long userId,
        Long parentId,
        String board,
        String category,
        PostTargetType targetType,
        Long targetId,
        String productImageUrl,
        String writerName,
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
        List<UserProductInquiryPostItemResponse> replies
) {
    public static UserProductInquiryPostItemResponse from(UserProductInquiryPostItemResult result) {
        List<UserProductInquiryPostItemResult> replies = result.getReplies();

        return UserProductInquiryPostItemResponse.builder()
                .id(result.getId())
                .userId(result.getUserId())
                .parentId(result.getParentId())
                .board(result.getBoard())
                .category(result.getCategory())
                .targetType(result.getTargetType())
                .targetId(result.getTargetId())
                .productImageUrl(result.getProductImageUrl())
                .writerName(result.getWriterName())
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
                        .map(UserProductInquiryPostItemResponse::from)
                        .toList()
                        : List.of()
                )
                .build();
    }
}
