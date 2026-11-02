package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class AligoSenderProfileSnapshotState {

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
    private EntityStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
