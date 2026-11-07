package com.personal.marketnote.community.service.like;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.community.domain.like.Like;
import com.personal.marketnote.community.domain.like.LikeTargetType;
import com.personal.marketnote.community.exception.LikeNotFoundException;
import com.personal.marketnote.community.port.in.usecase.like.GetLikeUseCase;
import com.personal.marketnote.community.port.out.like.FindLikePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true, propagation = REQUIRES_NEW)
public class GetLikeService implements GetLikeUseCase {
    private final FindLikePort findLikePort;

    @Override
    public boolean existsUserLike(LikeTargetType targetType, Long targetId, Long userId) {
        return findLikePort.existsByTargetAndUser(targetType, targetId, userId);
    }

    @Override
    public Like getLike(LikeTargetType targetType, Long targetId, Long userId) {
        return findLikePort.findByTargetAndUser(targetType, targetId, userId)
                .orElseThrow(() -> {
                    log.warn("좋아요를 찾을 수 없습니다. targetType={}, targetId={}, userId={}", targetType, targetId, userId);
                    return new LikeNotFoundException();
                });
    }
}
