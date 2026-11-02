package com.personal.marketnote.reward.domain.offerwall;

import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OfferwallMapperSnapshotState {
    private final Long id;
    private final OfferwallType offerwallType;
    private final String rewardKey;
    private final String userKey;
    private final UserDeviceType userDeviceType;
    private final String campaignKey;
    private final Integer campaignType;
    private final String campaignName;
    private final RewardQuantity quantity;
    private final String signedValue;
    private final Integer appKey;
    private final String appName;
    private final String adid;
    private final String idfa;
    private final Boolean isSuccess;
    private final short failureCount;
    private final LocalDateTime attendedAt;
    private final LocalDateTime createdAt;
}
