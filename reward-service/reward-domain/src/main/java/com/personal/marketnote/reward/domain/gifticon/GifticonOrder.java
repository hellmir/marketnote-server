package com.personal.marketnote.reward.domain.gifticon;

import com.personal.marketnote.reward.domain.exception.InvalidGifticonOrderStatusTransitionException;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class GifticonOrder {
    private Long id;
    private Long userId;
    private String goodsCode;
    private String goodsName;
    private String brandName;
    private String productImageUrl;
    private String trId;
    private String orderNo;
    private Long cashPrice;
    private GifticonOrderStatus orderStatus;
    private String couponImageUrl;
    private String pinNo;
    private LocalDate validEndDate;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static GifticonOrder from(GifticonOrderSnapshotState state) {
        return GifticonOrder.builder()
                .id(state.getId())
                .userId(state.getUserId())
                .goodsCode(state.getGoodsCode())
                .goodsName(state.getGoodsName())
                .brandName(state.getBrandName())
                .productImageUrl(state.getProductImageUrl())
                .trId(state.getTrId())
                .orderNo(state.getOrderNo())
                .cashPrice(state.getCashPrice())
                .orderStatus(state.getOrderStatus())
                .couponImageUrl(state.getCouponImageUrl())
                .pinNo(state.getPinNo())
                .validEndDate(state.getValidEndDate())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
    }

    public void issue(String couponImageUrl, String pinNo, String orderNo, LocalDate validEndDate) {
        if (!isPending()) {
            throw new InvalidGifticonOrderStatusTransitionException(this.orderStatus);
        }
        this.orderStatus = GifticonOrderStatus.ISSUED;
        this.couponImageUrl = couponImageUrl;
        this.pinNo = pinNo;
        this.orderNo = orderNo;
        this.validEndDate = validEndDate;
    }

    public void markSendFailed() {
        if (!isPending()) {
            throw new InvalidGifticonOrderStatusTransitionException(this.orderStatus);
        }
        this.orderStatus = GifticonOrderStatus.SEND_FAILED;
    }

    public void cancel() {
        if (isPending() || isIssued() || isSendFailed()) {
            this.orderStatus = GifticonOrderStatus.CANCELLED;
            return;
        }
        throw new InvalidGifticonOrderStatusTransitionException(this.orderStatus);
    }

    public void syncStatus(GifticonOrderStatus newStatus) {
        this.orderStatus = newStatus;
    }

    public boolean isPending() {
        return this.orderStatus.isPending();
    }

    public boolean isIssued() {
        return this.orderStatus.isIssued();
    }

    public boolean isAvailable() {
        return this.orderStatus.isAvailable();
    }

    public boolean isSendFailed() {
        return this.orderStatus.isSendFailed();
    }

    public boolean isTerminal() {
        return this.orderStatus.isTerminal();
    }
}
