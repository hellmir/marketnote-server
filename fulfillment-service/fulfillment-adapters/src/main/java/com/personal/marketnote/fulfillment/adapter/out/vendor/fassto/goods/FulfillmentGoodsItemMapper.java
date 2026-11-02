package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.goods;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FulfillmentGoodsItemMapper {
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

    public static FulfillmentGoodsItemMapper of(
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
        FulfillmentGoodsItemMapper mapper = FulfillmentGoodsItemMapper.builder()
                .cstGodCd(cstGodCd)
                .godNm(godNm)
                .godType(godType)
                .giftDiv(giftDiv)
                .godOptCd1(godOptCd1)
                .godOptCd2(godOptCd2)
                .invGodNmUseYn(invGodNmUseYn)
                .invGodNm(invGodNm)
                .supCd(supCd)
                .cateCd(cateCd)
                .seasonCd(seasonCd)
                .genderCd(genderCd)
                .makeYr(makeYr)
                .godPr(godPr)
                .inPr(inPr)
                .salPr(salPr)
                .dealTemp(dealTemp)
                .pickFac(pickFac)
                .godBarcd(godBarcd)
                .boxWeight(boxWeight)
                .origin(origin)
                .distTermMgtYn(distTermMgtYn)
                .useTermDay(useTermDay)
                .outCanDay(outCanDay)
                .inCanDay(inCanDay)
                .boxDiv(boxDiv)
                .bufGodYn(bufGodYn)
                .loadingDirection(loadingDirection)
                .subMate(subMate)
                .useYn(useYn)
                .safetyStock(safetyStock)
                .feeYn(feeYn)
                .saleUnitQty(saleUnitQty)
                .cstGodImgUrl(cstGodImgUrl)
                .externalGodImgUrl(externalGodImgUrl)
                .build();
        mapper.validate();
        return mapper;
    }

    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfNotNull(payload, "cstGodCd", cstGodCd);
        putIfNotNull(payload, "godNm", godNm);
        putIfNotNull(payload, "godType", godType);
        putIfNotNull(payload, "giftDiv", giftDiv);
        putIfNotNull(payload, "godOptCd1", godOptCd1);
        putIfNotNull(payload, "godOptCd2", godOptCd2);
        putIfNotNull(payload, "invGodNmUseYn", invGodNmUseYn);
        putIfNotNull(payload, "invGodNm", invGodNm);
        putIfNotNull(payload, "supCd", supCd);
        putIfNotNull(payload, "cateCd", cateCd);
        putIfNotNull(payload, "seasonCd", seasonCd);
        putIfNotNull(payload, "genderCd", genderCd);
        putIfNotNull(payload, "makeYr", makeYr);
        putIfNotNull(payload, "godPr", godPr);
        putIfNotNull(payload, "inPr", inPr);
        putIfNotNull(payload, "salPr", salPr);
        putIfNotNull(payload, "dealTemp", dealTemp);
        putIfNotNull(payload, "pickFac", pickFac);
        putIfNotNull(payload, "godBarcd", godBarcd);
        putIfNotNull(payload, "boxWeight", boxWeight);
        putIfNotNull(payload, "origin", origin);
        putIfNotNull(payload, "distTermMgtYn", distTermMgtYn);
        putIfNotNull(payload, "useTermDay", useTermDay);
        putIfNotNull(payload, "outCanDay", outCanDay);
        putIfNotNull(payload, "inCanDay", inCanDay);
        putIfNotNull(payload, "boxDiv", boxDiv);
        putIfNotNull(payload, "bufGodYn", bufGodYn);
        putIfNotNull(payload, "loadingDirection", loadingDirection);
        putIfNotNull(payload, "subMate", subMate);
        putIfNotNull(payload, "useYn", useYn);
        putIfNotNull(payload, "safetyStock", safetyStock);
        putIfNotNull(payload, "feeYn", feeYn);
        putIfNotNull(payload, "saleUnitQty", saleUnitQty);
        putIfNotNull(payload, "cstGodImgUrl", cstGodImgUrl);
        putIfNotNull(payload, "externalGodImgUrl", externalGodImgUrl);
        return payload;
    }

    private void validate() {
        if (FormatValidator.hasNoValue(cstGodCd)) {
            throw new FulfillmentQueryParameterNoValueException("cstGodCd", "goods request");
        }
        if (FormatValidator.hasNoValue(godNm)) {
            throw new FulfillmentQueryParameterNoValueException("godNm", "goods request");
        }
        if (FormatValidator.hasNoValue(godType)) {
            throw new FulfillmentQueryParameterNoValueException("godType", "goods request");
        }
        if (FormatValidator.hasNoValue(giftDiv)) {
            throw new FulfillmentQueryParameterNoValueException("giftDiv", "goods request");
        }
    }

    private void putIfNotNull(Map<String, Object> payload, String key, String value) {
        if (FormatValidator.hasValue(value)) {
            payload.put(key, value);
        }
    }
}
