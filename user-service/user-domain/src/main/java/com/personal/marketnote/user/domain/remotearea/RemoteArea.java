package com.personal.marketnote.user.domain.remotearea;

import com.personal.marketnote.common.domain.BaseDomain;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.domain.remotearea.exception.InvalidRemoteAreaRegionNameLengthException;
import com.personal.marketnote.user.domain.remotearea.exception.InvalidRemoteAreaZipCodeException;
import com.personal.marketnote.user.domain.remotearea.exception.RemoteAreaRegionNameNoValueException;
import com.personal.marketnote.user.domain.remotearea.exception.RemoteAreaTypeNoValueException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RemoteArea extends BaseDomain {

    private static final Pattern ZIP_CODE_PATTERN = Pattern.compile("^\\d{5}$");

    private Long id;
    private String zipCode;
    private RemoteAreaType remoteAreaType;
    private String regionName;

    public static RemoteArea from(RemoteAreaCreateState state) {
        validateZipCode(state.getZipCode());
        validateRemoteAreaType(state.getRemoteAreaType());
        validateRegionName(state.getRegionName());

        return RemoteArea.builder()
                .zipCode(state.getZipCode())
                .remoteAreaType(state.getRemoteAreaType())
                .regionName(state.getRegionName())
                .build();
    }

    public static RemoteArea from(RemoteAreaSnapshotState state) {
        return RemoteArea.builder()
                .id(state.getId())
                .zipCode(state.getZipCode())
                .remoteAreaType(state.getRemoteAreaType())
                .regionName(state.getRegionName())
                .build();
    }

    public boolean isJeju() {
        return remoteAreaType.isJeju();
    }

    public boolean isIslandMountainous() {
        return remoteAreaType.isIslandMountainous();
    }

    private static void validateZipCode(String zipCode) {
        if (!FormatValidator.isValid(zipCode, ZIP_CODE_PATTERN)) {
            throw new InvalidRemoteAreaZipCodeException();
        }
    }

    private static void validateRemoteAreaType(RemoteAreaType remoteAreaType) {
        if (FormatValidator.hasNoValue(remoteAreaType)) {
            throw new RemoteAreaTypeNoValueException();
        }
    }

    private static void validateRegionName(String regionName) {
        if (FormatValidator.hasNoValue(regionName)) {
            throw new RemoteAreaRegionNameNoValueException();
        }
        if (regionName.length() > 100) {
            throw new InvalidRemoteAreaRegionNameLengthException();
        }
    }
}
