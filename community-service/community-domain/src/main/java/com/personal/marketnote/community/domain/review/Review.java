package com.personal.marketnote.community.domain.review;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.money.Money;
import com.personal.marketnote.common.domain.quantity.Quantity;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.RandomCodeGenerator;
import com.personal.marketnote.common.utility.ValueMasker;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class Review {
    private Long id;
    private UUID reviewKey;
    private Long reviewerId;
    private Long orderId;
    private Long productId;
    private Long pricePolicyId;
    private String productImageUrl;
    private String selectedOptions;
    private Quantity quantity;
    private String reviewerName;
    private String maskedReviewerName;
    private Rating rating;
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
    private Money unitAmount;

    public static Review from(ReviewCreateState state) {
        return Review.builder()
                .reviewKey(RandomCodeGenerator.generateReviewKey())
                .reviewerId(state.getReviewerId())
                .orderId(state.getOrderId())
                .productId(state.getProductId())
                .pricePolicyId(state.getPricePolicyId())
                .productImageUrl(state.getProductImageUrl())
                .selectedOptions(state.getSelectedOptions())
                .quantity(toNullableQuantity(state.getQuantity()))
                .reviewerName(state.getReviewerName())
                .maskedReviewerName(ValueMasker.mask(state.getReviewerName()))
                .rating(state.getRating())
                .content(state.getContent())
                .isPhoto(state.getIsPhoto())
                .unitAmount(toNullableMoney(state.getUnitAmount()))
                .status(EntityStatus.ACTIVE)
                .build();
    }

    private static Money toNullableMoney(Long value) {
        if (FormatValidator.hasNoValue(value)) {
            return null;
        }
        return Money.of(value);
    }

    private static Quantity toNullableQuantity(Integer value) {
        if (FormatValidator.hasNoValue(value)) {
            return null;
        }
        return Quantity.of(value);
    }

    public static Review from(ReviewSnapshotState state) {
        return Review.builder()
                .id(state.getId())
                .reviewKey(state.getReviewKey())
                .reviewerId(state.getReviewerId())
                .orderId(state.getOrderId())
                .productId(state.getProductId())
                .pricePolicyId(state.getPricePolicyId())
                .productImageUrl(state.getProductImageUrl())
                .selectedOptions(state.getSelectedOptions())
                .quantity(toNullableQuantity(state.getQuantity()))
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
                .unitAmount(toNullableMoney(state.getUnitAmount()))
                .build();
    }

    public boolean hasUnitAmount() {
        return FormatValidator.hasValue(unitAmount);
    }

    public void updateIsUserLiked(boolean isUserLiked) {
        this.isUserLiked = isUserLiked;
    }

    public void update(Rating rating, String content, Boolean isPhoto) {
        this.rating = rating;
        this.content = content;
        this.isPhoto = isPhoto;
    }

    public boolean isOwnedBy(Long reviewerId) {
        return FormatValidator.equals(this.reviewerId, reviewerId);
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
