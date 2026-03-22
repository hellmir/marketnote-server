package com.personal.marketnote.fulfillment.domain.vendor.fassto.supplier;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.exception.FasstoQueryParameterNoValueException;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class FasstoSupplierMapper {
    private static final String REGISTRATION_SUPPLIER_CODE = "registration";

    private String customerCode;
    private String accessToken;
    private String supCd;
    private String supNm;
    private String cstSupCd;
    private String useYn;
    private String dealStrDt;
    private String dealEndDt;
    private String zipNo;
    private String addr1;
    private String addr2;
    private String ceoNm;
    private String busNo;
    private String busSp;
    private String busTp;
    private String telNo;
    private String faxNo;
    private String empNm1;
    private String empPosit1;
    private String empTelNo1;
    private String empEmail1;
    private String empNm2;
    private String empPosit2;
    private String empTelNo2;
    private String empEmail2;

    public static FasstoSupplierMapper register(
            String customerCode,
            String accessToken,
            String supNm,
            String cstSupCd,
            String useYn,
            String dealStrDt,
            String dealEndDt,
            String zipNo,
            String addr1,
            String addr2,
            String ceoNm,
            String busNo,
            String busSp,
            String busTp,
            String telNo,
            String faxNo,
            String empNm1,
            String empPosit1,
            String empTelNo1,
            String empEmail1,
            String empNm2,
            String empPosit2,
            String empTelNo2,
            String empEmail2
    ) {
        return create(
                customerCode,
                accessToken,
                REGISTRATION_SUPPLIER_CODE,
                supNm,
                cstSupCd,
                useYn,
                dealStrDt,
                dealEndDt,
                zipNo,
                addr1,
                addr2,
                ceoNm,
                busNo,
                busSp,
                busTp,
                telNo,
                faxNo,
                empNm1,
                empPosit1,
                empTelNo1,
                empEmail1,
                empNm2,
                empPosit2,
                empTelNo2,
                empEmail2
        );
    }

    public static FasstoSupplierMapper update(
            String customerCode,
            String accessToken,
            String supCd,
            String supNm,
            String cstSupCd,
            String useYn,
            String dealStrDt,
            String dealEndDt,
            String zipNo,
            String addr1,
            String addr2,
            String ceoNm,
            String busNo,
            String busSp,
            String busTp,
            String telNo,
            String faxNo,
            String empNm1,
            String empPosit1,
            String empTelNo1,
            String empEmail1,
            String empNm2,
            String empPosit2,
            String empTelNo2,
            String empEmail2
    ) {
        return create(
                customerCode,
                accessToken,
                supCd,
                supNm,
                cstSupCd,
                useYn,
                dealStrDt,
                dealEndDt,
                zipNo,
                addr1,
                addr2,
                ceoNm,
                busNo,
                busSp,
                busTp,
                telNo,
                faxNo,
                empNm1,
                empPosit1,
                empTelNo1,
                empEmail1,
                empNm2,
                empPosit2,
                empTelNo2,
                empEmail2
        );
    }

    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();

        putIfNotNull(payload, "supCd", supCd);
        putIfNotNull(payload, "supNm", supNm);
        putIfNotNull(payload, "cstSupCd", cstSupCd);
        putIfNotNull(payload, "useYn", useYn);
        putIfNotNull(payload, "dealStrDt", dealStrDt);
        putIfNotNull(payload, "dealEndDt", dealEndDt);
        putIfNotNull(payload, "zipNo", zipNo);
        putIfNotNull(payload, "addr1", addr1);
        putIfNotNull(payload, "addr2", addr2);
        putIfNotNull(payload, "ceoNm", ceoNm);
        putIfNotNull(payload, "busNo", busNo);
        putIfNotNull(payload, "busSp", busSp);
        putIfNotNull(payload, "busTp", busTp);
        putIfNotNull(payload, "telNo", telNo);
        putIfNotNull(payload, "faxNo", faxNo);
        putIfNotNull(payload, "empNm1", empNm1);
        putIfNotNull(payload, "empPosit1", empPosit1);
        putIfNotNull(payload, "empTelNo1", empTelNo1);
        putIfNotNull(payload, "empEmail1", empEmail1);
        putIfNotNull(payload, "empNm2", empNm2);
        putIfNotNull(payload, "empPosit2", empPosit2);
        putIfNotNull(payload, "empTelNo2", empTelNo2);
        putIfNotNull(payload, "empEmail2", empEmail2);

        return payload;
    }

    private void validate() {
        if (FormatValidator.hasNoValue(customerCode)) {
            throw new FasstoQueryParameterNoValueException("customerCode");
        }
        if (FormatValidator.hasNoValue(accessToken)) {
            throw new FasstoQueryParameterNoValueException("accessToken");
        }
        if (FormatValidator.hasNoValue(supCd)) {
            throw new FasstoQueryParameterNoValueException("supCd", "supplier request");
        }
        if (FormatValidator.hasNoValue(supNm)) {
            throw new FasstoQueryParameterNoValueException("supNm", "supplier request");
        }
    }

    private void putIfNotNull(Map<String, Object> payload, String key, String value) {
        if (FormatValidator.hasValue(value)) {
            payload.put(key, value);
        }
    }

    private static FasstoSupplierMapper create(
            String customerCode,
            String accessToken,
            String supCd,
            String supNm,
            String cstSupCd,
            String useYn,
            String dealStrDt,
            String dealEndDt,
            String zipNo,
            String addr1,
            String addr2,
            String ceoNm,
            String busNo,
            String busSp,
            String busTp,
            String telNo,
            String faxNo,
            String empNm1,
            String empPosit1,
            String empTelNo1,
            String empEmail1,
            String empNm2,
            String empPosit2,
            String empTelNo2,
            String empEmail2
    ) {
        FasstoSupplierMapper mapper = FasstoSupplierMapper.builder()
                .customerCode(customerCode)
                .accessToken(accessToken)
                .supCd(supCd)
                .supNm(supNm)
                .cstSupCd(cstSupCd)
                .useYn(useYn)
                .dealStrDt(dealStrDt)
                .dealEndDt(dealEndDt)
                .zipNo(zipNo)
                .addr1(addr1)
                .addr2(addr2)
                .ceoNm(ceoNm)
                .busNo(busNo)
                .busSp(busSp)
                .busTp(busTp)
                .telNo(telNo)
                .faxNo(faxNo)
                .empNm1(empNm1)
                .empPosit1(empPosit1)
                .empTelNo1(empTelNo1)
                .empEmail1(empEmail1)
                .empNm2(empNm2)
                .empPosit2(empPosit2)
                .empTelNo2(empTelNo2)
                .empEmail2(empEmail2)
                .build();
        mapper.validate();
        return mapper;
    }
}
