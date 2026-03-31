package com.personal.marketnote.product.adapter.out.web.fulfillment.request;

import com.personal.marketnote.product.port.out.fulfillment.UpdateFulfillmentVendorGoodsCommand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateFasstoGoodsItemRequest {
    private String cstGodCd;
    private String godNm;
    private String godType;
    private String giftDiv;
    private String godOptCd1;
    private String godOptCd2;
    private String invGodNmUseYn;
    private String invGodNm;
    private String supCd;
    private String cateCd;
    private String seasonCd;
    private String genderCd;
    private String makeYr;
    private String godPr;
    private String inPr;
    private String salPr;
    private String dealTemp;
    private String pickFac;
    private String godBarcd;
    private String boxWeight;
    private String origin;
    private String distTermMgtYn;
    private String useTermDay;
    private String outCanDay;
    private String inCanDay;
    private String boxDiv;
    private String bufGodYn;
    private String loadingDirection;
    private String subMate;
    private String useYn;
    private String safetyStock;
    private String feeYn;
    private String saleUnitQty;
    private String cstGodImgUrl;
    private String externalGodImgUrl;

    public static UpdateFasstoGoodsItemRequest from(UpdateFulfillmentVendorGoodsCommand command) {
        return UpdateFasstoGoodsItemRequest.builder()
                .cstGodCd(command.customerGoodsCode())
                .godNm(command.goodsName())
                .godType(command.goodsType())
                .giftDiv(command.giftDivision())
                .godOptCd1(command.goodsOptionCode1())
                .godOptCd2(command.goodsOptionCode2())
                .invGodNmUseYn(command.invoiceGoodsNameEnabled())
                .invGodNm(command.invoiceGoodsName())
                .supCd(command.supplierCode())
                .cateCd(command.categoryCode())
                .seasonCd(command.seasonCode())
                .genderCd(command.genderCode())
                .makeYr(command.manufactureYear())
                .godPr(command.unitPrice())
                .inPr(command.supplyPrice())
                .salPr(command.salePrice())
                .dealTemp(command.handlingTemperature())
                .pickFac(command.pickingFacility())
                .godBarcd(command.goodsBarcode())
                .boxWeight(command.boxWeight())
                .origin(command.origin())
                .distTermMgtYn(command.expirationDateManagementEnabled())
                .useTermDay(command.shelfLifeDays())
                .outCanDay(command.outboundAvailableDays())
                .inCanDay(command.inboundAvailableDays())
                .boxDiv(command.outboundBoxType())
                .bufGodYn(command.cushioningEnabled())
                .loadingDirection(command.loadingDirection())
                .subMate(command.subsidiaryMaterialCode())
                .useYn(command.enabled())
                .safetyStock(command.safetyStock())
                .feeYn(command.feeApplied())
                .saleUnitQty(command.saleUnitQuantity())
                .cstGodImgUrl(command.customerGoodsImageUrl())
                .externalGodImgUrl(command.externalGoodsImageUrl())
                .build();
    }
}
