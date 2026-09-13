package com.personal.marketnote.user.adapter.out.persistence.remotearea.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import com.personal.marketnote.user.domain.remotearea.RemoteArea;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressRegionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "remote_areas",
        uniqueConstraints = @UniqueConstraint(columnNames = {"province", "district", "village", "subarea"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RemoteAreaJpaEntity extends BaseGeneralEntity {

    @Column(name = "province", nullable = false, length = 50)
    private String province;

    @Column(name = "district", nullable = false, length = 50)
    private String district;

    @Column(name = "village", nullable = false, length = 50)
    private String village;

    @Column(name = "subarea", nullable = false, length = 50)
    private String subarea;

    @Enumerated(EnumType.STRING)
    @Column(name = "region_type", nullable = false, length = 25)
    private ShippingAddressRegionType regionType;

    public void markInactive() {
        super.deactivate();
    }

    public static RemoteAreaJpaEntity from(RemoteArea remoteArea) {
        return RemoteAreaJpaEntity.builder()
                .province(remoteArea.getProvince())
                .district(remoteArea.getDistrict())
                .village(remoteArea.getVillage())
                .subarea(remoteArea.getSubarea())
                .regionType(remoteArea.getRegionType())
                .build();
    }
}
