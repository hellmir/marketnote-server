package com.personal.marketnote.product.port.out.fulfillment;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.exception.FulfillmentVendorGoodsCustomCodeNoValueException;
import com.personal.marketnote.product.exception.FulfillmentVendorGoodsGiftDivisionNoValueException;
import com.personal.marketnote.product.exception.FulfillmentVendorGoodsNameNoValueException;
import com.personal.marketnote.product.exception.FulfillmentVendorGoodsTypeNoValueException;
import lombok.Builder;

@Builder
public record RegisterFulfillmentVendorGoodsCommand(
        String cstGodCd,
        String godNm,
        String godType,
        String giftDiv,
        String godOptCd1,
        String godOptCd2,
        String invGodNmUseYn,
        String invGodNm,
        String supCd,
        String cateCd,
        String seasonCd,
        String genderCd,
        String makeYr,
        String godPr,
        String inPr,
        String salPr,
        String dealTemp,
        String pickFac,
        String godBarcd,
        String boxWeight,
        String origin,
        String distTermMgtYn,
        String useTermDay,
        String outCanDay,
        String inCanDay,
        String boxDiv,
        String bufGodYn,
        String loadingDirection,
        String subMate,
        String useYn,
        String safetyStock,
        String feeYn,
        String saleUnitQty,
        String cstGodImgUrl,
        String externalGodImgUrl
) {
    public RegisterFulfillmentVendorGoodsCommand {
        if (FormatValidator.hasNoValue(cstGodCd)) {
            throw new FulfillmentVendorGoodsCustomCodeNoValueException();
        }
        if (FormatValidator.hasNoValue(godNm)) {
            throw new FulfillmentVendorGoodsNameNoValueException();
        }
        if (FormatValidator.hasNoValue(godType)) {
            throw new FulfillmentVendorGoodsTypeNoValueException();
        }
        if (FormatValidator.hasNoValue(giftDiv)) {
            throw new FulfillmentVendorGoodsGiftDivisionNoValueException();
        }
    }
}
