package com.personal.marketnote.community.adapter.in.web.review.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class UpdateReviewRequest {
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
    @Size(min = 10, max = 8192, message = "리뷰 내용은 10자 이상 8192자 이하로 입력해야 합니다.")
    private String content;

    @Schema(
            name = "isPhoto",
            description = "포토 리뷰 여부",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean isPhoto;
}
