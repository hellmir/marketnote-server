package com.personal.marketnote.fulfillment.domain.vendor.fassto.shop;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FasstoQueryParameterNoValueException;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FasstoShopMapper {
    private static final String REGISTRATION_MARKETNOTE_CODE = "registration";

    private String customerCode;
    private String accessToken;
    private String shopCd;
    private String shopNm;
    private String cstShopCd;
    private String dealStrDt;
    private String dealEndDt;
    private String zipNo;
    private String addr1;
    private String addr2;
    private String ceoNm;
    private String busNo;
    private String telNo;
    private String unloadWay;
    private String checkWay;
    private String standYn;
    private String formType;
    private String empNm;
    private String empPosit;
    private String empTelNo;
    private String useYn;

    public static FasstoShopMapper register(
            String customerCode,
            String accessToken,
            String shopNm,
            String cstShopCd,
            String dealStrDt,
            String dealEndDt,
            String zipNo,
            String addr1,
            String addr2,
            String ceoNm,
            String busNo,
            String telNo,
            String unloadWay,
            String checkWay,
            String standYn,
            String formType,
            String empNm,
            String empPosit,
            String empTelNo,
            String useYn
    ) {
        return create(
                customerCode,
                accessToken,
                REGISTRATION_MARKETNOTE_CODE,
                shopNm,
                cstShopCd,
                dealStrDt,
                dealEndDt,
                zipNo,
                addr1,
                addr2,
                ceoNm,
                busNo,
                telNo,
                unloadWay,
                checkWay,
                standYn,
                formType,
                empNm,
                empPosit,
                empTelNo,
                useYn
        );
    }

    public static FasstoShopMapper update(
            String customerCode,
            String accessToken,
            String shopCd,
            String shopNm,
            String cstShopCd,
            String dealStrDt,
            String dealEndDt,
            String zipNo,
            String addr1,
            String addr2,
            String ceoNm,
            String busNo,
            String telNo,
            String unloadWay,
            String checkWay,
            String standYn,
            String formType,
            String empNm,
            String empPosit,
            String empTelNo,
            String useYn
    ) {
        return create(
                customerCode,
                accessToken,
                shopCd,
                shopNm,
                cstShopCd,
                dealStrDt,
                dealEndDt,
                zipNo,
                addr1,
                addr2,
                ceoNm,
                busNo,
                telNo,
                unloadWay,
                checkWay,
                standYn,
                formType,
                empNm,
                empPosit,
                empTelNo,
                useYn
        );
    }

    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();

        putIfNotBlank(payload, "shopCd", shopCd);
        putIfNotBlank(payload, "shopNm", shopNm);
        putIfNotBlank(payload, "cstShopCd", cstShopCd);
        putIfNotBlank(payload, "dealStrDt", dealStrDt);
        putIfNotBlank(payload, "dealEndDt", dealEndDt);
        putIfNotBlank(payload, "zipNo", zipNo);
        putIfNotBlank(payload, "addr1", addr1);
        putIfNotBlank(payload, "addr2", addr2);
        putIfNotBlank(payload, "ceoNm", ceoNm);
        putIfNotBlank(payload, "busNo", busNo);
        putIfNotBlank(payload, "telNo", telNo);
        putIfNotBlank(payload, "unloadWay", unloadWay);
        putIfNotBlank(payload, "checkWay", checkWay);
        putIfNotBlank(payload, "standYn", standYn);
        putIfNotBlank(payload, "formType", formType);
        putIfNotBlank(payload, "empNm", empNm);
        putIfNotBlank(payload, "empPosit", empPosit);
        putIfNotBlank(payload, "empTelNo", empTelNo);
        putIfNotBlank(payload, "useYn", useYn);

        return payload;
    }

    private void validate() {
        if (FormatValidator.hasNoValue(customerCode)) {
            throw new FasstoQueryParameterNoValueException("customerCode");
        }
        if (FormatValidator.hasNoValue(accessToken)) {
            throw new FasstoQueryParameterNoValueException("accessToken");
        }
        if (FormatValidator.hasNoValue(shopCd)) {
            throw new FasstoQueryParameterNoValueException("shopCd", "shop request");
        }
        if (FormatValidator.hasNoValue(shopNm)) {
            throw new FasstoQueryParameterNoValueException("shopNm", "shop request");
        }
    }

    private void putIfNotBlank(Map<String, Object> payload, String key, String value) {
        if (FormatValidator.hasValue(value)) {
            payload.put(key, value);
        }
    }

    private static FasstoShopMapper create(
            String customerCode,
            String accessToken,
            String shopCd,
            String shopNm,
            String cstShopCd,
            String dealStrDt,
            String dealEndDt,
            String zipNo,
            String addr1,
            String addr2,
            String ceoNm,
            String busNo,
            String telNo,
            String unloadWay,
            String checkWay,
            String standYn,
            String formType,
            String empNm,
            String empPosit,
            String empTelNo,
            String useYn
    ) {
        FasstoShopMapper mapper = FasstoShopMapper.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .shopCd(shopCd)
                .shopNm(shopNm)
                .cstShopCd(cstShopCd)
                .dealStrDt(dealStrDt)
                .dealEndDt(dealEndDt)
                .zipNo(zipNo)
                .addr1(addr1)
                .addr2(addr2)
                .ceoNm(ceoNm)
                .busNo(busNo)
                .telNo(telNo)
                .unloadWay(unloadWay)
                .checkWay(checkWay)
                .standYn(standYn)
                .formType(formType)
                .empNm(empNm)
                .empPosit(empPosit)
                .empTelNo(empTelNo)
                .useYn(useYn)
                .build();
        mapper.validate();
        return mapper;
    }
}
