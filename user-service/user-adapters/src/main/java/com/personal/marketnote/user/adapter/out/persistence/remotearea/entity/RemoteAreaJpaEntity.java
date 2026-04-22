package com.personal.marketnote.user.adapter.out.persistence.remotearea.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import com.personal.marketnote.user.domain.remotearea.RemoteArea;
import com.personal.marketnote.user.domain.remotearea.RemoteAreaType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "remote_areas",
        uniqueConstraints = @UniqueConstraint(columnNames = {"zip_code"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RemoteAreaJpaEntity extends BaseGeneralEntity {

    @Column(name = "zip_code", nullable = false, length = 5)
    private String zipCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "remote_area_type", nullable = false, length = 30)
    private RemoteAreaType remoteAreaType;

    @Column(name = "region_name", nullable = false, length = 100)
    private String regionName;

    public static RemoteAreaJpaEntity from(RemoteArea remoteArea) {
        return RemoteAreaJpaEntity.builder()
                .zipCode(remoteArea.getZipCode())
                .remoteAreaType(remoteArea.getRemoteAreaType())
                .regionName(remoteArea.getRegionName())
                .build();
    }
}
