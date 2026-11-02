package com.personal.marketnote.community.adapter.in.web.post.request;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.domain.post.PostTargetGroupType;
import com.personal.marketnote.community.domain.post.PostTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import org.springframework.security.access.AccessDeniedException;

@Getter
public class RegisterPostRequest {
    @Schema(
            name = "parentId",
            description = "부모 게시글 ID (답변 시 사용)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Positive(message = "부모 게시글 ID는 자연수만 입력 가능합니다.")
    private Long parentId;

    @Schema(
            name = "board",
            description = "게시판(NOTICE/FAQ/PRODUCT_INQUERY/ONE_ON_ONE_INQUERY)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "게시판은 필수입니다.")
    private Board board;

    @Schema(
            name = "category",
            description = "게시글 카테고리",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "게시글 카테고리는 필수값입니다.")
    @Size(min = 2, max = 63, message = "게시글 카테고리는 2자 이상 63자 이하로 입력해야 합니다.")
    private String category;

    @Schema(
            name = "targetGroupType",
            description = "게시글 대상 도메인 그룹 유형",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private PostTargetGroupType targetGroupType;

    @Schema(
            name = "targetGroupId",
            description = "게시글 대상 도메인 그룹 ID",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Positive(message = "대상 그룹 ID는 자연수만 입력 가능합니다.")
    @Min(value = 1, message = "대상 그룹 ID는 1 이상이어야 합니다.")
    @Max(value = Long.MAX_VALUE, message = "대상 그룹 ID는 정수형 최대값을 초과할 수 없습니다.")
    private Long targetGroupId;

    @Schema(
            name = "targetType",
            description = "게시글 대상 도메인 유형",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private PostTargetType targetType;

    @Schema(
            name = "targetId",
            description = "게시글 대상 도메인 ID",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Positive(message = "대상 ID는 자연수만 입력 가능합니다.")
    @Min(value = 1, message = "대상 ID는 1 이상이어야 합니다.")
    @Max(value = Long.MAX_VALUE, message = "대상 ID는 정수형 최대값을 초과할 수 없습니다.")
    private Long targetId;

    @Schema(
            name = "productImageUrl",
            description = "상품 이미지 URL",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String productImageUrl;

    @Schema(
            name = "writerName",
            description = "작성자 이름",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "작성자 이름은 필수값입니다.")
    @Size(min = 2, max = 15, message = "작성자 이름은 2자 이상 15자 이하로 입력해야 합니다.")
    private String writerName;

    @Schema(
            name = "title",
            description = "게시글 제목",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(min = 1, max = 255, message = "게시글 제목은 1자 이상 255자 이하로 입력해야 합니다.")
    private String title;

    @Schema(
            name = "content",
            description = "게시글 내용",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "게시글 내용은 필수값입니다.")
    @Size(min = 1, max = 5000, message = "게시글 내용은 1자 이상 5,000자 이하로 입력해야 합니다.")
    private String content;

    @Schema(
            name = "isPrivate",
            description = "비밀글 여부",
            defaultValue = "false",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean isPrivate = false;

    @Schema(
            name = "isPhoto",
            description = "이미지 첨부 여부",
            defaultValue = "false",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "포토 리뷰 여부는 필수값입니다.")
    private Boolean isPhoto;

    @Schema(
            name = "isImportant",
            description = "중요 공지 여부 (NOTICE + ANNOUNCEMENT 게시글에만 유효)",
            defaultValue = "false",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Boolean isImportant = false;

    public void validate(boolean isAdmin, boolean isSeller) {
        if (isAdminRequired() && !isAdmin) {
            throw new AccessDeniedException("관리자만 작성할 수 있습니다.");
        }

        if (isSellerRequired() && !isAdmin && !isSeller) {
            throw new AccessDeniedException("관리자 또는 판매자만 작성할 수 있습니다.");
        }
    }

    private boolean isAdminRequired() {
        return board.isAdminRequired() || (board.isOneOnOneInquery() && FormatValidator.hasValue(parentId));
    }

    private boolean isSellerRequired() {
        return board.isProductInquery() && FormatValidator.hasValue(parentId);
    }
}
