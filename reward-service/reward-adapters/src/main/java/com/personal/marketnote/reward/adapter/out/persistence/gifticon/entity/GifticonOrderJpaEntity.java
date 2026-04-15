package com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseEntity;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrder;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrderSnapshotState;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "gifticon_order")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class GifticonOrderJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "goods_code", nullable = false)
    private String goodsCode;

    @Column(name = "goods_name", nullable = false)
    private String goodsName;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "product_image_url")
    private String productImageUrl;

    @Column(name = "tr_id", nullable = false, unique = true)
    private String trId;

    @Column(name = "order_no")
    private String orderNo;

    @Column(name = "cash_price", nullable = false)
    private Long cashPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private GifticonOrderStatus orderStatus;

    @Column(name = "coupon_image_url")
    private String couponImageUrl;

    @Column(name = "pin_no")
    private String pinNo;

    @Column(name = "valid_end_date")
    private LocalDate validEndDate;

    public static GifticonOrderJpaEntity from(GifticonOrder domain) {
        return GifticonOrderJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .goodsCode(domain.getGoodsCode())
                .goodsName(domain.getGoodsName())
                .brandName(domain.getBrandName())
                .productImageUrl(domain.getProductImageUrl())
                .trId(domain.getTrId())
                .orderNo(domain.getOrderNo())
                .cashPrice(domain.getCashPrice())
                .orderStatus(domain.getOrderStatus())
                .couponImageUrl(domain.getCouponImageUrl())
                .pinNo(domain.getPinNo())
                .validEndDate(domain.getValidEndDate())
                .build();
    }

    public GifticonOrder toDomain() {
        return GifticonOrder.from(
                GifticonOrderSnapshotState.builder()
                        .id(id)
                        .userId(userId)
                        .goodsCode(goodsCode)
                        .goodsName(goodsName)
                        .brandName(brandName)
                        .productImageUrl(productImageUrl)
                        .trId(trId)
                        .orderNo(orderNo)
                        .cashPrice(cashPrice)
                        .orderStatus(orderStatus)
                        .couponImageUrl(couponImageUrl)
                        .pinNo(pinNo)
                        .validEndDate(validEndDate)
                        .createdAt(getCreatedAt())
                        .modifiedAt(getModifiedAt())
                        .build()
        );
    }

    public void updateFrom(GifticonOrder domain) {
        this.orderNo = domain.getOrderNo();
        this.orderStatus = domain.getOrderStatus();
        this.couponImageUrl = domain.getCouponImageUrl();
        this.pinNo = domain.getPinNo();
        this.validEndDate = domain.getValidEndDate();
    }
}
