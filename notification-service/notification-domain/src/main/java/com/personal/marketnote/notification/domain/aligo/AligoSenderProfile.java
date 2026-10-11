package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.domain.BaseDomain;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class AligoSenderProfile extends BaseDomain {

    private Long id;
    private String senderKey;
    private String plusId;
    private String phoneNumber;
    private String categoryCode;
    private String name;
    private String licenseNum;
    private AligoProfileInspectionStatus profileInspectionStatus;
    private AligoSenderProfileStatus senderProfileStatus;
    private boolean alimUseYn;
    private String uuid;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static AligoSenderProfile from(AligoSenderProfileCreateState state) {
        validate(state);
        AligoSenderProfile profile = AligoSenderProfile.builder()
                .senderKey(state.getSenderKey())
                .plusId(state.getPlusId())
                .phoneNumber(state.getPhoneNumber())
                .categoryCode(state.getCategoryCode())
                .name(state.getName())
                .licenseNum(state.getLicenseNum())
                .profileInspectionStatus(state.getProfileInspectionStatus())
                .senderProfileStatus(state.getSenderProfileStatus())
                .alimUseYn(state.isAlimUseYn())
                .uuid(state.getUuid())
                .build();
        profile.activate();
        return profile;
    }

    public static AligoSenderProfile from(AligoSenderProfileSnapshotState state) {
        AligoSenderProfile profile = AligoSenderProfile.builder()
                .id(state.getId())
                .senderKey(state.getSenderKey())
                .plusId(state.getPlusId())
                .phoneNumber(state.getPhoneNumber())
                .categoryCode(state.getCategoryCode())
                .name(state.getName())
                .licenseNum(state.getLicenseNum())
                .profileInspectionStatus(state.getProfileInspectionStatus())
                .senderProfileStatus(state.getSenderProfileStatus())
                .alimUseYn(state.isAlimUseYn())
                .uuid(state.getUuid())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
        profile.status = state.getStatus();
        return profile;
    }

    public void updateFromExternal(
            AligoProfileInspectionStatus profileInspectionStatus,
            AligoSenderProfileStatus senderProfileStatus,
            boolean alimUseYn
    ) {
        this.profileInspectionStatus = profileInspectionStatus;
        this.senderProfileStatus = senderProfileStatus;
        this.alimUseYn = alimUseYn;
    }

    public void updateCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public boolean isInspectionApproved() {
        return profileInspectionStatus.isApproved();
    }

    public boolean isProfileActive() {
        return senderProfileStatus.isActive();
    }

    private static void validate(AligoSenderProfileCreateState state) {
        if (FormatValidator.hasNoValue(state.getSenderKey())) {
            throw new InvalidAligoSenderProfileException("senderKey는 필수입니다.");
        }
        if (FormatValidator.hasNoValue(state.getPlusId())) {
            throw new InvalidAligoSenderProfileException("plusId는 필수입니다.");
        }
    }
}
