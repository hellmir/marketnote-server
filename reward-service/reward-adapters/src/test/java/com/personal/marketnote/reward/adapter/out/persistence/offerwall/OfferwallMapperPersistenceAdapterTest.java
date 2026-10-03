package com.personal.marketnote.reward.adapter.out.persistence.offerwall;

import com.personal.marketnote.reward.adapter.out.persistence.offerwall.entity.OfferwallMapperJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.offerwall.repository.OfferwallMapperJpaRepository;
import com.personal.marketnote.reward.domain.attendance.RewardQuantity;
import com.personal.marketnote.reward.domain.offerwall.OfferwallMapper;
import com.personal.marketnote.reward.domain.offerwall.OfferwallMapperSnapshotState;
import com.personal.marketnote.reward.domain.offerwall.OfferwallType;
import com.personal.marketnote.reward.domain.offerwall.UserDeviceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("OfferwallMapperPersistenceAdapter 테스트")
class OfferwallMapperPersistenceAdapterTest {

    @Mock
    private OfferwallMapperJpaRepository repository;

    @InjectMocks
    private OfferwallMapperPersistenceAdapter adapter;

    private OfferwallMapperJpaEntity buildEntity(Long id, boolean isSuccess, short failureCount) {
        OfferwallMapper restored = OfferwallMapper.from(OfferwallMapperSnapshotState.builder()
                .id(id)
                .offerwallType(OfferwallType.ADPOPCORN)
                .rewardKey("REWARD_KEY_001")
                .userKey("user-key-1")
                .userDeviceType(UserDeviceType.ANDROID)
                .campaignKey("CAMP_KEY_001")
                .campaignType(1)
                .campaignName("캠페인")
                .quantity(RewardQuantity.of(100L))
                .signedValue("signed")
                .appKey(1001)
                .appName("앱")
                .adid("adid")
                .idfa("idfa")
                .isSuccess(isSuccess)
                .failureCount(failureCount)
                .attendedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build());
        return OfferwallMapperJpaEntity.from(restored);
    }

    @Test
    @DisplayName("save는 엔티티를 저장하고 도메인을 반환한다")
    void shouldSave() {
        // given
        OfferwallMapperJpaEntity entity = buildEntity(1L, true, (short) 0);
        given(repository.save(any(OfferwallMapperJpaEntity.class))).willReturn(entity);

        // when
        OfferwallMapper result = adapter.save(entity.toDomain());

        // then
        assertThat(result.getRewardKey()).isEqualTo("REWARD_KEY_001");
        assertThat(result.getIsSuccess()).isTrue();
        verify(repository).save(any(OfferwallMapperJpaEntity.class));
    }

    @Test
    @DisplayName("existsByOfferwallTypeAndRewardKeyAndIsSuccess는 리포지토리 결과를 그대로 반환한다")
    void shouldDelegateExists() {
        given(repository.existsByOfferwallTypeAndRewardKeyAndIsSuccess(OfferwallType.ADPOPCORN, "REWARD_KEY_001", true))
                .willReturn(true);
        assertThat(adapter.existsByOfferwallTypeAndRewardKeyAndIsSuccess(
                OfferwallType.ADPOPCORN, "REWARD_KEY_001", true)).isTrue();
    }

    @Test
    @DisplayName("findTopFailedOfferwallMapper는 가장 실패 카운트가 큰 매퍼를 반환한다")
    void shouldFindTopFailedOfferwallMapper() {
        // given
        given(repository.findTop1ByOfferwallTypeAndRewardKeyAndIsSuccessFalseOrderByFailureCountDesc(
                OfferwallType.ADPOPCORN, "REWARD_KEY_001"))
                .willReturn(Optional.of(buildEntity(1L, false, (short) 3)));

        // when
        Optional<OfferwallMapper> result = adapter.findTopFailedOfferwallMapper(
                OfferwallType.ADPOPCORN, "REWARD_KEY_001");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getIsSuccess()).isFalse();
        assertThat(result.get().getFailureCount()).isEqualTo((short) 3);
    }

    @Test
    @DisplayName("findTopFailedOfferwallMapper는 결과가 없으면 빈 Optional을 반환한다")
    void shouldReturnEmptyWhenNoFailedMapper() {
        given(repository.findTop1ByOfferwallTypeAndRewardKeyAndIsSuccessFalseOrderByFailureCountDesc(
                OfferwallType.ADPOPCORN, "REWARD_KEY_001"))
                .willReturn(Optional.empty());
        assertThat(adapter.findTopFailedOfferwallMapper(OfferwallType.ADPOPCORN, "REWARD_KEY_001")).isEmpty();
    }
}
