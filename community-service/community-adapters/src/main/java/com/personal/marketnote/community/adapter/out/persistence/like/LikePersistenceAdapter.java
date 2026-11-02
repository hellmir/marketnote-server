package com.personal.marketnote.community.adapter.out.persistence.like;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.community.adapter.out.mapper.LikeJpaEntityToDomainMapper;
import com.personal.marketnote.community.adapter.out.persistence.like.entity.LikeJpaEntity;
import com.personal.marketnote.community.adapter.out.persistence.like.repository.LikeJpaRepository;
import com.personal.marketnote.community.domain.like.Like;
import com.personal.marketnote.community.domain.like.LikeTargetType;
import com.personal.marketnote.community.exception.LikeNotFoundException;
import com.personal.marketnote.community.port.out.like.FindLikePort;
import com.personal.marketnote.community.port.out.like.SaveLikePort;
import com.personal.marketnote.community.port.out.like.UpdateLikePort;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class LikePersistenceAdapter implements SaveLikePort, FindLikePort, UpdateLikePort {
    private final LikeJpaRepository likeJpaRepository;

    @Override
    public void save(Like like) {
        likeJpaRepository.save(LikeJpaEntity.from(like));
    }

    @Override
    public boolean existsByTargetAndUser(LikeTargetType targetType, Long targetId, Long userId) {
        return likeJpaRepository.existsByTarget(targetType, targetId, userId);
    }

    @Override
    public Optional<Like> findByTargetAndUser(LikeTargetType targetType, Long targetId, Long userId)
            throws LikeNotFoundException {
        return LikeJpaEntityToDomainMapper.mapToDomain(
                findEntityByTargetAndUser(targetType, targetId, userId)
        );
    }

    @Override
    public void update(Like like) throws LikeNotFoundException {
        LikeJpaEntity entity = findEntityByTargetAndUser(like.getTargetType(), like.getTargetId(), like.getUserId());
        entity.updateFrom(like);
    }

    private LikeJpaEntity findEntityByTargetAndUser(LikeTargetType targetType, Long targetId, Long userId)
            throws LikeNotFoundException {
        return likeJpaRepository.findByTargetAndUser(targetType, targetId, userId)
                .orElseThrow(() -> new LikeNotFoundException(targetType, targetId, userId));
    }
}
