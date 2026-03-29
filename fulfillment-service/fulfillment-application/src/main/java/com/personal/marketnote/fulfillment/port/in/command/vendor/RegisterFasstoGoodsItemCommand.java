package com.personal.marketnote.fulfillment.port.in.command.vendor;

import lombok.Builder;

@Builder
public record RegisterFasstoGoodsItemCommand(
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
}
