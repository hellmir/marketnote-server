package com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseEntity;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoodsSnapshotState;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "gifticon_goods")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class GifticonGoodsJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "goods_code", nullable = false, unique = true)
    private String goodsCode;

    @Column(name = "goods_name", nullable = false)
    private String goodsName;

    @Column(name = "brand_code", nullable = false)
    private String brandCode;

    @Column(name = "brand_name", nullable = false)
    private String brandName;

    @Column(name = "brand_image_url")
    private String brandImageUrl;

    @Column(name = "category_code")
    private String categoryCode;

    @Column(name = "real_price", nullable = false)
    private Long realPrice;

    @Column(name = "sale_price", nullable = false)
    private Long salePrice;

    @Column(name = "cash_price", nullable = false)
    private Long cashPrice;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "valid_days")
    private Integer validDays;

    @Column(name = "goods_status", nullable = false)
    private String goodsStatus;

    @Column(name = "exposed", nullable = false)
    private boolean exposed;

    @Column(name = "popular", nullable = false)
    private boolean popular;

    @Column(name = "order_num")
    private Integer orderNum;

    public static GifticonGoodsJpaEntity from(GifticonGoods domain) {
        return GifticonGoodsJpaEntity.builder()
                .id(domain.getId())
                .goodsCode(domain.getGoodsCode())
                .goodsName(domain.getGoodsName())
                .brandCode(domain.getBrandCode())
                .brandName(domain.getBrandName())
                .brandImageUrl(domain.getBrandImageUrl())
                .categoryCode(domain.getCategoryCode())
                .realPrice(domain.getRealPrice())
                .salePrice(domain.getSalePrice())
                .cashPrice(domain.getCashPrice())
                .imageUrl(domain.getImageUrl())
                .description(domain.getDescription())
                .validDays(domain.getValidDays())
                .goodsStatus(domain.getGoodsStatus())
                .exposed(domain.isExposed())
                .popular(domain.isPopular())
                .orderNum(domain.getOrderNum())
                .build();
    }

    public GifticonGoods toDomain() {
        return GifticonGoods.from(
                GifticonGoodsSnapshotState.builder()
                        .id(id)
                        .goodsCode(goodsCode)
                        .goodsName(goodsName)
                        .brandCode(brandCode)
                        .brandName(brandName)
                        .brandImageUrl(brandImageUrl)
                        .categoryCode(categoryCode)
                        .realPrice(realPrice)
                        .salePrice(salePrice)
                        .cashPrice(cashPrice)
                        .imageUrl(imageUrl)
                        .description(description)
                        .validDays(validDays)
                        .goodsStatus(goodsStatus)
                        .exposed(exposed)
                        .popular(popular)
                        .orderNum(orderNum)
                        .createdAt(getCreatedAt())
                        .modifiedAt(getModifiedAt())
                        .build()
        );
    }

    public void updateFrom(GifticonGoods domain) {
        this.goodsName = domain.getGoodsName();
        this.brandCode = domain.getBrandCode();
        this.brandName = domain.getBrandName();
        this.brandImageUrl = domain.getBrandImageUrl();
        this.categoryCode = domain.getCategoryCode();
        this.realPrice = domain.getRealPrice();
        this.salePrice = domain.getSalePrice();
        this.cashPrice = domain.getCashPrice();
        this.imageUrl = domain.getImageUrl();
        this.description = domain.getDescription();
        this.validDays = domain.getValidDays();
        this.goodsStatus = domain.getGoodsStatus();
        this.exposed = domain.isExposed();
        this.popular = domain.isPopular();
        this.orderNum = domain.getOrderNum();
    }
}
