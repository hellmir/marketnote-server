package com.personal.marketnote.notification.domain.aligo;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class AligoBrandTalkMessageCreateState {

    private String senderKey;
    private String sender;
    private BrandTalkKakaoTarget kakaoTarget;
    private boolean advertYn;
    private String templateCode;
    private boolean failoverEnabled;
    private String failoverSubject;
    private BrandTalkFailoverType failoverType;
    private LocalDateTime requestedAt;
    private List<AligoBrandTalkMessageRecipientCreateState> recipients;
}
