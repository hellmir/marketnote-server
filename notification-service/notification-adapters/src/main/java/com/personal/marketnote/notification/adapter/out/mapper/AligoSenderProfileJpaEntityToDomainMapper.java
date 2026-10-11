package com.personal.marketnote.notification.adapter.out.mapper;

import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoSenderProfileJpaEntity;
import com.personal.marketnote.notification.domain.aligo.AligoSenderProfile;
import com.personal.marketnote.notification.domain.aligo.AligoSenderProfileSnapshotState;

import java.util.Optional;

public class AligoSenderProfileJpaEntityToDomainMapper {

    private AligoSenderProfileJpaEntityToDomainMapper() {
    }

    public static Optional<AligoSenderProfile> mapToDomain(AligoSenderProfileJpaEntity entity) {
        return Optional.ofNullable(entity)
                .map(e -> AligoSenderProfile.from(
                        AligoSenderProfileSnapshotState.builder()
                                .id(e.getId())
                                .senderKey(e.getSenderKey())
                                .plusId(e.getPlusId())
                                .phoneNumber(e.getPhoneNumber())
                                .categoryCode(e.getCategoryCode())
                                .name(e.getName())
                                .licenseNum(e.getLicenseNum())
                                .profileInspectionStatus(e.getProfileInspectionStatus())
                                .senderProfileStatus(e.getSenderProfileStatus())
                                .alimUseYn(e.isAlimUseYn())
                                .uuid(e.getUuid())
                                .status(e.getStatus())
                                .createdAt(e.getCreatedAt())
                                .modifiedAt(e.getModifiedAt())
                                .build()
                ));
    }
}
