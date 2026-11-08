package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AligoBrandTalkMessageSnapshotState {

    private final Long id;
    private final String senderKey;
    private final String sender;
    private final BrandTalkKakaoTarget kakaoTarget;
    private final boolean advertYn;
    private final String templateCode;
    private final BrandTalkMessageStatus messageStatus;
    private final BrandTalkMessageCostType costType;
    private final Integer unitCost;
    private final Integer totalCost;
    private final String aligoMid;
    private final int successCount;
    private final int failCount;
    private final boolean failoverEnabled;
    private final String failoverSubject;
    private final BrandTalkFailoverType failoverType;
    private final LocalDateTime requestedAt;
    private final LocalDateTime sentAt;
    private final List<AligoBrandTalkMessageRecipientSnapshotState> recipients;
    private final EntityStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
}
