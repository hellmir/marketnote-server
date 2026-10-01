package com.personal.marketnote.reward.adapter.out.persistence.offerwall.repository;

import com.personal.marketnote.reward.adapter.out.persistence.offerwall.entity.OfferwallMapperJpaEntity;
import com.personal.marketnote.reward.domain.offerwall.OfferwallType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OfferwallMapperJpaRepository extends JpaRepository<OfferwallMapperJpaEntity, Long> {
    boolean existsByOfferwallTypeAndRewardKeyAndIsSuccess(
            OfferwallType offerwallType, String rewardKey, boolean isSuccess
    );

    Optional<OfferwallMapperJpaEntity> findTop1ByOfferwallTypeAndRewardKeyAndIsSuccessFalseOrderByFailureCountDesc(
            OfferwallType offerwallType,
            String rewardKey
    );
}
