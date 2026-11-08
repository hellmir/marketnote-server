package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AligoSenderProfileSnapshotState {

    private final Long id;
    private final String senderKey;
    private final String plusId;
    private final String phoneNumber;
    private final String categoryCode;
    private final String name;
    private final String licenseNum;
    private final AligoProfileInspectionStatus profileInspectionStatus;
    private final AligoSenderProfileStatus senderProfileStatus;
    private final boolean alimUseYn;
    private final String uuid;
    private final EntityStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
}
