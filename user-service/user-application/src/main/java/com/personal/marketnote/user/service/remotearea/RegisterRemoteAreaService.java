package com.personal.marketnote.user.service.remotearea;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.domain.remotearea.RemoteArea;
import com.personal.marketnote.user.domain.remotearea.RemoteAreaCreateState;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressRegionType;
import com.personal.marketnote.user.exception.RemoteAreaAlreadyExistsException;
import com.personal.marketnote.user.port.in.command.remotearea.RegisterRemoteAreaCommand;
import com.personal.marketnote.user.port.in.usecase.remotearea.RegisterRemoteAreaUseCase;
import com.personal.marketnote.user.port.out.remotearea.FindRemoteAreaPort;
import com.personal.marketnote.user.port.out.remotearea.SaveRemoteAreaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
public class RegisterRemoteAreaService implements RegisterRemoteAreaUseCase {

    private final FindRemoteAreaPort findRemoteAreaPort;
    private final SaveRemoteAreaPort saveRemoteAreaPort;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void registerRemoteArea(RegisterRemoteAreaCommand command) {
        ShippingAddressRegionType regionType = resolveRegionType(command.regionType());

        RemoteArea remoteArea = RemoteArea.from(
                RemoteAreaCreateState.builder()
                        .province(command.province())
                        .district(command.district())
                        .village(command.village())
                        .subarea(command.subarea())
                        .regionType(regionType)
                        .build()
        );

        validateNotDuplicate(remoteArea);

        saveRemoteAreaPort.save(remoteArea);
    }

    private ShippingAddressRegionType resolveRegionType(String regionType) {
        if (FormatValidator.hasNoValue(regionType)) {
            return ShippingAddressRegionType.ISLAND;
        }
        return ShippingAddressRegionType.valueOf(regionType);
    }

    private void validateNotDuplicate(RemoteArea remoteArea) {
        if (findRemoteAreaPort.existsByAddress(remoteArea.getProvince(), remoteArea.getDistrict(), remoteArea.getVillage(), remoteArea.getSubarea())) {
            throw new RemoteAreaAlreadyExistsException(remoteArea.getProvince(), remoteArea.getDistrict(), remoteArea.getVillage(), remoteArea.getSubarea());
        }
    }
}
