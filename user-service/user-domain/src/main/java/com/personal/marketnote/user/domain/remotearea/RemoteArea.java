package com.personal.marketnote.user.domain.remotearea;

import com.personal.marketnote.common.domain.BaseDomain;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.domain.remotearea.exception.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RemoteArea extends BaseDomain {

    private static final int MAX_PROVINCE_LENGTH = 50;
    private static final int MAX_DISTRICT_LENGTH = 50;
    private static final int MAX_VILLAGE_LENGTH = 50;
    private static final int MAX_SUBAREA_LENGTH = 50;

    private Long id;
    private String province;
    private String district;
    private String village;
    private String subarea;

    public static RemoteArea from(RemoteAreaCreateState state) {
        validateProvince(state.getProvince());

        String district = normalizeOptionalField(state.getDistrict());
        String village = normalizeOptionalField(state.getVillage());
        String subarea = normalizeOptionalField(state.getSubarea());

        validateDistrictLength(district);
        validateVillageLength(village);
        validateSubareaLength(subarea);

        return RemoteArea.builder()
                .province(state.getProvince())
                .district(district)
                .village(village)
                .subarea(subarea)
                .build();
    }

    public static RemoteArea from(RemoteAreaSnapshotState state) {
        return RemoteArea.builder()
                .id(state.getId())
                .province(state.getProvince())
                .district(state.getDistrict())
                .village(state.getVillage())
                .subarea(state.getSubarea())
                .build();
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    private static String normalizeOptionalField(String value) {
        if (FormatValidator.hasNoValue(value)) {
            return "";
        }
        return value;
    }

    private static void validateProvince(String province) {
        if (FormatValidator.hasNoValue(province)) {
            throw new RemoteAreaProvinceNoValueException();
        }
        if (province.length() > MAX_PROVINCE_LENGTH) {
            throw new InvalidRemoteAreaProvinceLengthException();
        }
    }

    private static void validateDistrictLength(String district) {
        if (district.length() > MAX_DISTRICT_LENGTH) {
            throw new InvalidRemoteAreaDistrictLengthException();
        }
    }

    private static void validateVillageLength(String village) {
        if (village.length() > MAX_VILLAGE_LENGTH) {
            throw new InvalidRemoteAreaVillageLengthException();
        }
    }

    private static void validateSubareaLength(String subarea) {
        if (subarea.length() > MAX_SUBAREA_LENGTH) {
            throw new InvalidRemoteAreaSubareaLengthException();
        }
    }
}
