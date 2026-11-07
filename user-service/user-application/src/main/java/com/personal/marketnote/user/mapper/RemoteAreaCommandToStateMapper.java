package com.personal.marketnote.user.mapper;

import com.personal.marketnote.user.domain.remotearea.RemoteAreaCreateState;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressRegionType;
import com.personal.marketnote.user.port.in.command.remotearea.RegisterRemoteAreaCommand;

public class RemoteAreaCommandToStateMapper {
    private RemoteAreaCommandToStateMapper() {
    }

    public static RemoteAreaCreateState mapToCreateState(
            RegisterRemoteAreaCommand command, ShippingAddressRegionType regionType
    ) {
        return RemoteAreaCreateState.builder()
                .province(command.province())
                .district(command.district())
                .village(command.village())
                .subarea(command.subarea())
                .regionType(regionType)
                .build();
    }
}
