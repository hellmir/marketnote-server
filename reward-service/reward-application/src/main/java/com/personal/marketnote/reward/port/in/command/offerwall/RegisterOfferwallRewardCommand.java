package com.personal.marketnote.reward.port.in.command.offerwall;

import com.personal.marketnote.reward.domain.offerwall.OfferwallType;
import com.personal.marketnote.reward.domain.offerwall.UserDeviceType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RegisterOfferwallRewardCommand(
        OfferwallType offerwallType,
        String rewardUnit,
        String rewardKey,
        String userKey,
        UserDeviceType userDeviceType,
        String campaignKey,
        Integer campaignType,
        String campaignName,
        Long quantity,
        String signedValue,
        Integer appKey,
        String appName,
        String adid,
        String idfa,
        LocalDateTime attendedAt
) {
}
