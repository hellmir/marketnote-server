package com.personal.marketnote.reward.domain.offerwall;

import com.personal.marketnote.reward.domain.attendance.RewardQuantity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OfferwallMapper 테스트")
class OfferwallMapperTest {

    private OfferwallMapperCreateState createState() {
        return OfferwallMapperCreateState.builder()
                .offerwallType(OfferwallType.ADPOPCORN)
                .rewardKey("reward-key-1")
                .userKey("user-key-1")
                .userDeviceType(UserDeviceType.ANDROID)
                .campaignKey("campaign-key-1")
                .campaignType(1)
                .campaignName("테스트 캠페인")
                .quantity(RewardQuantity.of(100L))
                .signedValue("signed-value-1")
                .appKey(1)
                .appName("테스트 앱")
                .adid("adid-1")
                .idfa("idfa-1")
                .isSuccess(true)
                .attendedAt(LocalDateTime.of(2026, 4, 11, 10, 0, 0))
                .build();
    }

    @Nested
    @DisplayName("fromFailed()")
    class FromFailed {

        @Test
        @DisplayName("실패 카운트가 이전 값에서 1 증가한다")
        void shouldIncrementFailureCountByOne() {
            // given
            OfferwallMapperCreateState state = createState();
            short previousFailureCount = 3;

            // when
            OfferwallMapper mapper = OfferwallMapper.fromFailed(state, previousFailureCount);

            // then
            assertThat(mapper.getFailureCount()).isEqualTo((short) 4);
        }

        @Test
        @DisplayName("isSuccess가 항상 false로 설정된다")
        void shouldAlwaysSetIsSuccessToFalse() {
            // given
            OfferwallMapperCreateState state = createState();

            // when
            OfferwallMapper mapper = OfferwallMapper.fromFailed(state, (short) 0);

            // then
            assertThat(mapper.getIsSuccess()).isFalse();
            assertThat(mapper.getFailureCount()).isEqualTo((short) 1);
        }

        @Test
        @DisplayName("CreateState의 필드가 그대로 매핑된다")
        void shouldMapAllFieldsFromCreateState() {
            // given
            OfferwallMapperCreateState state = createState();

            // when
            OfferwallMapper mapper = OfferwallMapper.fromFailed(state, (short) 0);

            // then
            assertThat(mapper.getOfferwallType()).isEqualTo(OfferwallType.ADPOPCORN);
            assertThat(mapper.getRewardKey()).isEqualTo("reward-key-1");
            assertThat(mapper.getUserKey()).isEqualTo("user-key-1");
            assertThat(mapper.getCampaignKey()).isEqualTo("campaign-key-1");
            assertThat(mapper.getQuantityValue()).isEqualTo(100L);
        }
    }
}
