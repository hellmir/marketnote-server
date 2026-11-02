package com.personal.marketnote.reward.domain.offerwall;

import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OfferwallMapperCreateState {
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
    private final boolean isSuccess;
    private final LocalDateTime attendedAt;
}
