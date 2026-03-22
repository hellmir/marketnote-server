package com.personal.marketnote.fulfillment.port.in.command.vendor;

import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
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
    public static RegisterFasstoGoodsItemCommand ofMinimal(
            String cstGodCd,
            String godNm,
            String godType,
            String giftDiv
    ) {
        return RegisterFasstoGoodsItemCommand.builder()
                .cstGodCd(cstGodCd)
                .godNm(godNm)
                .godType(godType)
                .giftDiv(giftDiv)
                .build();
    }

    public static RegisterFasstoGoodsItemCommand of(
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
        return new RegisterFasstoGoodsItemCommand(
                cstGodCd,
                godNm,
                godType,
                giftDiv,
                godOptCd1,
                godOptCd2,
                invGodNmUseYn,
                invGodNm,
                supCd,
                cateCd,
                seasonCd,
                genderCd,
                makeYr,
                godPr,
                inPr,
                salPr,
                dealTemp,
                pickFac,
                godBarcd,
                boxWeight,
                origin,
                distTermMgtYn,
                useTermDay,
                outCanDay,
                inCanDay,
                boxDiv,
                bufGodYn,
                loadingDirection,
                subMate,
                useYn,
                safetyStock,
                feeYn,
                saleUnitQty,
                cstGodImgUrl,
                externalGodImgUrl
        );
    }
}
