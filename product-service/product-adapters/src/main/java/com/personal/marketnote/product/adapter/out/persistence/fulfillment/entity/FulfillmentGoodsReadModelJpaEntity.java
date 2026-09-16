package com.personal.marketnote.product.adapter.out.persistence.fulfillment.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "fulfillment_goods_read_models",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_fulfillment_goods_read_model_customer_goods_code",
                columnNames = "customerGoodsCode"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class FulfillmentGoodsReadModelJpaEntity extends BaseGeneralEntity {

    @Column(nullable = false, unique = true)
    private String customerGoodsCode;

    private String goodsCode;
    private String goodsName;
    private String goodsType;
    private String goodsTypeName;
    private String invoiceGoodsNameEnabled;
    private String goodsOptionCode1;
    private String goodsOptionCode2;
    private String customerCode;
    private String customerName;
    private String supplierCode;
    private String supplierName;
    private String categoryCode;
    private String categoryName;
    private String seasonCode;
    private String genderCode;
    private String unitPrice;
    private String supplyPrice;
    private String salePrice;
    private String handlingTemperature;
    private String pickingFacility;
    private String giftDivision;
    private String giftDivisionName;
    private String goodsWidth;
    private String goodsLength;
    private String goodsHeight;
    private String manufactureYear;
    private String goodsBulk;
    private String goodsWeight;
    private String goodsSideSum;
    private String goodsVolume;
    private String goodsBarcode;
    private String boxWidth;
    private String boxLength;
    private String boxHeight;
    private String boxBulk;
    private String boxWeight;
    private String innerBoxBarcode;
    private String innerBoxLength;
    private String innerBoxHeight;
    private String innerBoxBulk;
    private String innerBoxWidth;
    private String innerBoxWeight;
    private String innerBoxSideSum;
    private String boxInnerCount;
    private String innerBoxInnerCount;
    private String palletInnerCount;
    private String origin;
    private String expirationDateManagementEnabled;
    private String shelfLifeDays;
    private String outboundAvailableDays;
    private String inboundAvailableDays;
    private String outboundBoxType;
    private String cushioningEnabled;
    private String loadingDirection;
    private String firstInboundDate;
    private String enabled;
    private String feeApplied;
    private String saleUnitQuantity;
    private String oneDayDeliveryEnabled;
    private String safetyStock;

    @OneToMany(mappedBy = "fulfillmentGoods", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FulfillmentGoodsElementReadModelJpaEntity> elements = new ArrayList<>();

    public static FulfillmentGoodsReadModelJpaEntity of(String customerGoodsCode) {
        return FulfillmentGoodsReadModelJpaEntity.builder()
                .customerGoodsCode(customerGoodsCode)
                .build();
    }

    public void updateFrom(
            String goodsCode, String goodsName, String goodsType, String goodsTypeName,
            String invoiceGoodsNameEnabled, String goodsOptionCode1, String goodsOptionCode2,
            String customerCode, String customerName, String supplierCode, String supplierName,
            String categoryCode, String categoryName, String seasonCode, String genderCode,
            String unitPrice, String supplyPrice, String salePrice, String handlingTemperature,
            String pickingFacility, String giftDivision, String giftDivisionName,
            String goodsWidth, String goodsLength, String goodsHeight, String manufactureYear,
            String goodsBulk, String goodsWeight, String goodsSideSum, String goodsVolume,
            String goodsBarcode, String boxWidth, String boxLength, String boxHeight,
            String boxBulk, String boxWeight, String innerBoxBarcode, String innerBoxLength,
            String innerBoxHeight, String innerBoxBulk, String innerBoxWidth, String innerBoxWeight,
            String innerBoxSideSum, String boxInnerCount, String innerBoxInnerCount,
            String palletInnerCount, String origin, String expirationDateManagementEnabled,
            String shelfLifeDays, String outboundAvailableDays, String inboundAvailableDays,
            String outboundBoxType, String cushioningEnabled, String loadingDirection,
            String firstInboundDate, String enabled, String feeApplied, String saleUnitQuantity,
            String oneDayDeliveryEnabled, String safetyStock
    ) {
        this.goodsCode = goodsCode;
        this.goodsName = goodsName;
        this.goodsType = goodsType;
        this.goodsTypeName = goodsTypeName;
        this.invoiceGoodsNameEnabled = invoiceGoodsNameEnabled;
        this.goodsOptionCode1 = goodsOptionCode1;
        this.goodsOptionCode2 = goodsOptionCode2;
        this.customerCode = customerCode;
        this.customerName = customerName;
        this.supplierCode = supplierCode;
        this.supplierName = supplierName;
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
        this.seasonCode = seasonCode;
        this.genderCode = genderCode;
        this.unitPrice = unitPrice;
        this.supplyPrice = supplyPrice;
        this.salePrice = salePrice;
        this.handlingTemperature = handlingTemperature;
        this.pickingFacility = pickingFacility;
        this.giftDivision = giftDivision;
        this.giftDivisionName = giftDivisionName;
        this.goodsWidth = goodsWidth;
        this.goodsLength = goodsLength;
        this.goodsHeight = goodsHeight;
        this.manufactureYear = manufactureYear;
        this.goodsBulk = goodsBulk;
        this.goodsWeight = goodsWeight;
        this.goodsSideSum = goodsSideSum;
        this.goodsVolume = goodsVolume;
        this.goodsBarcode = goodsBarcode;
        this.boxWidth = boxWidth;
        this.boxLength = boxLength;
        this.boxHeight = boxHeight;
        this.boxBulk = boxBulk;
        this.boxWeight = boxWeight;
        this.innerBoxBarcode = innerBoxBarcode;
        this.innerBoxLength = innerBoxLength;
        this.innerBoxHeight = innerBoxHeight;
        this.innerBoxBulk = innerBoxBulk;
        this.innerBoxWidth = innerBoxWidth;
        this.innerBoxWeight = innerBoxWeight;
        this.innerBoxSideSum = innerBoxSideSum;
        this.boxInnerCount = boxInnerCount;
        this.innerBoxInnerCount = innerBoxInnerCount;
        this.palletInnerCount = palletInnerCount;
        this.origin = origin;
        this.expirationDateManagementEnabled = expirationDateManagementEnabled;
        this.shelfLifeDays = shelfLifeDays;
        this.outboundAvailableDays = outboundAvailableDays;
        this.inboundAvailableDays = inboundAvailableDays;
        this.outboundBoxType = outboundBoxType;
        this.cushioningEnabled = cushioningEnabled;
        this.loadingDirection = loadingDirection;
        this.firstInboundDate = firstInboundDate;
        this.enabled = enabled;
        this.feeApplied = feeApplied;
        this.saleUnitQuantity = saleUnitQuantity;
        this.oneDayDeliveryEnabled = oneDayDeliveryEnabled;
        this.safetyStock = safetyStock;
        activate();
    }

    public void replaceElements(List<FulfillmentGoodsElementReadModelJpaEntity> newElements) {
        this.elements.clear();
        for (FulfillmentGoodsElementReadModelJpaEntity element : newElements) {
            element.assignFulfillmentGoods(this);
            this.elements.add(element);
        }
    }
}
