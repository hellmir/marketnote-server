package com.personal.marketnote.notification.domain.aligo;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class AligoSenderProfileCreateState {

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
}
