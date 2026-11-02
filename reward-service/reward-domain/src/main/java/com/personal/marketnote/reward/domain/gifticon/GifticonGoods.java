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
    private GoodsCode goodsCode;
    private String goodsName;
    private BrandCode brandCode;
    private String brandName;
    private String brandImageUrl;
    private CategoryCode categoryCode;
    private Money realPrice;
    private Money salePrice;
    private Money cashPrice;
    private String imageUrl;
    private String description;
    private ValidDays validDays;
    private GoodsStatus goodsStatus;
    private boolean exposed;
    private boolean popular;
    private Integer orderNum;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static GifticonGoods from(GifticonGoodsCreateState state) {
        return GifticonGoods.builder()
                .goodsCode(GoodsCode.of(state.getGoodsCode()))
                .goodsName(state.getGoodsName())
                .brandCode(BrandCode.of(state.getBrandCode()))
                .brandName(state.getBrandName())
                .brandImageUrl(state.getBrandImageUrl())
                .categoryCode(toNullableCategoryCode(state.getCategoryCode()))
                .realPrice(resolveMoneyOrZero(state.getRealPrice()))
                .salePrice(resolveMoneyOrZero(state.getSalePrice()))
                .cashPrice(resolveMoneyOrZero(state.getCashPrice()))
                .imageUrl(state.getImageUrl())
                .description(state.getDescription())
                .validDays(toNullableValidDays(state.getValidDays()))
                .goodsStatus(state.getGoodsStatus())
                .exposed(false)
                .popular(false)
                .orderNum(null)
                .build();
    }

    public static GifticonGoods from(GifticonGoodsSnapshotState state) {
        return GifticonGoods.builder()
                .id(state.getId())
                .goodsCode(GoodsCode.fromSnapshot(state.getGoodsCode()))
                .goodsName(state.getGoodsName())
                .brandCode(BrandCode.fromSnapshot(state.getBrandCode()))
                .brandName(state.getBrandName())
                .brandImageUrl(state.getBrandImageUrl())
                .categoryCode(toNullableCategoryCodeFromSnapshot(state.getCategoryCode()))
                .realPrice(resolveMoneyOrZero(state.getRealPrice()))
                .salePrice(resolveMoneyOrZero(state.getSalePrice()))
                .cashPrice(resolveMoneyOrZero(state.getCashPrice()))
                .imageUrl(state.getImageUrl())
                .description(state.getDescription())
                .validDays(toNullableValidDaysFromSnapshot(state.getValidDays()))
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
        this.brandCode = BrandCode.of(state.getBrandCode());
        this.brandName = state.getBrandName();
        this.brandImageUrl = state.getBrandImageUrl();
        this.categoryCode = toNullableCategoryCode(state.getCategoryCode());
        this.realPrice = resolveMoneyOrZero(state.getRealPrice());
        this.salePrice = resolveMoneyOrZero(state.getSalePrice());
        this.imageUrl = state.getImageUrl();
        this.description = state.getDescription();
        this.validDays = toNullableValidDays(state.getValidDays());
        this.goodsStatus = state.getGoodsStatus();
    }

    private static ValidDays toNullableValidDays(Integer value) {
        if (FormatValidator.hasNoValue(value)) {
            return null;
        }
        return ValidDays.of(value);
    }

    private static ValidDays toNullableValidDaysFromSnapshot(Integer value) {
        if (FormatValidator.hasNoValue(value)) {
            return null;
        }
        return ValidDays.fromSnapshot(value);
    }

    private static CategoryCode toNullableCategoryCode(String value) {
        if (FormatValidator.hasNoValue(value) || value.isBlank()) {
            return null;
        }
        return CategoryCode.of(value);
    }

    private static CategoryCode toNullableCategoryCodeFromSnapshot(String value) {
        if (FormatValidator.hasNoValue(value)) {
            return null;
        }
        return CategoryCode.fromSnapshot(value);
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
        return FormatValidator.hasValue(goodsStatus) && goodsStatus.isSale();
    }

    public void suspend() {
        if (!isSale()) {
            return;
        }
        this.goodsStatus = GoodsStatus.SUSPENDED;
    }
}
