package com.personal.marketnote.notification.adapter.out.persistence.aligo.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import com.personal.marketnote.notification.domain.aligo.AligoProfileInspectionStatus;
import com.personal.marketnote.notification.domain.aligo.AligoSenderProfile;
import com.personal.marketnote.notification.domain.aligo.AligoSenderProfileStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "aligo_sender_profile", indexes = {
        @Index(name = "idx_aligo_sender_profile_sender_key", columnList = "sender_key", unique = true)
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class AligoSenderProfileJpaEntity extends BaseGeneralEntity {

    @Column(name = "sender_key", nullable = false, length = 100, unique = true)
    private String senderKey;

    @Column(name = "plus_id", nullable = false, length = 100)
    private String plusId;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "category_code", length = 50)
    private String categoryCode;

    @Column(name = "name", length = 200)
    private String name;

    @Column(name = "license_num", length = 50)
    private String licenseNum;

    @Column(name = "profile_inspection_status", length = 10)
    @Enumerated(EnumType.STRING)
    private AligoProfileInspectionStatus profileInspectionStatus;

    @Column(name = "sender_profile_status", length = 10)
    @Enumerated(EnumType.STRING)
    private AligoSenderProfileStatus senderProfileStatus;

    @Column(name = "alim_use_yn", nullable = false)
    private boolean alimUseYn;

    @Column(name = "uuid", length = 100)
    private String uuid;

    public void updateFrom(AligoSenderProfile profile) {
        this.profileInspectionStatus = profile.getProfileInspectionStatus();
        this.senderProfileStatus = profile.getSenderProfileStatus();
        this.alimUseYn = profile.isAlimUseYn();
        this.categoryCode = profile.getCategoryCode();
        this.name = profile.getName();
    }

    public static AligoSenderProfileJpaEntity from(AligoSenderProfile profile) {
        return AligoSenderProfileJpaEntity.builder()
                .senderKey(profile.getSenderKey())
                .plusId(profile.getPlusId())
                .phoneNumber(profile.getPhoneNumber())
                .categoryCode(profile.getCategoryCode())
                .name(profile.getName())
                .licenseNum(profile.getLicenseNum())
                .profileInspectionStatus(profile.getProfileInspectionStatus())
                .senderProfileStatus(profile.getSenderProfileStatus())
                .alimUseYn(profile.isAlimUseYn())
                .uuid(profile.getUuid())
                .build();
    }
}
