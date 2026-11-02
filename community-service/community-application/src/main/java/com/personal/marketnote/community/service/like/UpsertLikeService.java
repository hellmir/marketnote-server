package com.personal.marketnote.community.service.like;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.community.domain.like.Like;
import com.personal.marketnote.community.exception.LikeNotFoundException;
import com.personal.marketnote.community.mapper.LikeCommandToStateMapper;
import com.personal.marketnote.community.port.in.command.like.UpsertLikeCommand;
import com.personal.marketnote.community.port.in.result.like.UpsertLikeResult;
import com.personal.marketnote.community.port.in.usecase.like.GetLikeUseCase;
import com.personal.marketnote.community.port.in.usecase.like.UpsertLikeUseCase;
import com.personal.marketnote.community.port.out.like.SaveLikePort;
import com.personal.marketnote.community.port.out.like.UpdateLikePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class UpsertLikeService implements UpsertLikeUseCase {
    private final GetLikeUseCase getLikeUseCase;
    private final SaveLikePort saveLikePort;
    private final UpdateLikePort updateLikePort;

    @Override
    public UpsertLikeResult upsertLike(UpsertLikeCommand command) {
        try {
            Like like = getLikeUseCase.getLike(command.targetType(), command.targetId(), command.userId());

            // 좋아요 상태가 변경된 경우에만 갱신(따닥 이슈 방지)
            if (like.isStatusChanged(command.isLiked())) {
                like.revert();
                updateLikePort.update(like);
            }

            return UpsertLikeResult.from(like, false);
        } catch (LikeNotFoundException lnfe) {
            Like like = Like.from(LikeCommandToStateMapper.mapToState(command));
            saveLikePort.save(like);

            return UpsertLikeResult.from(like, true);
        }
    }
}
