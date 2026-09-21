package com.personal.marketnote.reward.adapter.out.persistence.offerwall.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.domain.offerwall.OfferwallMapper;
import com.personal.marketnote.reward.domain.offerwall.OfferwallMapperSnapshotState;
import com.personal.marketnote.reward.domain.offerwall.OfferwallType;
import com.personal.marketnote.reward.domain.offerwall.UserDeviceType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "offerwall_mapper",
        indexes = {
                @Index(
                        name = "idx_offerwall_type_reward_key_success_failure_desc",
                        columnList = "offerwall_type, reward_key, success_yn, failure_count DESC"
                )
        }
)
@EntityListeners(value = AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class OfferwallMapperJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "offerwall_type", nullable = false, length = 15)
    private OfferwallType offerwallType;

    @Column(name = "reward_key", nullable = false, length = 256)
    private String rewardKey;

    @Column(name = "user_key", nullable = false, length = 128)
    private String userKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_device_type", nullable = false, length = 15)
    private UserDeviceType userDeviceType;

    @Column(name = "campaign_key", nullable = false, length = 50)
    private String campaignKey;

    @Column(name = "campaign_type")
    private Integer campaignType;

    @Column(name = "campaign_name", length = 256)
    private String campaignName;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(name = "signed_value", nullable = false, length = 256)
    private String signedValue;

    @Column(name = "app_key")
    private Integer appKey;

    @Column(name = "app_name", length = 50)
    private String appName;

    @Column(name = "adid", length = 128)
    private String adid;

    @Column(name = "idfa", length = 128)
    private String idfa;

    @Column(name = "success_yn", nullable = false)
    private Boolean isSuccess;

    @Column(name = "failure_count", nullable = false, columnDefinition = "SMALLINT DEFAULT 0")
    private short failureCount;

    @Column(name = "attended_at")
    private LocalDateTime attendedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    public static OfferwallMapperJpaEntity from(OfferwallMapper offerwallMapper) {
        return OfferwallMapperJpaEntity.builder()
                .id(offerwallMapper.getId())
                .offerwallType(offerwallMapper.getOfferwallType())
                .rewardKey(offerwallMapper.getRewardKey())
                .userKey(offerwallMapper.getUserKey())
                .userDeviceType(offerwallMapper.getUserDeviceType())
                .campaignKey(offerwallMapper.getCampaignKey())
                .campaignType(offerwallMapper.getCampaignType())
                .campaignName(offerwallMapper.getCampaignName())
                .quantity(offerwallMapper.getQuantityValue())
                .signedValue(offerwallMapper.getSignedValue())
                .appKey(offerwallMapper.getAppKey())
                .appName(offerwallMapper.getAppName())
                .adid(offerwallMapper.getAdid())
                .idfa(offerwallMapper.getIdfa())
                .isSuccess(offerwallMapper.getIsSuccess())
                .failureCount(offerwallMapper.getFailureCount())
                .attendedAt(offerwallMapper.getAttendedAt())
                .createdAt(offerwallMapper.getCreatedAt())
                .build();
    }

    public OfferwallMapper toDomain() {
        return OfferwallMapper.from(
                OfferwallMapperSnapshotState.builder()
                        .id(id)
                        .offerwallType(offerwallType)
                        .rewardKey(rewardKey)
                        .userKey(userKey)
                        .userDeviceType(userDeviceType)
                        .campaignKey(campaignKey)
                        .campaignType(campaignType)
                        .campaignName(campaignName)
                        .quantity(quantity == null ? null : RewardQuantity.of(quantity))
                        .signedValue(signedValue)
                        .appKey(appKey)
                        .appName(appName)
                        .adid(adid)
                        .idfa(idfa)
                        .isSuccess(isSuccess)
                        .failureCount(failureCount)
                        .attendedAt(attendedAt)
                        .createdAt(createdAt)
                        .build()
        );
    }
}
