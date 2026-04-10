package com.personal.marketnote.community.adapter.in.web.review.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class RegisterReviewRequest {
    @Schema(
            name = "orderId",
            description = "주문 ID",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "주문 ID는 필수값입니다.")
    @Positive(message = "가격 정책 ID는 자연수만 입력 가능합니다.")
    @Min(value = 1, message = "주문 ID는 1 이상이어야 합니다.")
    @Max(value = Long.MAX_VALUE, message = "주문 ID는 정수형 최대값을 초과할 수 없습니다.")
    private Long orderId;

    @Schema(
            name = "productId",
            description = "상품 ID",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "상품 ID는 필수값입니다.")
    @Positive(message = "상품 ID는 자연수만 입력 가능합니다.")
    @Min(value = 1, message = "상품 ID는 1 이상이어야 합니다.")
    @Max(value = Long.MAX_VALUE, message = "상품 ID는 정수형 최대값을 초과할 수 없습니다.")
    private Long productId;

    @Schema(
            name = "pricePolicyId",
            description = "가격 정책 ID",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "가격 정책 ID는 필수값입니다.")
    @Positive(message = "가격 정책 ID는 자연수만 입력 가능합니다.")
    @Min(value = 1, message = "가격 정책 ID는 1 이상이어야 합니다.")
    @Max(value = Long.MAX_VALUE, message = "가격 정책 ID는 정수형 최대값을 초과할 수 없습니다.")
    private Long pricePolicyId;

    @Schema(
            name = "productImageUrl",
            description = "상품 이미지 URL",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String productImageUrl;

    @Schema(
            name = "selectedOptions",
            description = "선택된 옵션 목록",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Size(min = 1, max = 127, message = "선택된 옵션 목록은 1자 이상 127자 이하로 입력해야 합니다.")
    private String selectedOptions;

    @Schema(
            name = "quantity",
            description = "주문 수량",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "주문 수량은 필수값입니다.")
    @Positive(message = "주문 수량은 자연수만 입력 가능합니다.")
    @Min(value = 1, message = "주문 수량은 1 이상이어야 합니다.")
    @Max(value = Integer.MAX_VALUE, message = "주문 수량은 정수형 최대값을 초과할 수 없습니다.")
    private Integer quantity;

    @Schema(
            name = "reviewerName",
            description = "리뷰 작성자 이름",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "리뷰 작성자 이름은 필수값입니다.")
    @Size(min = 2, max = 15, message = "리뷰 작성자 이름은 2자 이상 15자 이하로 입력해야 합니다.")
    private String reviewerName;

    @Schema(
            name = "rating",
            description = "리뷰 평점(1 ~ 5 정수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "리뷰 평점은 필수값입니다.")
    @Digits(integer = 1, fraction = 0, message = "리뷰 평점은 정수만 입력 가능합니다.")
    @DecimalMin(value = "0.0", message = "리뷰 평점은 0.0 이상이어야 합니다.")
    @DecimalMax(value = "5.0", message = "리뷰 평점은 5.0 이하여야 합니다.")
    private Float rating;

    @Schema(
            name = "content",
            description = "리뷰 내용",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "리뷰 내용은 필수값입니다.")
    @Size(min = 10, max = 1000, message = "리뷰 내용은 10자 이상 1,000자 이하로 입력해야 합니다.")
    private String content;

    @Schema(
            name = "isPhoto",
            description = "포토 리뷰 여부",
            defaultValue = "false",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "포토 리뷰 여부는 필수값입니다.")
    private Boolean isPhoto;
}
