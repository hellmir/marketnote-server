package com.personal.marketnote.reward.domain.offerwall;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class OfferwallMapper {
    private Long id;
    private OfferwallType offerwallType;
    private String rewardKey;
    private String userKey;
    private UserDeviceType userDeviceType;
    private String campaignKey;
    private Integer campaignType;
    private String campaignName;
    private RewardQuantity quantity;
    private String signedValue;
    private Integer appKey;
    private String appName;
    private String adid;
    private String idfa;
    private Boolean isSuccess;
    private short failureCount;
    private LocalDateTime attendedAt;
    private LocalDateTime createdAt;

    public static OfferwallMapper from(OfferwallMapperCreateState state) {
        return OfferwallMapper.builder()
                .offerwallType(state.getOfferwallType())
                .rewardKey(state.getRewardKey())
                .userKey(state.getUserKey())
                .userDeviceType(state.getUserDeviceType())
                .campaignKey(state.getCampaignKey())
                .campaignType(state.getCampaignType())
                .campaignName(state.getCampaignName())
                .quantity(state.getQuantity())
                .signedValue(state.getSignedValue())
                .appKey(state.getAppKey())
                .appName(state.getAppName())
                .adid(state.getAdid())
                .idfa(state.getIdfa())
                .isSuccess(state.isSuccess())
                .attendedAt(state.getAttendedAt())
                .build();
    }

    public static OfferwallMapper fromFailed(OfferwallMapperCreateState state, short previousFailureCount) {
        return OfferwallMapper.builder()
                .offerwallType(state.getOfferwallType())
                .rewardKey(state.getRewardKey())
                .userKey(state.getUserKey())
                .userDeviceType(state.getUserDeviceType())
                .campaignKey(state.getCampaignKey())
                .campaignType(state.getCampaignType())
                .campaignName(state.getCampaignName())
                .quantity(state.getQuantity())
                .signedValue(state.getSignedValue())
                .appKey(state.getAppKey())
                .appName(state.getAppName())
                .adid(state.getAdid())
                .idfa(state.getIdfa())
                .isSuccess(false)
                .failureCount(++previousFailureCount)
                .attendedAt(state.getAttendedAt())
                .build();
    }

    public static OfferwallMapper from(OfferwallMapperSnapshotState state) {
        return OfferwallMapper.builder()
                .id(state.getId())
                .offerwallType(state.getOfferwallType())
                .rewardKey(state.getRewardKey())
                .userKey(state.getUserKey())
                .userDeviceType(state.getUserDeviceType())
                .campaignKey(state.getCampaignKey())
                .campaignType(state.getCampaignType())
                .campaignName(state.getCampaignName())
                .quantity(state.getQuantity())
                .signedValue(state.getSignedValue())
                .appKey(state.getAppKey())
                .appName(state.getAppName())
                .adid(state.getAdid())
                .idfa(state.getIdfa())
                .isSuccess(state.getIsSuccess())
                .failureCount(state.getFailureCount())
                .attendedAt(state.getAttendedAt())
                .createdAt(state.getCreatedAt())
                .build();
    }

    public void addFailureCount() {

    }

    public Long getQuantityValue() {
        if (FormatValidator.hasNoValue(quantity)) {
            return null;
        }
        return quantity.getValue();
    }
}
