package com.personal.marketnote.reward.domain.gifticon;

import com.personal.marketnote.common.domain.money.Money;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class GifticonGoods {
    private Long id;
    private String goodsCode;
    private String goodsName;
    private String brandCode;
    private String brandName;
    private String brandImageUrl;
    private String categoryCode;
    private Money realPrice;
    private Money salePrice;
    private Money cashPrice;
    private String imageUrl;
    private String description;
    private Integer validDays;
    private String goodsStatus;
    private boolean exposed;
    private boolean popular;
    private Integer orderNum;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static GifticonGoods from(GifticonGoodsCreateState state) {
        return GifticonGoods.builder()
                .goodsCode(state.getGoodsCode())
                .goodsName(state.getGoodsName())
                .brandCode(state.getBrandCode())
                .brandName(state.getBrandName())
                .brandImageUrl(state.getBrandImageUrl())
                .categoryCode(state.getCategoryCode())
                .realPrice(resolveMoneyOrZero(state.getRealPrice()))
                .salePrice(resolveMoneyOrZero(state.getSalePrice()))
                .cashPrice(resolveMoneyOrZero(state.getCashPrice()))
                .imageUrl(state.getImageUrl())
                .description(state.getDescription())
                .validDays(state.getValidDays())
                .goodsStatus(state.getGoodsStatus())
                .exposed(false)
                .popular(false)
                .orderNum(null)
                .build();
    }

    public static GifticonGoods from(GifticonGoodsSnapshotState state) {
        return GifticonGoods.builder()
                .id(state.getId())
                .goodsCode(state.getGoodsCode())
                .goodsName(state.getGoodsName())
                .brandCode(state.getBrandCode())
                .brandName(state.getBrandName())
                .brandImageUrl(state.getBrandImageUrl())
                .categoryCode(state.getCategoryCode())
                .realPrice(resolveMoneyOrZero(state.getRealPrice()))
                .salePrice(resolveMoneyOrZero(state.getSalePrice()))
                .cashPrice(resolveMoneyOrZero(state.getCashPrice()))
                .imageUrl(state.getImageUrl())
                .description(state.getDescription())
                .validDays(state.getValidDays())
                .goodsStatus(state.getGoodsStatus())
                .exposed(state.isExposed())
                .popular(state.isPopular())
                .orderNum(state.getOrderNum())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
    }

    public void syncFromApi(GifticonGoodsSyncState state) {
        this.goodsName = state.getGoodsName();
        this.brandCode = state.getBrandCode();
        this.brandName = state.getBrandName();
        this.brandImageUrl = state.getBrandImageUrl();
        this.categoryCode = state.getCategoryCode();
        this.realPrice = resolveMoneyOrZero(state.getRealPrice());
        this.salePrice = resolveMoneyOrZero(state.getSalePrice());
        this.imageUrl = state.getImageUrl();
        this.description = state.getDescription();
        this.validDays = state.getValidDays();
        this.goodsStatus = state.getGoodsStatus();
    }

    private static Money resolveMoneyOrZero(Long value) {
        if (FormatValidator.hasNoValue(value)) {
            return Money.zero();
        }
        return Money.of(value);
    }

    public void expose() {
        this.exposed = true;
    }

    public void unexpose() {
        this.exposed = false;
    }

    public void changeOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public void markPopular(Integer popularOrderNum) {
        this.popular = true;
        this.orderNum = popularOrderNum;
    }

    public void unmarkPopular() {
        this.popular = false;
        this.orderNum = null;
    }

    public boolean isSale() {
        return "SALE".equals(this.goodsStatus);
    }

    public void suspend() {
        if (!isSale()) {
            return;
        }
        this.goodsStatus = "SUS";
    }
}
