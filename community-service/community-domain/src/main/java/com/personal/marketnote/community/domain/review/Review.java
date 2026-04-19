package com.personal.marketnote.community.domain.review;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.ValueMasker;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class Review {
    private Long id;
    private Long reviewerId;
    private Long orderId;
    private Long productId;
    private Long pricePolicyId;
    private String productImageUrl;
    private String selectedOptions;
    private Integer quantity;
    private String reviewerName;
    private String maskedReviewerName;
    private Float rating;
    private String content;
    private Boolean isPhoto;
    private Boolean isEdited;
    private Integer likeCount;
    private boolean isUserLiked;
    private EntityStatus status;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime modifiedAt;

    private Long orderNum;
    private Long unitAmount;

    public static Review from(ReviewCreateState state) {
        return Review.builder()
                .reviewerId(state.getReviewerId())
                .orderId(state.getOrderId())
                .productId(state.getProductId())
                .pricePolicyId(state.getPricePolicyId())
                .productImageUrl(state.getProductImageUrl())
                .selectedOptions(state.getSelectedOptions())
                .quantity(state.getQuantity())
                .reviewerName(state.getReviewerName())
                .maskedReviewerName(ValueMasker.mask(state.getReviewerName()))
                .rating(round(state.getRating()))
                .content(state.getContent())
                .isPhoto(state.getIsPhoto())
                .unitAmount(state.getUnitAmount())
                .status(EntityStatus.ACTIVE)
                .build();
    }

    private static Float round(Float value) {
        return BigDecimal.valueOf(value)
                .setScale(0, RoundingMode.HALF_UP)
                .floatValue();
    }

    public static Review from(ReviewSnapshotState state) {
        return Review.builder()
                .id(state.getId())
                .reviewerId(state.getReviewerId())
                .orderId(state.getOrderId())
                .productId(state.getProductId())
                .pricePolicyId(state.getPricePolicyId())
                .productImageUrl(state.getProductImageUrl())
                .selectedOptions(state.getSelectedOptions())
                .quantity(state.getQuantity())
                .reviewerName(state.getReviewerName())
                .maskedReviewerName(state.getMaskedReviewerName())
                .rating(state.getRating())
                .content(state.getContent())
                .isPhoto(state.getIsPhoto())
                .isEdited(state.getIsEdited())
                .likeCount(state.getLikeCount())
                .status(state.getStatus())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .orderNum(state.getOrderNum())
                .unitAmount(state.getUnitAmount())
                .build();
    }

    public boolean hasUnitAmount() {
        return FormatValidator.hasValue(unitAmount);
    }

    public void updateIsUserLiked(boolean isUserLiked) {
        this.isUserLiked = isUserLiked;
    }

    public void update(Float rating, String content, Boolean isPhoto) {
        this.rating = rating;
        this.content = content;
        this.isPhoto = isPhoto;
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

    public void delete() {
        status = EntityStatus.from(false);
    }

    public void changeExposure() {
        status = EntityStatus.changeVisibility(status);
    }
}
