package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class AligoBrandTalkMessageSnapshotState {

    private Long id;
    private String senderKey;
    private String sender;
    private BrandTalkKakaoTarget kakaoTarget;
    private boolean advertYn;
    private String templateCode;
    private BrandTalkMessageStatus messageStatus;
    private BrandTalkMessageCostType costType;
    private Integer unitCost;
    private Integer totalCost;
    private String aligoMid;
    private int successCount;
    private int failCount;
    private boolean failoverEnabled;
    private String failoverSubject;
    private BrandTalkFailoverType failoverType;
    private LocalDateTime requestedAt;
    private LocalDateTime sentAt;
    private List<AligoBrandTalkMessageRecipientSnapshotState> recipients;
    private EntityStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
