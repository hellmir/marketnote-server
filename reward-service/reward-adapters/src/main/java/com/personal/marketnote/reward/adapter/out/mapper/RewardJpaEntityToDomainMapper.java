package com.personal.marketnote.reward.adapter.out.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.adapter.out.persistence.offerwall.entity.OfferwallMapperJpaEntity;
import com.personal.marketnote.reward.domain.offerwall.OfferwallMapper;

import java.util.Optional;

public class RewardJpaEntityToDomainMapper {

    public static Optional<OfferwallMapper> mapToDomain(OfferwallMapperJpaEntity entity) {
        if (FormatValidator.hasNoValue(entity)) {
            return Optional.empty();
        }
        return Optional.of(entity.toDomain());
    }
}
