package com.personal.marketnote.user.adapter.out.persistence.shippingaddress;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.adapter.out.persistence.remotearea.repository.RemoteAreaJpaRepository;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressRegionType;
import com.personal.marketnote.user.port.out.shippingaddress.ClassifyShippingAddressRegionPort;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@PersistenceAdapter
@RequiredArgsConstructor
public class ShippingAddressRegionClassifier implements ClassifyShippingAddressRegionPort {

    private static final Map<String, String> PROVINCE_ABBREVIATIONS = Map.ofEntries(
            Map.entry("서울특별시", "서울"),
            Map.entry("부산광역시", "부산"),
            Map.entry("대구광역시", "대구"),
            Map.entry("인천광역시", "인천"),
            Map.entry("광주광역시", "광주"),
            Map.entry("대전광역시", "대전"),
            Map.entry("울산광역시", "울산"),
            Map.entry("세종특별자치시", "세종"),
            Map.entry("경기도", "경기"),
            Map.entry("충청북도", "충북"),
            Map.entry("충청남도", "충남"),
            Map.entry("전라남도", "전남"),
            Map.entry("전라북도", "전북"),
            Map.entry("전북특별자치도", "전북"),
            Map.entry("경상북도", "경북"),
            Map.entry("경상남도", "경남"),
            Map.entry("강원도", "강원"),
            Map.entry("강원특별자치도", "강원"),
            Map.entry("제주특별자치도", "제주")
    );

    private final RemoteAreaJpaRepository remoteAreaJpaRepository;

    @Override
    public ShippingAddressRegionType classify(String address) {
        if (FormatValidator.hasNoValue(address)) {
            return ShippingAddressRegionType.NORMAL;
        }

        if (isJejuAddress(address)) {
            return ShippingAddressRegionType.JEJU;
        }

        if (isRemoteAreaAddress(address)) {
            return ShippingAddressRegionType.ISLAND;
        }

        return ShippingAddressRegionType.NORMAL;
    }

    private boolean isJejuAddress(String address) {
        return address.startsWith("제주");
    }

    private boolean isRemoteAreaAddress(String address) {
        String[] tokens = address.split(" ");
        if (tokens.length < 2) {
            return false;
        }

        String province = normalizeProvince(tokens[0]);
        String district = tokens[1];

        return remoteAreaJpaRepository.existsByProvinceAndDistrictAndStatus(province, district, EntityStatus.ACTIVE);
    }

    private String normalizeProvince(String rawProvince) {
        String abbreviation = PROVINCE_ABBREVIATIONS.get(rawProvince);
        if (FormatValidator.hasValue(abbreviation)) {
            return abbreviation;
        }
        return rawProvince;
    }
}
